package google;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import db.UsersDAO;
import db.dbAuth;
import entities.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import utils.AuthUtil;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@WebServlet("/oauth2callback")
public class OAuth2CallbackServlet extends HttpServlet {

	private static final Logger log = LogManager.getLogger(OAuth2CallbackServlet.class);

	private static final String REDIRECT_URI = dbAuth.BACK_HOST + "/oauth2callback";
	private static final List<String> SCOPES = Arrays.asList("openid", "email", "profile");
	private static GoogleAuthorizationCodeFlow flow;
	private String REDIRECT_URL;

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			flow = new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), dbAuth.CLIENT_ID, dbAuth.CLIENT_SECRET, SCOPES).setAccessType("offline").setApprovalPrompt("auto").build();
		} catch (Exception e) {
			throw new ServletException("Failed to initialize Google OAuth flow", e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String code = request.getParameter("code");
		String error = request.getParameter("error");
		String stateStr = URLDecoder.decode(request.getParameter("state"), StandardCharsets.UTF_8);
		JSONObject stateJson = new JSONObject(stateStr);
		REDIRECT_URL = stateJson.getString("redirect");
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("oauth_state") == null || !session.getAttribute("oauth_state").equals(stateJson.getString("csrf"))) {

			redirectToFailure(response, "Invalid state parameter. Please try again.");
			return;
		}
		session.removeAttribute("oauth_state");

		if (error != null) {
			redirectToFailure(response, "Google OAuth error: " + error);
			return;
		}

		if (code != null) {
			processAuthCode(code, stateJson, request, response);
		} else {
			redirectToFailure(response, "Invalid request. No authorization code received from Google.");
		}
	}

	private void processAuthCode(String authCode, JSONObject stateJson, HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			GoogleTokenResponse tokenResponse = flow.newTokenRequest(authCode).setRedirectUri(REDIRECT_URI).execute();

			String idTokenString = tokenResponse.getIdToken();
			String refreshToken = tokenResponse.getRefreshToken();

			GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance()).setAudience(Collections.singletonList(dbAuth.CLIENT_ID)).build();

			GoogleIdToken idToken = verifier.verify(idTokenString);

			if (idToken != null) {
				Payload payload = idToken.getPayload();

				String userId = payload.getSubject();
				String email = ((String) payload.get("email")).toLowerCase();
				String name = (String) payload.get("name");
				String pictureUrl = (String) payload.get("picture");

				registerUser(userId, email, name, pictureUrl, refreshToken, stateJson.getString("source"), request, response);

			} else {
				redirectToFailure(response, "Invalid Google ID token.");
			}

		} catch (TokenResponseException e) {
			redirectToFailure(response, "Failed to exchange code for tokens: " + e.getDetails().getError());
		} catch (IOException e) {
			redirectToFailure(response, "Internal server error during token exchange.");
		} catch (GeneralSecurityException e) {
			redirectToFailure(response, "ID token verification failed.");
		}
	}

	private void registerUser(String googleUserId, String email, String name, String pictureUrl, String refreshToken, String source, HttpServletRequest request, HttpServletResponse response) throws IOException {
		try (Connection conn = dbAuth.getConnection()) {
			conn.setAutoCommit(false);
			long time = System.currentTimeMillis() / 1000L;

			User oldUser = UsersDAO.getUserByEmail(conn, email);
			User newUser;
			if (oldUser == null) {
				String user_uuid = UUID.randomUUID().toString();

				newUser = new User.Builder(user_uuid, email, time, time).fullName(name).accType("google").googleId(googleUserId).isVerified(true).lastLogin(time).profilePic(pictureUrl).refreshToken(refreshToken).build();

				UsersDAO.insertUser(conn, newUser);

				AuthUtil.createAndSetAuthCookie(conn, request, response, newUser.uuid());

			} else if (oldUser.accType().equals("google") || oldUser.accType().equals("both") || (oldUser.accType().equals("pass") && source.equals("glink"))) {

				// Start building the updated user object based on the old user's data
				// All fields are initially copied from oldUser, then selectively updated
				User.Builder userBuilder = new User.Builder(oldUser.uuid(), // UUID remains the same
					oldUser.email(), // Email remains the same
					oldUser.createdAt(), // Creation time remains the same
					time // Update lastLogin to current time
				); // No need to chain lastLogin here, we'll set it later

				// Copy existing data. This is crucial for preserving existing values.
				userBuilder.passwordHash(oldUser.passwordHash()).isVerified(oldUser.isVerified()).profilePic(oldUser.profilePic()).fullName(oldUser.fullName()).metadata(oldUser.metadata()).permissions(oldUser.permissions()).accType(oldUser.accType()) // Keep existing account type
					// initially
					.googleId(oldUser.googleId()).refreshToken(oldUser.refreshToken()).refreshTokenExpiresAt(oldUser.refreshTokenExpiresAt());

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
				// Make sure this method exists and works!
				boolean userUpdated = UsersDAO.updateUser(conn, newUser);
				if (!userUpdated) {
					conn.rollback();
					redirectToFailure(response, "Failed to update user information.");
					return;
				}
				// 6. Set authentication and user cookies for the newly logged-in/updated user
				AuthUtil.createAndSetAuthCookie(conn, request, response, newUser.uuid());

			} else {
				conn.rollback();
				redirectToFailure(response, "User already exists with this email, try a different login method.");
				return;
			}

			conn.commit();
			String encodedMessage = URLEncoder.encode("Logged in successfully", StandardCharsets.UTF_8);
			response.sendRedirect(REDIRECT_URL + "?type=success&message=" + encodedMessage);
		} catch (SQLException e) {
			log.catching(e);
		}
	}

	private void redirectToFailure(HttpServletResponse response, String message) throws IOException {
		String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
		response.sendRedirect(REDIRECT_URL + "?type=error&message=" + encodedMessage);
	}
}