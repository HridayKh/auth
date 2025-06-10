package google;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);

		if (session != null && Boolean.TRUE.equals(session.getAttribute("isLoggedIn"))) {
			String userName = (String) session.getAttribute("userName");
			response.getWriter().println("<h1>Welcome, " + userName + "!</h1>");
			response.getWriter().println("<p>This is your protected dashboard.</p>");
			response.getWriter().println("<a href=\"/yourapp/logout\">Logout</a>");
		} else {
			response.sendRedirect("/auth/google.html");
		}
	}
}
