package servlets.userSessions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.AuthUtil;
import utils.HttpUtil;

import java.io.IOException;
import java.util.Map;

public class UsersSessionDeleteCurrent {

//	private static final Logger log = LogManager.getLogger(UsersSessionDeleteCurrent.class);

	public static void deleteCurrentUserSession(HttpServletRequest req, HttpServletResponse resp, Map<String, String> ignoredParams) throws IOException {
		HttpSession session = req.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		AuthUtil.clearAuthCookie(resp);
		HttpUtil.clearUserCookie(resp);
		HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "Logged out and cookie removed");
	}

}
