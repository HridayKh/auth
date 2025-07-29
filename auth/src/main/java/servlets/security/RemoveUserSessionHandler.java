package servlets.security;

import java.io.IOException;
import java.sql.Connection;

import db.SessionDAO;
import db.dbAuth;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.AuthUtil;
import utils.HttpUtil;

public class RemoveUserSessionHandler {

	public static void removeUserSession(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {

		try (Connection conn = dbAuth.getConnection()) {
			String uuid = AuthUtil.getUserUUIDFromAuthCookie(req, resp, conn);
			if (uuid == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "Not logged in");
				return;
			}

			String sessionId = (String) req.getAttribute("sessionId");
			if (sessionId == null || sessionId.isBlank() || sessionId.isEmpty()) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Session Id Missing");
				return;
			}
			if (!SessionDAO.invalidateSession(conn, sessionId, uuid)) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
						"Session not found or you do not have permission to invalidate it.");
				return;
			}
			HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "Session Removed Successfully");
		} catch (Exception e) {
			e.printStackTrace();
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Internal Server Error");
		}
	}
}