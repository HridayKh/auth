package servlets;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/auth/update_password")
public class Update extends HttpServlet {

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		String jwt = req.getHeader("Authorization").split(" ")[1];
		String oldPass = req.getHeader("Authorization").split(" ")[1];
		String newPass = req.getHeader("Authorization").split(" ")[1];
		// verify jwt existence
		// get user info
	}

}
