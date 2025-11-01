package servlets.authentication;

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

public class UsersSessionCreate {

	private static final Logger log = LogManager.getLogger(UsersSessionCreate.class);

	public static void createUserSession(HttpServletRequest req, HttpServletResponse resp, Map<String, String> ignoredParams) throws IOException {
		JSONObject body = HttpUtil.readBodyJSON(req);
		String email = body.getString("email");
		String pass = body.getString("pass");

		try (Connection conn = dbAuth.getConnection()) {
			User user = UsersDAO.getUserByEmail(conn, email.toLowerCase());

			if (user == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Invalid email/password");
				return;
			}
			if (user.accType().equals("google")) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Please use google login");
				return;
			}
			if (!PassUtil.sha256Hash(pass).equals(user.passwordHash())) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Invalid email/password");
				return;
			}

			if (!user.isVerified()) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Please verify your email");
				return;
			}

			boolean lastLoginUpdated = UsersDAO.updateLastLogin(conn, user.uuid(), System.currentTimeMillis() / 1000L);

			if (!lastLoginUpdated) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Could not login.");
				return;
			}
			AuthUtil.createAndSetAuthCookie(conn, req, resp, user.uuid());

			HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "Logged In Successfully, Redirecting....");

		} catch (Exception e) {
			log.catching(e);
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Internal Server Error");
		}
	}
}
