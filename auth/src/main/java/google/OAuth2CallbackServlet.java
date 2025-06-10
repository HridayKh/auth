package google;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import db.UsersDAO;
import db.dbAuth;
import entities.User;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload; // Corrected import for Payload

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.auth.oauth2.TokenResponseException; // Import for specific exception

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.AuthUtil;
import utils.HttpUtil;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.net.URLDecoder;
import java.net.URLEncoder; // For encoding URL parameters
import java.nio.charset.StandardCharsets; // For encoding URL parameters
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.json.JSONObject;

@WebServlet("/oauth2callback")
public class OAuth2CallbackServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String REDIRECT_URI = dbAuth.BACK_HOST + "/oauth2callback";
	private String REDIRECT_URL;

	private static final List<String> SCOPES = Arrays.asList("openid", "email", "profile");
	private static GoogleAuthorizationCodeFlow flow;

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			flow = new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(),
					dbAuth.CLIENT_ID, dbAuth.CLIENT_SECRET, SCOPES).setAccessType("offline").setApprovalPrompt("auto")
					.build();
		} catch (Exception e) {
			throw new ServletException("Failed to initialize Google OAuth flow", e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String code = request.getParameter("code");
		String error = request.getParameter("error");
		String stateStr = URLDecoder.decode(request.getParameter("state"), StandardCharsets.UTF_8);
		JSONObject stateJson = new JSONObject(stateStr);
		REDIRECT_URL = stateJson.getString("redirect");
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("oauth_state") == null
				|| !session.getAttribute("oauth_state").equals(stateJson.getString("csrf"))) {

			redirectToFailure(request, response, "Invalid state parameter. Please try again.");
			return;
		}
		session.removeAttribute("oauth_state");

		if (error != null) {

			redirectToFailure(request, response, "Google OAuth error: " + error);
			return;
		}

		if (code != null) {

			processAuthCode(code, stateJson, request, response);
		} else {
			redirectToFailure(request, response, "Invalid request. No authorization code received from Google.");
		}
	}

	private void processAuthCode(String authCode, JSONObject stateJson, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		try {
			GoogleTokenResponse tokenResponse = flow.newTokenRequest(authCode).setRedirectUri(REDIRECT_URI).execute();

			String idTokenString = tokenResponse.getIdToken();
			String refreshToken = tokenResponse.getRefreshToken();

			GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
					GsonFactory.getDefaultInstance()).setAudience(Collections.singletonList(dbAuth.CLIENT_ID)).build();

			GoogleIdToken idToken = verifier.verify(idTokenString);

			if (idToken != null) {
				Payload payload = idToken.getPayload();

				String userId = payload.getSubject();
				String email = ((String) payload.get("email")).toLowerCase();
				String name = (String) payload.get("name");
				String pictureUrl = (String) payload.get("picture");

				registerUser(userId, email, name, pictureUrl, refreshToken, stateJson.getString("source"), request,
						response);

			} else {
				redirectToFailure(request, response, "Invalid Google ID token.");
			}

		} catch (TokenResponseException e) {
			redirectToFailure(request, response, "Failed to exchange code for tokens: " + e.getDetails().getError());
		} catch (IOException e) {
			redirectToFailure(request, response, "Internal server error during token exchange.");
		} catch (GeneralSecurityException e) {
			redirectToFailure(request, response, "ID token verification failed.");
		} catch (Exception e) {
			redirectToFailure(request, response, "An unexpected error occurred during login.");
		}
	}

	private void registerUser(String googleUserId, String email, String name, String pictureUrl, String refreshToken,
			String source, HttpServletRequest request, HttpServletResponse response) throws IOException {
		try (Connection conn = dbAuth.getConnection()) {
			conn.setAutoCommit(false);
			long time = System.currentTimeMillis() / 1000L;

			User oldUser = UsersDAO.getUserByEmail(conn, email);
			User newUser;
			if (oldUser == null) {
				String user_uuid = UUID.randomUUID().toString();

				newUser = new User.Builder(user_uuid, email, time, time).fullName(name).accType("google")
						.googleId(googleUserId).isVerified(true).lastLogin(time).profilePic(pictureUrl)
						.refreshToken(refreshToken).build();

				UsersDAO.insertUser(conn, newUser);

				AuthUtil.createAndSetAuthCookie(conn, request, response, newUser.uuid());
				HttpUtil.createAndSetUserCookie(response, newUser);

			} else if (oldUser.accType().equals("google") || oldUser.accType().equals("both")
					|| (oldUser.accType().equals("pass") && source.equals("glink"))) {

				// Start building the updated user object based on the old user's data
				// All fields are initially copied from oldUser, then selectively updated
				User.Builder userBuilder = new User.Builder(oldUser.uuid(), // UUID remains the same
						oldUser.email(), // Email remains the same
						oldUser.createdAt(), // Creation time remains the same
						time // Update lastLogin to current time
				); // No need to chain lastLogin here, we'll set it later

				// Copy existing data. This is crucial for preserving existing values.
				userBuilder.passwordHash(oldUser.passwordHash()).isVerified(oldUser.isVerified())
						.profilePic(oldUser.profilePic()).fullName(oldUser.fullName()).metadata(oldUser.metadata())
						.permissions(oldUser.permissions()).accType(oldUser.accType()) // Keep existing account type
																						// initially
						.googleId(oldUser.googleId()).refreshToken(oldUser.refreshToken())
						.refreshTokenExpiresAt(oldUser.refreshTokenExpiresAt());

				// 1. Update Full Name: Prioritize Google's name if the old name is null/empty
				// or if you always want to update.
				// A common strategy is to update if the existing name is null/empty, or if
				// Google provides a new one.
				if (name != null && !name.isBlank()) {
					userBuilder.fullName(name);
				}
				// If you want to always override with Google's name:
				// userBuilder.fullName(name);

				// 2. Update Profile Picture: Use Google's profile pic if available.
				if (pictureUrl != null && !pictureUrl.isBlank()) {
					userBuilder.profilePic(pictureUrl);
				}

				// 3. Update Refresh Token: Only update if a new one was provided by the Google
				// OAuth flow.
				// (This `refreshToken` variable comes from tokenResponse.getRefreshToken())
				if (refreshToken != null) {
					userBuilder.refreshToken(refreshToken);
				}

				// 4. Handle Account Type (linking):
				if (oldUser.accType().equals("pass") && source.equals("glink")) {
					// This is the "linking" scenario: a password user is linking their Google
					// account
					userBuilder.accType("both"); // Change account type to "both"
				}

				// Always update lastLogin
				userBuilder.lastLogin(time);

				// Build the final updated User object
				newUser = userBuilder.build();

				// 5. Update the user in the database
				// You will need a UsersDAO.updateUser() method that takes a User object
				// and updates the existing record identified by its UUID.
				UsersDAO.updateUser(conn, newUser); // Make sure this method exists and works!

				// 6. Set authentication and user cookies for the newly logged-in/updated user
				AuthUtil.createAndSetAuthCookie(conn, request, response, newUser.uuid());
				HttpUtil.createAndSetUserCookie(response, newUser);

			} else {
				conn.rollback();
				redirectToFailure(request, response, "User already exists with this email");
			}

			conn.commit();
			String encodedMessage = URLEncoder.encode("Logged in successfully", StandardCharsets.UTF_8.toString());
			response.sendRedirect(REDIRECT_URL + "?type=sucecss&message=" + encodedMessage);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void redirectToFailure(HttpServletRequest request, HttpServletResponse response, String message)
			throws IOException {
		String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
		response.sendRedirect(REDIRECT_URL + "?type=error&message=" + encodedMessage);
	}
}