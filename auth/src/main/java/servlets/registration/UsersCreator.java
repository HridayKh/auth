package servlets.registration;

import db.EmailDAO;
import db.UsersDAO;
import db.dbAuth;
import entities.EmailToken;
import entities.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import servlets.ApiConstants;
import utils.HttpUtil;
import utils.MailUtil;
import utils.PassUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class UsersCreator {

	private static final Logger log = LogManager.getLogger(UsersCreator.class);

	public static void createUser(HttpServletRequest req, HttpServletResponse resp, Map<String, String> ignoredParams) {

		JSONObject body = HttpUtil.readBodyJSON(req);

		String email = body.getString("email").toLowerCase();
		String pass = body.getString("pass");
		String FullName = body.getString("fullName");
		String redirectUrl = body.getString("redirect");

		long time = System.currentTimeMillis() / 1000L;
		String user_uuid = UUID.randomUUID().toString();
		String token = UUID.randomUUID().toString();

		try (Connection conn = dbAuth.getConnection()) {

			User oldUser = UsersDAO.getUserByEmail(conn, email.toLowerCase());
			if (oldUser != null && !oldUser.isVerified()) {

				HttpUtil.sendJsonReVerify(resp, HttpServletResponse.SC_CONFLICT, "error", "Unverified User with this email/username already exists");
				return;

			} else if (oldUser != null && oldUser.isVerified()) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_CONFLICT, "error", "Verified User with this email/username already exists");
				return;
			}

			conn.setAutoCommit(false);

			User user = new User.Builder(user_uuid, email, time, time).passwordHash(PassUtil.sha256Hash(pass)).fullName(FullName).accType("password").isVerified(false).build();
			if (!UsersDAO.insertUser(conn, user)) {
				conn.rollback();
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Failed to register user. Try again later");
				return;
			}

			EmailToken emailToken = new EmailToken(token, user_uuid, (System.currentTimeMillis() / 1000L) + 86_400);
			if (!EmailDAO.insertEmailToken(conn, emailToken)) {
				conn.rollback();
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Failed to register user. Try again later");
				return;
			}

			String verifyLink = dbAuth.BACK_HOST + ApiConstants.USERS_VERIFY_EMAIL + "?token=" + token + "&redirect=" + redirectUrl;
			MailUtil.sendMail(email, "Verify your E-Mail for HridayKh.in", MailUtil.templateVerifyMail(verifyLink));

			conn.commit();
			HttpUtil.sendJson(resp, HttpServletResponse.SC_CREATED, "success", "User registered successfully. Please check your email account verification link.");
		} catch (IOException | SQLException e) {
			log.catching(e);
			try {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "An error occurred.");
			} catch (IOException e1) {
				log.catching(e1);
			}
		}
	}

}
