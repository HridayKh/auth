package servlets.authentication;

import java.io.IOException;
import java.sql.Connection;

import org.json.JSONObject;

import db.UsersDAO;
import db.dbAuth;
import entities.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.AuthUtil;
import utils.HttpUtil;
import utils.PassUtil;

public class LoginHandler {

	public static void loginUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		JSONObject body = HttpUtil.readBodyJSON(req);
		String email = body.getString("email");
		String pass = body.getString("pass");

		try (Connection conn = dbAuth.getConnection()) {
			User user = UsersDAO.getUserByEmailPass(conn, email.toLowerCase(), PassUtil.sha256Hash(pass));

			if (user == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error",
						"User with this email/password does not exist");
				return;
			}

			if (!user.isVerified()) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Please verify your email");
				return;
			}

			long unixTime = System.currentTimeMillis() / 1000L;
			UsersDAO.updateLastLogin(conn, user.uuid(), unixTime);
			AuthUtil.createAndSetAuthCookie(conn, req, resp, user.uuid());

			HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "Logged In Successfully, Redirecting....");
			return;

		} catch (Exception e) {
			e.printStackTrace();
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Internal Server Error");
			return;
		}
	}
}
