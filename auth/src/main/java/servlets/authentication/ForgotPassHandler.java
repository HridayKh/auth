package servlets.authentication;

import db.UsersDAO;
import db.dbAuth;
import entities.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import utils.AuthUtil;
import utils.HttpUtil;

import java.io.IOException;
import java.sql.Connection;
import java.util.Map;

public class ForgotPassHandler {

	public static void forgotPass(HttpServletRequest req, HttpServletResponse resp, Map<String, String> params) throws IOException {
		JSONObject body = HttpUtil.readBodyJSON(req);
		String email = body.getString("email");

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

			if (!user.isVerified()) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Please verify your email");
				return;
			}

			UsersDAO.updateLastLogin(conn, user.uuid(), System.currentTimeMillis() / 1000L);
			AuthUtil.createAndSetAuthCookie(conn, req, resp, user.uuid());

			HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "Logged In Successfully, Redirecting....");
		} catch (Exception e) {
			e.printStackTrace();
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Internal Server Error");
		}
	}
}
