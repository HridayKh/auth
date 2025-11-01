package servlets.registration;

import java.io.IOException;
import java.sql.Connection;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import db.EmailDAO;
import db.UsersDAO;
import db.dbAuth;
import entities.EmailToken;
import entities.User;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import servlets.ApiConstants;
import utils.HttpUtil;
import utils.MailUtil;

public class UsersResendVerifyEmail extends HttpServlet {

	private static final Logger log = LogManager.getLogger(UsersResendVerifyEmail.class);
	public static void resendVerifyEmail(HttpServletRequest req, HttpServletResponse resp, Map<String, String> ignoredParams) throws IOException {
		JSONObject body = HttpUtil.readBodyJSON(req);
		String email = body.getString("email").toLowerCase();
		String redirectUrl = body.getString("redirect");

		try (Connection conn = dbAuth.getConnection()) {
			User user = UsersDAO.getUserByEmail(conn, email);
			if (user == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "error", "No such user found.");
				return;
			}

			if (user.isVerified()) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Email is already verified.");
				return;
			}

			String newToken = UUID.randomUUID().toString();

			if (!EmailDAO.deleteEmailTokenByUser(conn, user.uuid())) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
						"Unable to invalidate old token.");
				return;
			}
			if (!EmailDAO.insertEmailToken(conn,
					new EmailToken(newToken, user.uuid(), (System.currentTimeMillis() / 1000L) + 86_400))) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Token insert failed.");
				return;
			}

			String verifyLink = dbAuth.BACK_HOST + ApiConstants.USERS_VERIFY_EMAIL + "?token=" + newToken + "&redirect="
					+ redirectUrl;
			MailUtil.sendMail(email, "Your new HridayKh.in email verification link",
					MailUtil.templateVerifyMail(verifyLink));

			HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "A new verification email has been sent.");
		} catch (Exception e) {
			log.catching(e);
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Server error occurred.");
		}
	}

}
