package servlets;

import java.io.IOException;
import java.sql.Connection;

import org.json.JSONObject;

import db.UsersDAO;
import db.dbAuth;
import entities.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.AuthUtil;
import utils.HttpUtil;
import utils.PassUtil;

@WebServlet("/login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		JSONObject body = HttpUtil.readBodyJSON(req);
		String email = body.getString("email");
		String pass = body.getString("pass");

		try (Connection conn = dbAuth.getConnection()) {
			User user = UsersDAO.getUserByEmail(conn, email, PassUtil.sha256Hash(pass));

			if (user == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error",
						"User with this email/password does not exist");
				return;
			}

			if (!user.is_verified()) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Please verify your email");
				return;
			}

			UsersDAO.updateLastLogin(conn, user.uuid(), System.currentTimeMillis() / 1000L);

			AuthUtil.setAuthCookie(resp, user.uuid());

			HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "Logged In Successfully, redirecting....");
			return;

		} catch (Exception e) {
			e.printStackTrace();
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Internal Server Error");
			return;
		}
	}
}
