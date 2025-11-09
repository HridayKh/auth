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

	public static void updateUserPass(HttpServletRequest req, HttpServletResponse resp,
			Map<String, String> ignoredParams) throws IOException {

		try (Connection conn = dbAuth.getConnection()) {

			String uuid = AuthUtil.getUserUUIDFromAuthCookie(req, resp, conn);
			if (uuid == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "Not logged in");
				return;
			}

			User user = UsersDAO.getUserByUuid(conn, uuid);
			if (user == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error",
						"User not found!");
				return;
			}

			JSONObject body = HttpUtil.readBodyJSON(req);
			String neW = body.getString("new");
			if (neW == null || neW.isBlank()) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error",
						"Empty or null new password!");
				return;
			}
			String newPass = PassUtil.sha256Hash(neW);

			if (!user.accType().equals("google")) {
				String old = body.getString("old");
				String oldPass = PassUtil.sha256Hash(old);
				if (!oldPass.equals(user.passwordHash())) {
					HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error",
							"Invalid Old Password!");
					return;
				}
			}

			if (!UsersDAO.updatePasswordAndAccType(conn, uuid, newPass, user.accType(),
					System.currentTimeMillis() / 1000L)) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
						"Unknown error occurred!");
				return;
			}

			HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "Password is updated!");
		} catch (Exception e) {
			log.catching(e);
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
					"Internal Server error!");
		}
	}

}
