package servlets;

import java.io.IOException;

import db.dbAuth;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.HttpUtil;

@WebServlet("/v1/logout")
public class Logout extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		HttpSession session = req.getSession(false);
		if (session != null) {
			session.invalidate();
		}

		Cookie cookie = new Cookie("hriday_tech_auth_token", "");
		cookie.setMaxAge(0);
		cookie.setPath("/");
		if ("yes".equals(dbAuth.PROD)) {
			cookie.setSecure(true);
			cookie.setHttpOnly(true);
			cookie.setDomain("hriday.tech");
		}

		resp.addCookie(cookie);

		HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "Logged out and cookie removed");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doPost(req, resp);
	}
}
