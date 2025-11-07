package auth;

import db.dbAuth;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.HttpUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.List;
import java.util.Map;

@WebServlet("/app")
public class TestApp extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Cookie[] cookies = req.getCookies();
		String jwtCookieValue = null;

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("hridaykh_in_auth_token".equals(cookie.getName())) {
					jwtCookieValue = cookie.getValue();
					break;
				}
			}
		}

		if (jwtCookieValue == null) {
			HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "Not logged in");
			return;
		}

		try {
			URL url = new URI(dbAuth.BACK_HOST + "/v1/getUser").toURL();

			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			// Forward the auth token to the backend
			con.setRequestProperty("Cookie", "hridaykh_in_auth_token=" + jwtCookieValue);

			int status = con.getResponseCode();

			// --- NEW CODE START ---
			// 1. Get all response headers from the backend connection
			Map<String, List<String>> headerFields = con.getHeaderFields();

			// 2. Iterate through headers and forward 'Set-Cookie' to the browser
			for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
				String headerName = entry.getKey();
				if (headerName != null && headerName.equalsIgnoreCase("Set-Cookie")) { // Header names are
					// case-insensitive
					List<String> headerValues = entry.getValue();
					for (String headerValue : headerValues) {
						// Add each Set-Cookie header value to the response going back to the browser
						resp.addHeader("Set-Cookie", headerValue);
					}
				}
				// OPTIONAL: If your backend also sends other specific headers that the frontend
				// needs,
				// you might forward them here too (e.g., Authorization, X-RateLimit-*, etc.)
				// Example: if (headerName != null &&
				// headerName.equalsIgnoreCase("X-Custom-Header")) {
				// resp.addHeader("X-Custom-Header", entry.getValue().get(0));
				// }
			}
			// --- NEW CODE END ---

			BufferedReader in = new BufferedReader(
				new InputStreamReader(status == 200 ? con.getInputStream() : con.getErrorStream()));

			String inputLine;
			StringBuilder content = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}

			in.close();
			con.disconnect();

			// Set the response status and content type from the backend
			resp.setStatus(status);
			resp.setContentType("application/json");
			resp.getWriter().write(content.toString()); // Write the body from the backend

		} catch (MalformedURLException | URISyntaxException e) { // Combined catch block for similar handling
			e.printStackTrace();
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
				"Unable to get User info due to URL/URI syntax error."); // Changed to 500
		} catch (IOException e) {
			e.printStackTrace();
			// You might want to distinguish between network errors and backend 4xx/5xx
			// responses
			// For example, if status is not 200, it goes to errorStream, which is handled.
			// This catch is for actual connection/IO problems.
			HttpUtil.sendJson(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "error",
				"Failed to connect to backend user service."); // Changed to 503
		}
	}
}