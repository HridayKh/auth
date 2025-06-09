package servlets;

import java.io.IOException;
import java.sql.Connection;

import org.json.JSONObject;

import utils.AuthUtil;
import utils.HttpUtil;
import utils.PassUtil;
import db.UsersDAO;
import db.dbAuth;
import entities.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class UpdatePassHandler {

	public static void updateUser(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

		try (Connection conn = dbAuth.getConnection()) {

			String uuid = AuthUtil.getUserUUIDFromAuthCookie(req, resp, conn);

			JSONObject body = HttpUtil.readBodyJSON(req);
			String old = body.getString("old");
			String neW = body.getString("new");

			if (old == null || old.isBlank() || old.isEmpty() || neW == null || neW.isEmpty() || neW.isBlank()) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "Empty or null passwords!");
				return;
			}

			String oldPass = PassUtil.sha256Hash(old);
			String newPass = PassUtil.sha256Hash(neW);
			User user = UsersDAO.getUserByUuid(conn, uuid);

			if (!oldPass.equals(user.passwordHash())) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "Invalid Old Password!");
				return;
			}

			if (!UsersDAO.updatePasswordAndAccType(conn, uuid, newPass, user.accType(), System.currentTimeMillis() / 1000L)) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
						"Unknown error occured!");
				return;
			}

			HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "Password is updated!");
			return;
		} catch (Exception e) {
			e.printStackTrace();
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Internal Server error!");
			return;
		}
	}

}
