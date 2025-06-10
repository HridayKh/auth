package servlets.registration;

import java.io.IOException;
import java.sql.Connection;
import java.util.UUID;

import org.json.JSONObject;

import db.EmailDAO;
import db.UsersDAO;
import db.dbAuth;
import entities.EmailToken;
import entities.User;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.HttpUtil;
import utils.MailUtil;


public class ReVerifyHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public static void reVerifyUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		JSONObject body = HttpUtil.readBodyJSON(req);
		String email = body.getString("email").toLowerCase();
		String redir = body.getString("redirect");

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

			String verifyLink = dbAuth.BACK_HOST + "/v1/verify?token=" + newToken + "&redirect=" + redir;
			MailUtil.sendMail(email, "Your new Hriday.Tech email verification link",
					MailUtil.templateVerifyMail(verifyLink));

			HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "A new verification email has been sent.");
		} catch (Exception e) {
			e.printStackTrace();
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Server error occurred.");
		}
	}

}
