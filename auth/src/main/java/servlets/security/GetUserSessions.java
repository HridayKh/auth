package servlets.security;

import java.io.IOException;
import java.sql.Connection;

import org.json.JSONArray;
import org.json.JSONObject;

import utils.AuthUtil;
import utils.HttpUtil;
import db.SessionDAO;
import db.UsersDAO;
import db.dbAuth;
import entities.Session;
import entities.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/v1/getSessions")
public class GetUserSessions extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		try (Connection conn = dbAuth.getConnection()) {
			String uuid = AuthUtil.getUserUUIDFromAuthCookie(req, resp, conn);
			if (uuid == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "Not logged in");
				return;
			}

			User user = UsersDAO.getUserByUuid(conn, uuid);
			if (user == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "error", "User not found");
				return;
			}

			Session[] sessions = SessionDAO.getAllSessionsOfUser(conn, uuid);

			if (sessions == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
						"Internal error: Session data could not be retrieved.");
				return;
			}

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
			e.printStackTrace(); // Log the exception for debugging
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Internal Server Error");
		}
	}
}
