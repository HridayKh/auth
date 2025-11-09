package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.util.Map;

import db.UsersDAO;
import db.dbAuth;
import entities.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.AuthUtil;
import utils.HttpUtil;

public class UnlinkGoggle {
	public static void unlinkGoogleAccount(HttpServletRequest req, HttpServletResponse resp,
			Map<String, String> pathParams) throws IOException {
		Connection conn = null;
		try {
			conn = dbAuth.getConnection();
			String uuid = AuthUtil.getUserUUIDFromAuthCookie(req, resp, conn);
			if (uuid == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "Not logged in");
				return;
			}

			User user = UsersDAO.getUserByUuid(conn, uuid);
			if (user == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "User not found");
				return;
			}

			boolean googleUnlinked = UsersDAO.unlinkGoogle(conn, uuid);
			if (googleUnlinked) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success",
						"Google account unlinked");
			} else {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
						"Failed to unlink Google account");
			}

		} catch (Exception e) {
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
					"Database connection error");
			return;
		}
	}
}