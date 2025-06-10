package servlets.registration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import org.json.JSONObject;

import db.EmailDAO;
import db.UsersDAO;
import db.dbAuth;
import entities.EmailToken;
import entities.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.MailUtil;
import utils.HttpUtil;
import utils.PassUtil;

public class RegisterHandler {

	public static void registerUser(HttpServletRequest req, HttpServletResponse resp) {

		JSONObject body = HttpUtil.readBodyJSON(req);
		String email = body.getString("email").toLowerCase();
		String pass = body.getString("pass");
		String FullName = body.getString("fullName");
		String redir = body.getString("redirect");

		long time = System.currentTimeMillis() / 1000L;
		String user_uuid = UUID.randomUUID().toString();
		String token = UUID.randomUUID().toString();

		try (Connection conn = dbAuth.getConnection()) {

			User oldUser = UsersDAO.getUserByEmail(conn, email.toLowerCase());
			if (oldUser != null && !oldUser.isVerified()) {

				HttpUtil.sendJsonReVerify(resp, HttpServletResponse.SC_CONFLICT, "error",
						"Unverified User with this email/username already exists");
				return;

			} else if (oldUser != null && oldUser.isVerified()) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_CONFLICT, "error",
						"Verified User with this email/username already exists");
				return;
			}

			conn.setAutoCommit(false);

			User user = new User.Builder(user_uuid, email, time, time).passwordHash(PassUtil.sha256Hash(pass))
					.fullName(FullName).accType("password").isVerified(false).build();
			if (!UsersDAO.insertUser(conn, user)) {
				conn.rollback();
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
						"Failed to register user. Try again later");
				return;
			}

			EmailToken emailToken = new EmailToken(token, user_uuid, (System.currentTimeMillis() / 1000L) + 86_400);
			if (!EmailDAO.insertEmailToken(conn, emailToken)) {
				conn.rollback();
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
						"Failed to register user. Try again later");
				return;
			}

			String verifyLink = dbAuth.BACK_HOST + "/v1/verify?token=" + token + "&redirect=" + redir;
			MailUtil.sendMail(email, "Verify your E-Mail for Hriday.Tech", MailUtil.templateVerifyMail(verifyLink));

			conn.commit();
			HttpUtil.sendJson(resp, HttpServletResponse.SC_CREATED, "success",
					"User registered successfully. Please check your email account verification link.");
		} catch (IOException | SQLException e) {
			e.printStackTrace();
			try {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "An error occured.");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

}
