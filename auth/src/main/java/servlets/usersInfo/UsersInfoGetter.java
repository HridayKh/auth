package servlets.usersInfo;

import db.UsersDAO;
import db.dbAuth;
import entities.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.AuthUtil;
import utils.HttpUtil;

import java.io.IOException;
import java.sql.Connection;
import java.util.Map;

public class UsersInfoGetter {

	private static final Logger log = LogManager.getLogger(UsersInfoGetter.class);

	public static void getUser(HttpServletRequest req, HttpServletResponse resp, Map<String, String> ignoredParams) throws IOException {
		try (Connection conn = dbAuth.getConnection()) {
			String uuid = AuthUtil.getUserUUIDFromAuthCookie(req, resp, conn);
			if (uuid == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "Not Logged IN");
				return;
			}
			User user = UsersDAO.getUserByUuid(conn, uuid);
			if (user == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "error", "User not found");
				return;
			}
			HttpUtil.sendUser(resp, user);
			HttpUtil.createAndSetUserCookie(resp, user);
		} catch (Exception e) {
			log.catching(e);
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Internal Server Error");
		}
	}

}
