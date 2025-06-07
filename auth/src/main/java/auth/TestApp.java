package auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/app")
public class TestApp extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		Cookie[] cookies = req.getCookies();
		String jwtCookieValue = null;

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("hriday_tech_auth_token".equals(cookie.getName())) {
					jwtCookieValue = cookie.getValue();
					break;
				}
			}
		}

		if (jwtCookieValue == null) {
			resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			resp.setContentType("application/json");
			resp.getWriter().write("{\"type\":\"error\",\"message\":\"No auth token cookie present\"}");
			return;
		}
		
		// Create a connection to your real servlet
		URL url = new URL("http://localhost:8080/auth/getUser"); // adjust port/path if needed
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		// Attach the cookie
		con.setRequestProperty("Cookie", "hriday_tech_auth_token=" + jwtCookieValue);

		// Read the response
		int status = con.getResponseCode();
		BufferedReader in = new BufferedReader(
				new InputStreamReader(status == 200 ? con.getInputStream() : con.getErrorStream()));

		String inputLine;
		StringBuilder content = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}

		in.close();
		con.disconnect();

		// Send the user info back to the browser
		resp.setStatus(status);
		resp.setContentType("application/json");
		resp.getWriter().write(content.toString());
		// String jwt = req.getHeader("Authorization").split(" ")[1];
		// check is jwt valid
		// get user info
		// return user info
	}
}
