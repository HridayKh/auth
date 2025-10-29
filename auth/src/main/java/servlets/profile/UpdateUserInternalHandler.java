package servlets.profile;

import java.io.IOException;
import java.sql.Connection;
import java.util.Map;

import db.UsersDAO;
import db.dbAuth;
import entities.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.AuthUtil;
import utils.HttpUtil;

public class UpdateUserInternalHandler {

	public static void updateUserAdminProfile(HttpServletRequest req, HttpServletResponse resp, Map<String, String> params)
			throws ServletException, IOException {
		try (Connection conn = dbAuth.getConnection()) {
			String uuid = AuthUtil.getUserUUIDFromAuthCookie(req, resp, conn);
			if (uuid == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "Not Logged In");
				return;
			}
			User user = UsersDAO.getUserByUuid(conn, uuid);
			if (user == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "error", "User not found");
				return;
			}
			HttpUtil.sendUserMetadataPerms(resp, user);
			HttpUtil.createAndSetUserCookie(resp, user);
		} catch (Exception e) {
			e.printStackTrace();
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Internal Server Error");
		}
	}
}