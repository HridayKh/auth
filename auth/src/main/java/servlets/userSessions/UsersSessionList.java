package servlets.userSessions;

import java.io.IOException;
import java.sql.Connection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import utils.AuthUtil;
import utils.HttpUtil;
import db.SessionDAO;
import db.UsersDAO;
import db.dbAuth;
import entities.Session;
import entities.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class UsersSessionList {

	private static final Logger log = LogManager.getLogger(UsersSessionList.class);
	public static void listUserSessions(HttpServletRequest req, HttpServletResponse resp, Map<String, String> params) throws IOException {
		try (Connection conn = dbAuth.getConnection()) {
			String uuid = AuthUtil.getUserUUIDFromAuthCookie(req, resp, conn);
			if (uuid == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "Not logged in");
				return;
			}

			// Get userId from path parameter (set by the routing servlet)
			String requestedUserId = params.get("userId");
			
			// For security, users can only view their own sessions unless they have admin permissions
			// For now, enforce that users can only see their own sessions
			if (requestedUserId != null && !requestedUserId.equals(uuid)) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, "error", "Access denied");
				return;
			}

			User user = UsersDAO.getUserByUuid(conn, uuid);
			if (user == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "error", "User not found");
				return;
			}

			Session[] sessions = SessionDAO.getAllSessionsOfUser(conn, uuid);

			JSONArray sessionsJsonArr = new JSONArray();
			for (Session s : sessions) {
				JSONObject sJson = new JSONObject();
				sJson.put("session_id", s.sessionId());
				sJson.put("user_uuid", s.userUuid());
				sJson.put("created_at", s.createdAt());
				sJson.put("last_accessed_at", s.lastAccessedAt());
				sJson.put("expires_at", s.expiresAt());
				sJson.put("user_agent", s.userAgent());
				sJson.put("is_active", s.isActive());
				sessionsJsonArr.put(sJson);
			}

			JSONObject respJson = new JSONObject();
			respJson.put("type", "success");
			respJson.put("sessions", sessionsJsonArr);
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentType("application/json");
			resp.getWriter().write(respJson.toString());

		} catch (Exception e) {
			log.catching(e);
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Internal Server Error");
		}
	}
}
