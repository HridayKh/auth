package servlets.registration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant; // Added for precise timestamp handling if needed in AuthUtil
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.AuthUtil;
import db.EmailDAO;
import db.UsersDAO;
import db.dbAuth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Assuming SessionDAO is used internally by AuthUtil for session management
// import dao.SessionDAO; // Not directly used here, but essential for AuthUtil

public class UsersVerifyEmail {

	private static final Logger log = LogManager.getLogger(UsersVerifyEmail.class);
	/**
	 * Handles the email verification process for a user. It verifies the provided
	 * token, updates the user's verification status, expires the token, and creates
	 * a new user session.
	 *
	 * @param req  The HttpServletRequest containing the token and redirect
	 *             parameters.
	 * @param resp The HttpServletResponse to send redirects.
	 * @throws IOException      If an input or output error occurs.
	 */
	public static void verifyUser(HttpServletRequest req, HttpServletResponse resp, Map<String, String> ignoredParams)
			throws IOException {
		String token = req.getParameter("token");
		String redir = req.getParameter("redirect");

		// Input validation for token
		if (token == null || token.isEmpty()) {
			resp.sendRedirect(
					dbAuth.FRONT_HOST + "/register?redirect=" + redir + "&type=error&msg=Missing/Invalid Token");
			return;
		}

		// Establish database connection and start a transaction
		try (Connection conn = dbAuth.getConnection()) {
			conn.setAutoCommit(false); // Begin transaction

			// 1. Verify the token and get the associated user UUID
			String userUuid = EmailDAO.verifyToken(conn, token);
			if (userUuid == null) {
				conn.rollback(); // Rollback transaction on failure
				// Log for debugging (remove in production or use a logging framework)
				resp.sendRedirect(dbAuth.FRONT_HOST + "/register?redirect=" + redir
						+ "&type=error&msg=Invalid or Expired email verification token");
				return;
			}

			// 2. Update user's verification status and last login timestamp
			// Using System.currentTimeMillis() / 1000L for epoch seconds
			boolean userVerify = UsersDAO.updateUserVerify(conn, userUuid, Instant.now().getEpochSecond());
			if (!userVerify) {
				conn.rollback();
				resp.sendRedirect(
						dbAuth.FRONT_HOST + "/register?redirect=" + redir + "&type=error&msg=Unable to verify user");
				return;
			}

			// 3. Expire the used email verification token
			boolean expireToken = EmailDAO.expireToken(conn, token);
			if (!expireToken) {
				conn.rollback();
				resp.sendRedirect(
						dbAuth.FRONT_HOST + "/register?redirect=" + redir + "&type=error&msg=Unable to expire token");
				return;
			}

			// 4. Create a new user session and set the authentication cookie.
			// This method in AuthUtil should now handle:
			// - Generating a new session_id (UUID)
			// - Creating a Session object using the SessionDAO
			// - Storing the Session object in the 'sessions' table
			// - Setting an HTTP-only cookie with the session_id
			AuthUtil.createAndSetAuthCookie(conn, req, resp, userUuid);

			// 5. Commit the transaction if all database operations are successful
			conn.commit();

			// Redirect to the success page
			resp.sendRedirect(redir + "?type=success&msg=Email verified successfully.");
		} catch (SQLException e) {
			log.catching(e);
			resp.sendRedirect(
					dbAuth.FRONT_HOST + "/register?redirect=" + redir + "&type=error&msg=Unexpected server error");
		}
	}
}