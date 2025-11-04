package servlets.usersCreate;

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
import utils.AuthUtil;
import utils.HttpUtil;
import utils.MailUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class UsersVerifier {

	private static final Logger log = LogManager.getLogger(UsersVerifier.class);

	public static void verifyUser(HttpServletRequest req, HttpServletResponse resp, Map<String, String> ignoredParams) throws IOException {
		String token = req.getParameter("token");
		String redirect = req.getParameter("redirect");

		if (token == null || token.isBlank()) {
			resp.sendRedirect(dbAuth.FRONT_HOST + "/register?redirect=" + redirect + "&type=error&msg=Missing/Invalid Token");
			return;
		}

		try (Connection conn = dbAuth.getConnection()) {
			conn.setAutoCommit(false);

			String userUuid = EmailDAO.userUserUuidFromVerifyToken(conn, token);
			if (userUuid == null) {
				conn.rollback();
				resp.sendRedirect(dbAuth.FRONT_HOST + "/register?redirect=" + redirect + "&type=error&msg=Invalid or Expired email verification token");
				return;
			}

			boolean userVerify = UsersDAO.updateUserVerifyStatus(conn, userUuid);
			if (!userVerify) {
				conn.rollback();
				resp.sendRedirect(dbAuth.FRONT_HOST + "/register?redirect=" + redirect + "&type=error&msg=Unable to verify user");
				return;
			}

			boolean expireToken = EmailDAO.expireToken(conn, token);
			if (!expireToken) {
				conn.rollback();
				resp.sendRedirect(dbAuth.FRONT_HOST + "/register?redirect=" + redirect + "&type=error&msg=Unable to expire token");
				return;
			}

			AuthUtil.createAndSetAuthCookie(conn, req, resp, userUuid);

			conn.commit();
			resp.sendRedirect(redirect + "?type=success&msg=Email verified successfully.");
		} catch (SQLException e) {
			log.catching(e);
			resp.sendRedirect(dbAuth.FRONT_HOST + "/register?redirect=" + redirect + "&type=error&msg=Unexpected server error");
		}
	}

	public static void resendVerifyEmail(HttpServletRequest req, HttpServletResponse resp, Map<String, String> ignoredParams) throws IOException {
		JSONObject body = HttpUtil.readBodyJSON(req);
		String email = body.getString("email").toLowerCase();
		String redirectUrl = body.getString("redirect");

		if (email == null || email.isBlank()) {
			HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Invalid Email Provided.");
			return;
		}

		try (Connection conn = dbAuth.getConnection()) {
			User user = UsersDAO.getUserByEmail(conn, email);
			if (user == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "error", "No such user found.");
				return;
			}
			if (user.isVerified()) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "User with given email is already verified.");
				return;
			}

			String newToken = UUID.randomUUID().toString();

			if (!EmailDAO.deleteEmailTokenByUser(conn, user.uuid())) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Unable to invalidate old token.");
				return;
			}
			if (!EmailDAO.insertEmailToken(conn, new EmailToken(newToken, user.uuid(), (System.currentTimeMillis() / 1000L) + 86_400))) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Token insert failed.");
				return;
			}

			String verifyLink = dbAuth.BACK_HOST + ApiConstants.USERS_VERIFY_EMAIL + "?token=" + newToken + "&redirect=" + redirectUrl;
			MailUtil.sendMail(email, "Your new HridayKh.in email verification link", MailUtil.templateVerifyMail(verifyLink));
			HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "A new verification email has been sent.");
		} catch (Exception e) {
			log.catching(e);
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Server error occurred.");
		}
	}

}