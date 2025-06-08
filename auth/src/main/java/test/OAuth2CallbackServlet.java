package test;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.net.URLEncoder; // For encoding URL parameters
import java.nio.charset.StandardCharsets; // For encoding URL parameters
import java.util.Collections;
import java.util.Arrays;
import java.util.List;

@WebServlet("/oauth2callback") // This must match your REDIRECT_URI path
public class OAuth2CallbackServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String CLIENT_ID = "HHHHHHHHHH.apps.googleusercontent.com";
	private static final String CLIENT_SECRET = "HHHHHHHHHH";
	private static final String REDIRECT_URI = "http://localhost:8080/auth/oauth2callback";

	private static final String SUCCESS_REDIRECT_URL = "http://localhost:8080/auth/dashboard";
	private static final String FAILURE_REDIRECT_URL = "http://localhost:8080/auth/google.html";

	private static final List<String> SCOPES = Arrays.asList("openid", "email", "profile");

	private static GoogleAuthorizationCodeFlow flow;

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			flow = new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(),
					CLIENT_ID, CLIENT_SECRET, SCOPES).setAccessType("offline").setApprovalPrompt("auto").build();
		} catch (Exception e) {
			System.err.println("Error initializing GoogleAuthorizationCodeFlow: " + e.getMessage());
			throw new ServletException("Failed to initialize Google OAuth flow", e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// This method receives the redirect from Google with the 'code' or 'error'
		// parameter.
		String code = request.getParameter("code");
		String error = request.getParameter("error");
		String state = request.getParameter("state"); // Retrieve the state parameter from Google's redirect

		HttpSession session = request.getSession(false); // Do not create a new session if none exists

		// --- CSRF Protection: Verify the state parameter ---
		if (session == null || session.getAttribute("oauth_state") == null
				|| !session.getAttribute("oauth_state").equals(state)) {
			System.err.println("Invalid or missing OAuth state. Possible CSRF attack.");
			redirectToFailure(request, response, "Invalid state parameter. Please try again.");
			return;
		}
		// Remove the state from session after verification to prevent reuse
		session.removeAttribute("oauth_state");

		if (error != null) {
			System.out.println("WARN: " + "OAuth Error (GET from Google): " + error);
			redirectToFailure(request, response, "Google OAuth error: " + error);
			return;
		}

		if (code != null) {
			System.out.println("Authorization code received via GET from Google: " + code);
			processAuthCode(code, request, response);
		} else {
			redirectToFailure(request, response, "Invalid request. No authorization code received from Google.");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("WARN: "
				+ "POST request received at OAuth2CallbackServlet. This servlet is intended for GET redirects from Google.");
		redirectToFailure(request, response, "Unsupported request method. Please use the Google sign-in button.");
	}

	private void processAuthCode(String authCode, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		try {
			// Exchange the authorization code for tokens
			GoogleTokenResponse tokenResponse = flow.newTokenRequest(authCode).setRedirectUri(REDIRECT_URI).execute();

			String accessToken = tokenResponse.getAccessToken();
			String idTokenString = tokenResponse.getIdToken();
			String refreshToken = tokenResponse.getRefreshToken();

			System.out.println("Access Token: " + accessToken);
			if (idTokenString != null) {
				System.out.println("ID Token: " + idTokenString);
			}
			if (refreshToken != null) {
				System.out.println("Refresh Token: " + refreshToken);
			}

			GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
					GsonFactory.getDefaultInstance()).setAudience(Collections.singletonList(CLIENT_ID)).build();

			GoogleIdToken idToken = verifier.verify(idTokenString);

			if (idToken != null) {
				Payload payload = idToken.getPayload();

				String userId = payload.getSubject();
				String email = (String) payload.get("email");
				String name = (String) payload.get("name");
				String pictureUrl = (String) payload.get("picture");
				Boolean emailVerified = (Boolean) payload.get("email_verified");

				System.out.println("User ID: " + userId);
				System.out.println(
						"Email: " + email + " (Verified: " + (emailVerified != null ? emailVerified : "N/A") + ")");
				System.out.println("Name: " + name);
				System.out.println("Picture URL: " + pictureUrl);

				// --- IMPORTANT: Establish user session ---
				HttpSession session = request.getSession(true);
				session.setAttribute("userId", userId);
				session.setAttribute("userName", name);
				session.setAttribute("userEmail", email);
				session.setAttribute("isLoggedIn", true);

				// Redirect to the success page (e.g., dashboard)
				response.sendRedirect(SUCCESS_REDIRECT_URL);

			} else {
				System.out.println("WARN: " + "Invalid ID Token received from Google.");
				redirectToFailure(request, response, "Invalid Google ID token.");
			}

		} catch (TokenResponseException e) {
			System.err.println(
					"Error exchanging code for tokens (Google API error): " + e.getDetails().getErrorDescription());
			redirectToFailure(request, response, "Failed to exchange code for tokens: " + e.getDetails().getError());
		} catch (IOException e) {
			System.err.println("IO Error during token exchange or ID token verification: " + e.getMessage());
			redirectToFailure(request, response, "Internal server error during token exchange.");
		} catch (GeneralSecurityException e) {
			System.err.println("Security exception during ID token verification: " + e.getMessage());
			redirectToFailure(request, response, "ID token verification failed.");
		} catch (Exception e) {
			System.err.println("Unexpected error in processAuthCode: " + e.getMessage());
			redirectToFailure(request, response, "An unexpected error occurred during login.");
		}
	}

	private void redirectToFailure(HttpServletRequest request, HttpServletResponse response, String message)
			throws IOException {
		String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
		response.sendRedirect(FAILURE_REDIRECT_URL + "?status=failure&message=" + encodedMessage);
	}
}