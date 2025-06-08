package servlets;

import java.io.IOException;
import java.sql.Connection;

import utils.AuthUtil;
import db.EmailDAO;
import db.UsersDAO;
import db.dbAuth;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/v1/verify")
public class Verify extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		String token = req.getParameter("token");
		String redir = req.getParameter("redirect");

		if (token == null || token.isEmpty()) {
			resp.sendRedirect(
					dbAuth.FRONT_HOST + "/register?redirect=" + redir + "&type=error&msg=Missing/Invalid Token");
			return;
		}

		try (Connection conn = dbAuth.getConnection()) {
			conn.setAutoCommit(false);

			String userUuid = EmailDAO.verifyToken(conn, token);
			if (userUuid == null) {
				conn.rollback();
				resp.sendRedirect(dbAuth.FRONT_HOST + "/register?redirect=" + redir
						+ "&type=error&msg=Invalid or Expired email verification token");
				return;
			}

			boolean userVerify = UsersDAO.updateUserVerify(conn, userUuid, System.currentTimeMillis() / 1000L);
			if (!userVerify) {
				conn.rollback();
				resp.sendRedirect(
						dbAuth.FRONT_HOST + "/register?redirect=" + redir + "&type=error&msg=Unable to verify user");
				return;
			}

			boolean expireToken = EmailDAO.expireToken(conn, token);
			if (!expireToken) {
				conn.rollback();
				resp.sendRedirect(
						dbAuth.FRONT_HOST + "/register?redirect=" + redir + "&type=error&msg=Unable to expire token");
				return;
			}

			conn.commit();
			AuthUtil.setAuthCookie(resp, userUuid);
			resp.sendRedirect(redir + "?type=success&msg=Email verified successfully.");
			return;
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect(
					dbAuth.FRONT_HOST + "/register?redirect=" + redir + "&type=error&msg=Unexpected server error");
			return;
		}
	}

}
