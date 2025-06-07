package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.util.UUID;

import org.json.JSONObject;

import auth.Mail;
import db.EmailDAO;
import db.UsersDAO;
import db.dbAuth;
import entities.EmailTemplates;
import entities.EmailToken;
import entities.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.HttpUtil;
import utils.PassUtil;

@WebServlet("/register")
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

		JSONObject body = HttpUtil.readBodyJSON(req);
		String email = body.getString("email");
		String pass = body.getString("pass");

		long time = System.currentTimeMillis() / 1000L;
		String user_uuid = UUID.randomUUID().toString();
		String token = UUID.randomUUID().toString();

		try (Connection conn = dbAuth.getConnection()) {
			if (UsersDAO.userExists(conn, email)) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_CONFLICT, "error",
						"User with this email/username already exists");
				return;
			}
			conn.setAutoCommit(false);

			User user = new User(user_uuid, email, PassUtil.sha256Hash(pass), false, time, time, time);
			if (!UsersDAO.insertUser(conn, user)) {
				conn.rollback();
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
						"Failed to register user. Try again later");
				return;
			}

			// Token valid for 24h = 72,600,000 ms
			EmailToken emailToken = new EmailToken(token, user_uuid, System.currentTimeMillis() + 72_600_000);
			if (!EmailDAO.insertEmailToken(conn, emailToken)) {
				conn.rollback();
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
						"Failed to register user. Try again later");
				return;
			}

			String verifyLink = System.getenv("BACK_HOST") + "/verify?token=" + token;
			Mail.sendMail(email, "Verify your E-Mail for Hriday.Tech", EmailTemplates.verifyMail(verifyLink));

			conn.commit();
			HttpUtil.sendJson(resp, HttpServletResponse.SC_CREATED, "success",
					"User registered successfully. Please check your email account verification link.");
		} catch (Exception e) {
			e.printStackTrace();
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "An error occured.");
		}
	}

}
