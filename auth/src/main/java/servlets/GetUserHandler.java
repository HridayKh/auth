package servlets;

import java.io.IOException;
import java.sql.Connection;

import utils.AuthUtil;
import utils.HttpUtil;
import db.UsersDAO;
import db.dbAuth;
import entities.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class GetUserHandler {

	public static void getUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String uuid = AuthUtil.getUserUUIDFromCookie(req);

		try (Connection conn = dbAuth.getConnection()) {
			User user = UsersDAO.getUserByUUID(conn, uuid);
			if (user == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "error", "User not found");
				return;
			}

			HttpUtil.sendUser(resp, user);

		} catch (Exception e) {
			e.printStackTrace();
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Internal Server Error");
		}
	}

}
