package servlets.userPasswords;

import db.PassDAO;
import db.UsersDAO;
import db.dbAuth;
import entities.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import utils.HttpUtil;
import utils.MailUtil;
import utils.PassUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class UsersPassReset {

	private static final Logger log = LogManager.getLogger(UsersPassReset.class);

	public static void initReset(HttpServletRequest req, HttpServletResponse resp, Map<String, String> ignoredParams) throws IOException {
		JSONObject body = HttpUtil.readBodyJSON(req);
		String email = body.optString("email", "").toLowerCase().strip();
		String redirectUrl = body.optString("redirect", dbAuth.FRONT_HOST + "/profile");

		if (email == null || email.isBlank()) {
			HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Invalid Email Provided.");
			return;
		}

		Connection conn = null;
		try {
			conn = dbAuth.getConnection();
			conn.setAutoCommit(false);

			User user = UsersDAO.getUserByEmail(conn, email);
			if (user == null) {
				conn.rollback();
				HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "error", "user not found.");
				return;
			}

			String token = UUID.randomUUID().toString();
			if (!PassDAO.startPassReset(conn, token, user.uuid(), (System.currentTimeMillis() / 1000L) + 3600)) {
				conn.rollback();
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Unable to start password reset.");
				return;
			}

			String verifyLink = dbAuth.FRONT_HOST + "/password-reset?token=" + token + "&redirect=" + redirectUrl;
			MailUtil.sendMail(email, "Reset your HridayKh.in account password", MailUtil.templatePassReset(verifyLink));
			conn.commit();
			HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "Password reset email sent.");
		} catch (SQLException e) {
			log.catching(e);
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Server error occurred.");
			try {
				if (conn != null) {
					conn.rollback();
				}
			} catch (SQLException ex) {
				log.catching(ex);
			}
		}
	}

	public static void completeReset(HttpServletRequest req, HttpServletResponse resp, Map<String, String> ignoredParams) throws IOException {
		JSONObject body = HttpUtil.readBodyJSON(req);
		String password = body.optString("pass", "").strip();
		String token = body.optString("token", "").strip();

		if (password == null || password.isBlank()) {
			HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Invalid Password Provided.");
			return;
		}
		if (token == null || token.isBlank()) {
			HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Invalid Token Provided.");
			return;
		}

		Connection conn = null;
		try {
			conn = dbAuth.getConnection();
			conn.setAutoCommit(false);

			String userUuid = PassDAO.userUuidFromPassToken(conn, token);
			if (userUuid == null) {
				conn.rollback();
				HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "invalid token.");
				return;
			}

			User user = UsersDAO.getUserByUuid(conn, userUuid);
			if(user == null) {
				conn.rollback();
				HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "error", "user not found.");
				return;
			}

			boolean passUpdated = UsersDAO.updatePasswordAndAccType(conn, user.uuid(), PassUtil.sha256Hash(password), user.accType(), System.currentTimeMillis() / 1000L);
			if (!passUpdated) {
				conn.rollback();
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Unable to update password.");
				return;
			}
			conn.commit();
			HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "Password has been updated, please log in");
		} catch (SQLException e) {
			log.catching(e);
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Server error occurred.");
			try {
				if (conn != null) {
					conn.rollback();
				}
			} catch (SQLException ex) {
				log.catching(ex);
			}
		}
	}

}