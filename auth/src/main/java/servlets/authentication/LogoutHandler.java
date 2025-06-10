package servlets.authentication;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.AuthUtil;
import utils.HttpUtil;

public class LogoutHandler {

	public static void logoutUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		HttpSession session = req.getSession(false);
		if (session != null) {
			session.invalidate();
		}

		AuthUtil.clearAuthCookie(resp);
		HttpUtil.clearUserCookie(resp);

		HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "Logged out and cookie removed");
	}

}
