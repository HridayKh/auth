package servlets;

import java.io.IOException;
import java.sql.Connection;

import db.EmailDAO;
import db.UsersDAO;
import db.dbAuth;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/verify")
public class Verify extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		String token = req.getParameter("token");
		if (token == null || token.isEmpty()) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().write("{\"type\":\"error\",\"message\":\"Missing token parameter.\"}");
			return;
		}

		try (Connection conn = dbAuth.getConnection()) {
			conn.setAutoCommit(false);

			String userUuid = EmailDAO.verifyToken(token, conn);
			if (userUuid == null) {
				conn.rollback();
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				resp.getWriter()
						.write("{\"type\":\"error\",\"message\":\"Invalid or Expired email verification token.\"}");
				return;
			}

			boolean userVerify = UsersDAO.updateUserVerify(conn, userUuid);
			if (!userVerify) {
				conn.rollback();
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				resp.getWriter().write("{\"type\":\"error\",\"message\":\"Unable to verify user.\"}");
				return;
			}

			boolean expireToken = EmailDAO.expireToken(token, conn);
			if (!expireToken) {
				conn.rollback();
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				resp.getWriter().write("{\"type\":\"error\",\"message\":\"Unable to expire token.\"}");
				return;
			}

			conn.commit();
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().write("{\"type\":\"success\",\"message\":\"Email verified successfully.\nPlease Login.\"}");

		} catch (Exception e) {
			e.printStackTrace();
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			resp.getWriter().write("{\"type\":\"error\",\"message\":\"Unexpected server error.\"}");
		}
	}

}
