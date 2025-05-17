package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import auth.PassUtil;
import db.UsersDAO;
import entities.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/auth/register")
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		String email = req.getParameter("email");
		String pass = req.getParameter("pass");
		
		if (UsersDAO.userExists(email)) {
			resp.setStatus(HttpServletResponse.SC_CONFLICT);
			resp.setContentType("application/json");
			resp.getWriter().write("{\"error\":\"User with this email/username already exists\"}");
			return;
		}

		long time = System.currentTimeMillis() / 100L;
		User user = new User(UUID.randomUUID().toString(), email, PassUtil.sha256Hash(pass), false, time, time, time);

		// make email veri token and save it
		// send veri link
	}

}
