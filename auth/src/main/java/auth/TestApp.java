package auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import db.dbAuth;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.HttpUtil;

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
			HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "Not logged in");
			return;
		}

		try {
			URL url = new URI(dbAuth.BACK_HOST + "/v1/getUser").toURL();

			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			con.setRequestProperty("Cookie", "hriday_tech_auth_token=" + jwtCookieValue);

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

			resp.setStatus(status);
			resp.setContentType("application/json");
			resp.getWriter().write(content.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "unable get User info");
		} catch (URISyntaxException e) {
			HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "unable get User info");
			e.printStackTrace();
		}
	}
}
