package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.util.UUID;

import auth.Mail;
import auth.PassUtil;
import db.EmailDAO;
import db.UsersDAO;
import db.dbAuth;
import entities.EmailToken;
import entities.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		resp.setContentType("application/json");
		String email = req.getParameter("email");
		String pass = req.getParameter("pass");
		long time = System.currentTimeMillis() / 100L;
		String user_uuid = UUID.randomUUID().toString();
		String token = UUID.randomUUID().toString();

		try (Connection conn = dbAuth.getConnection()) {
			if (UsersDAO.userExists(email, conn)) {
				resp.setStatus(HttpServletResponse.SC_CONFLICT);
				resp.setContentType("application/json");
				resp.getWriter()
						.write("{\"type\":\"error\",\"message\":\"User with this email/username already exists\"}");
				return;
			}

			conn.setAutoCommit(false);

			boolean userInserted = UsersDAO.insertUser(conn,
					new User(user_uuid, email, PassUtil.sha256Hash(pass), false, time, time, time));
			if (!userInserted) {
        conn.rollback();
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				resp.getWriter()
						.write("{\"type\":\"error\",\"message\":\"Failed to register user. Try again later.\"}");
				return;
			}

			boolean tokenInserted = EmailDAO.insertEmailToken(
					new EmailToken(token, user_uuid, (System.currentTimeMillis() + 24 * 60 * 60 * 1000)), conn);
			if (!tokenInserted) {
				conn.rollback();
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				resp.getWriter()
						.write("{\"type\":\"error\",\"message\":\"Failed to register user. Try again later.\"}");
				return;
			}

			conn.commit();

			String verificationLink;
			if (req.getServerName().replace("localhost", "").equals(req.getServerName())) {
				verificationLink = "https://auth.hriday.tech/verify?token=" + token;
			} else {
				verificationLink = "http://localhost:8080/auth/verify?token=" + token;
			}
			String htmlBody = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
					+ "    <title>Please Verify your E-Mail for Hriday.Tech</title><style>"
					+ "      body { font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px; }"
					+ "      .container { max-width: 600px; margin: auto; background: #fff; border-radius: 8px; padding: 20px; box-shadow: 0 0 10px rgba(0,0,0,0.05); }"
					+ "      h2 { color: #333; }      p { color: #555; }"
					+ "      .btn { display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px; }"
					+ "    </style>  </head>  <body>    <div class=\"container\">"
					+ "      <h2>Welcome to Hriday.Tech!</h2>"
					+ "      <p>Thank you for signing up. Please confirm your email address to activate your account.</p>"
					+ "      <p><a class=\"btn\" href=\"" + verificationLink + "\">Verify Email</a></p>"
					+ "      <p>If you didn't sign up, you can safely ignore this email.</p>"
					+ "      <p style=\"font-size: 12px; color: #999;\">This link will expire in 24 hours.</p>"
					+ "    </div>  </body></html>";

			System.out.println(Mail.sendMail(email, "Verify your E-Mail for Hriday.Tech", htmlBody));

			resp.setStatus(HttpServletResponse.SC_CREATED);
			resp.setContentType("application/json");
			resp.getWriter().write(
					"{\"type\":\"success\",\"message\":\"User registered successfully. Please check your email to verify your account.\"}");

		} catch (Exception e) {
			e.printStackTrace();
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			resp.setContentType("application/json");
			resp.getWriter().write("{\"type\":\"error\",\"message\":\"An error occured.\"}");

		}
	}

}
