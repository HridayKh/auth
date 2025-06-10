package servlets.security;

import java.io.IOException;
import java.sql.Connection;

import org.json.JSONObject;

import db.SessionDAO;
import db.dbAuth;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.AuthUtil;
import utils.HttpUtil;

@WebServlet("/v1/removeSession")
public class RemoveSession extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		try (Connection conn = dbAuth.getConnection()) {
			String uuid = AuthUtil.getUserUUIDFromAuthCookie(req, resp, conn);
			if (uuid == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "Not logged in");
				return;
			}

			JSONObject body = HttpUtil.readBodyJSON(req);
			String sessionId = body.has("session_id") ? body.getString("session_id") : null;
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