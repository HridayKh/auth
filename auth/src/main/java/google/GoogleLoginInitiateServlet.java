package google;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory; // Still needed for Google API client

import db.dbAuth;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // For managing state (highly recommended)
import utils.GoogleUtil;
import utils.HttpUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

@WebServlet("/googleLoginInitiate")
public class GoogleLoginInitiateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String REDIRECT_URI = dbAuth.BACK_HOST + "/oauth2callback";
	private static final List<String> SCOPES = Arrays.asList("openid", "email", "profile");
	private static GoogleAuthorizationCodeFlow flow;

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			flow = new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(),
					dbAuth.CLIENT_ID, dbAuth.CLIENT_SECRET, SCOPES).setAccessType("offline")
					.setApprovalPrompt("no".equals(dbAuth.PROD) ? "force" : "auth").build();
		} catch (Exception e) {
			throw new ServletException("Failed to initialize Google OAuth flow", e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String redirect = request.getParameter("redirect");
		String source = request.getParameter("source");
		if (redirect == null || redirect.isBlank() || source == null || source.isBlank()) {
			HttpUtil.sendJson(response, HttpServletResponse.SC_BAD_REQUEST, "error",
					"Blank or non-existent redirect or source param");
			return;
		}

		JSONObject stateJSon = GoogleUtil.genStateJson(GoogleUtil.generateSecureRandomString(), redirect, source);
		HttpSession session = request.getSession(true);
		session.setAttribute("oauth_state", stateJSon.getString("csrf"));

		GoogleAuthorizationCodeRequestUrl authUrl = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI)
				.setState(URLEncoder.encode(stateJSon.toString(), StandardCharsets.UTF_8));

		response.sendRedirect(authUrl.build());
	}

}
