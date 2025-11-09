package servlets.userSessions;

import db.SessionDAO;
import db.dbAuth;
import entities.Session;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.AuthUtil;
import utils.HttpUtil;

import java.io.IOException;
import java.sql.Connection;
import java.util.Map;

public class UsersSessionDelete {

	private static final Logger log = LogManager.getLogger(UsersSessionDelete.class);

	public static void deleteUserSession(HttpServletRequest req, HttpServletResponse resp,
			Map<String, String> params) throws IOException {

		try (Connection conn = dbAuth.getConnection()) {
			String uuid = AuthUtil.getUserUUIDFromAuthCookie(req, resp, conn);
			if (uuid == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "Not logged in");
				return;
			}

			String sessionId = params.get("sessionId");
			if (sessionId == null || sessionId.isBlank()) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error",
						"Session Id Missing");
				return;
			}

			Session session = SessionDAO.getSessionById(conn, sessionId);
			if (session == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "error",
						"Session not found.");
				return;
			}

			if (!session.userUuid().equals(uuid)) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, "error",
						"Not authorized to delete this session.");
				return;
			}

			if (!SessionDAO.invalidateSession(conn, sessionId)) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
						"Unable to invalidate session.");
				return;
			}

			HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "Session Removed Successfully");
		} catch (Exception e) {
			log.catching(e);
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
					"Internal Server Error");
		}
	}
}