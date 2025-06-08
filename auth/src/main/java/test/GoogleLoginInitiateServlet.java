package test;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory; // Still needed for Google API client
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // For managing state (highly recommended)

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Arrays;
import java.util.List;

@WebServlet("/google-login-initiate")
public class GoogleLoginInitiateServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	// IMPORTANT: Replace with your actual Google Client ID and Client Secret
	private static final String CLIENT_ID = "HHHHHHHHHH.apps.googleusercontent.com";
	private static final String CLIENT_SECRET = "HHHHHHHHHH";
	private static final String REDIRECT_URI = "http://localhost:8080/auth/oauth2callback";

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

		String state = generateSecureRandomString();
		HttpSession session = request.getSession(true);
		session.setAttribute("oauth_state", state);
		System.out.println("Generated OAuth state: " + state + " and stored in session.");

		// Construct the Google Authorization URL
		GoogleAuthorizationCodeRequestUrl authUrl = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI)
				.setState(state); // Add the state parameter

		System.out.println("Redirecting to Google Auth URL: " + authUrl.build());
		// Redirect the user's browser directly to Google's authorization endpoint
		response.sendRedirect(authUrl.build());
	}

	// Helper to generate a secure random string for the state parameter
	private String generateSecureRandomString() {
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[32]; // 32 bytes for a good-sized random string
		random.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}
