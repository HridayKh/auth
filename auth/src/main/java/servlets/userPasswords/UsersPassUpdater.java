package servlets.userPasswords;

import db.UsersDAO;
import db.dbAuth;
import entities.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import utils.AuthUtil;
import utils.HttpUtil;
import utils.PassUtil;

import java.io.IOException;
import java.sql.Connection;
import java.util.Map;

public class UsersPassUpdater {

	private static final Logger log = LogManager.getLogger(UsersPassUpdater.class);

	public static void updateUserPass(HttpServletRequest req, HttpServletResponse resp, Map<String, String> params) throws IOException {

		try (Connection conn = dbAuth.getConnection()) {

			String uuid = AuthUtil.getUserUUIDFromAuthCookie(req, resp, conn);
			if (uuid == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "Not logged in");
				return;
			}

			String requestedUserId = params.get("userId");

			if (requestedUserId != null && !requestedUserId.equals(uuid)) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, "error", "Access denied");
				return;
			}

			JSONObject body = HttpUtil.readBodyJSON(req);
			String old = body.getString("old");
			String neW = body.getString("new");

			if (old == null || old.isBlank() || neW == null || neW.isBlank()) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "Empty or null passwords!");
				return;
			}

			String oldPass = PassUtil.sha256Hash(old);
			String newPass = PassUtil.sha256Hash(neW);
			User user = UsersDAO.getUserByUuid(conn, uuid);

			if (user == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "User not found!");
				return;
			}

			if (!oldPass.equals(user.passwordHash())) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "Invalid Old Password!");
				return;
			}

			if (!UsersDAO.updatePasswordAndAccType(conn, uuid, newPass, user.accType(), System.currentTimeMillis() / 1000L)) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
					"Unknown error occurred!");
				return;
			}

			HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "Password is updated!");
		} catch (Exception e) {
			log.catching(e);
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Internal Server error!");
		}
	}

}
