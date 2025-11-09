package servlets.userSessions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.AuthUtil;
import utils.HttpUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import db.SessionDAO;
import db.dbAuth;

public class UsersSessionDeleteCurrent {

	private static final Logger log = LogManager.getLogger(UsersSessionDeleteCurrent.class);

	public static void deleteCurrentUserSession(HttpServletRequest req, HttpServletResponse resp,
			Map<String, String> ignoredParams) throws IOException {
		HttpSession session = req.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		try (Connection conn = dbAuth.getConnection()) {
			String currentSessionUuid = AuthUtil.getCurrentSessionFromAuthCookie(req, resp, conn);
			if (currentSessionUuid != null) {
				SessionDAO.invalidateSession(conn, currentSessionUuid);
			}
		} catch (Exception e) {
			log.catching(e);
		}
		AuthUtil.clearAuthCookie(resp);
		HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "Logged out and cookie removed");
	}

}
