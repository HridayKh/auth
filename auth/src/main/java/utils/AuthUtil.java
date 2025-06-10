// utils/AuthUtil.java (UPDATED)
package utils;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import db.SessionDAO; // Import the updated SessionDAO
import db.dbAuth;
import entities.Session; // Import your Session record
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthUtil {

	// Define your session expiry time in seconds (e.g., 7 days)
	// Make this configurable if possible, maybe from dbAuth or a properties file
	public static final int SESSION_EXPIRY_SECONDS = (int) TimeUnit.DAYS.toSeconds(7);
	public static final String COOKIE_DOMAIN = "hriday.tech";
	private static final String AUTH_COOKIE_NAME = "hriday_tech_auth_token";

	/**
	 * Creates a new server-side session, stores it in the database, and sets the
	 * auth cookie. This method should be called after a successful login or email
	 * verification.
	 *
	 * @param resp     The HttpServletResponse to add the cookie to.
	 * @param conn     The database connection.
	 * @param userUuid The UUID of the user logging in.
	 * @param req      The HttpServletRequest to get User-Agent.
	 * @throws SQLException
	 */
	public static void createAndSetAuthCookie(Connection conn, HttpServletRequest req, HttpServletResponse resp,
			String userUuid) throws SQLException {
		String userAgent = req.getHeader("User-Agent");

		// 1. Create a new session record in the database
		String sessionId = SessionDAO.createSession(conn, userUuid, userAgent, SESSION_EXPIRY_SECONDS);
		// 2. Sign the sessionId. Assuming PassUtil.signUUID can securely sign any
		// string.
		String signedSessionId = PassUtil.signString(sessionId);

		// 3. Combine sessionId and signature for the cookie value
		String jwt = sessionId + ":|:" + signedSessionId;
		String encodedJwt = Base64.getEncoder().encodeToString(jwt.getBytes(StandardCharsets.UTF_8));

		// 4. Set the HttpOnly auth cookie
		Cookie authCookie = new Cookie(AUTH_COOKIE_NAME, encodedJwt);

		if ("yes".equals(dbAuth.PROD)) {
			authCookie.setSecure(true);
			authCookie.setDomain(COOKIE_DOMAIN);
		}
		authCookie.setHttpOnly(true);
		authCookie.setMaxAge(SESSION_EXPIRY_SECONDS);
		authCookie.setPath("/");
		resp.addCookie(authCookie);
	}

	/**
	 * Validates the auth cookie, retrieves the session from the database, updates
	 * its last access time, and returns the associated user UUID. This method
	 * should be called by any secured endpoint or filter.
	 *
	 * @param req  The HttpServletRequest to get the cookie from.
	 * @param resp The HttpServletResponse to clear the cookie if invalid.
	 * @param conn The database connection.
	 * @return The user UUID if the session is valid and active, null otherwise.
	 * @throws SQLException If a database access error occurs during session
	 *                      lookup/update.
	 */
	public static String getUserUUIDFromAuthCookie(HttpServletRequest req, HttpServletResponse resp, Connection conn)
			throws SQLException {
		Cookie[] cookies = req.getCookies();
		if (cookies == null) {
			return null; // No cookies present, user not logged in
		}

		String jwtEnc = null;
		for (Cookie cookie : cookies) {
			if (AUTH_COOKIE_NAME.equals(cookie.getName())) {
				jwtEnc = cookie.getValue();
				break;
			}
		}
		if (jwtEnc == null) {
			return null; // Auth cookie not found, user not logged in
		}

		String sessionId = null;
		try {
			byte[] decodedBytes = Base64.getDecoder().decode(jwtEnc);
			String jwt = new String(decodedBytes, StandardCharsets.UTF_8);
			String[] parts = jwt.split(":\\|:");
			if (parts.length != 2) {
				clearAuthCookie(resp); // Clear malformed cookie
				return null;
			}

			sessionId = parts[0];
			String receivedSignature = parts[1];

			// 1. Verify the signature of the sessionId
			if (!PassUtil.signString(sessionId).equals(receivedSignature)) {
				clearAuthCookie(resp); // Clear cookie with invalid signature
				return null;
			}

			// 2. Look up the session in the database
			Session session = SessionDAO.getSessionById(conn, sessionId);

			if (session != null) {
				long now = System.currentTimeMillis() / 1000L;

				// 3. Check if session is active and not expired
				if (session.isActive() && session.expiresAt() > now) {
					// 4. Update last_accessed_at and re-extend expiration (rolling session)
					long newExpiresAt = now + SESSION_EXPIRY_SECONDS;
					SessionDAO.updateSessionLastAccessed(conn, sessionId, now, newExpiresAt);
					return session.userUuid(); // Return the user UUID associated with this valid session
				} else {
					clearAuthCookie(resp); // Clear the client's cookie for the invalid session
					return null;
				}
			} else {
				// Session ID not found in DB (e.g., invalidated by "sign out other devices")
				clearAuthCookie(resp); // Clear the client's cookie
				return null;
			}
		} catch (IllegalArgumentException e) {
			clearAuthCookie(resp);
			return null;
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * Clears the authentication cookie from the client. This should be called on
	 * logout or when an invalid/expired session is detected.
	 *
	 * @param resp The HttpServletResponse.
	 */
	public static void clearAuthCookie(HttpServletResponse resp) {
		Cookie authCookie = new Cookie(AUTH_COOKIE_NAME, "");
		authCookie.setMaxAge(0); // Set max age to 0 to delete the cookie
		authCookie.setPath("/");
		authCookie.setHttpOnly(true);
		if ("yes".equals(dbAuth.PROD)) {
			authCookie.setDomain(COOKIE_DOMAIN);
			authCookie.setSecure(true);
		}
		resp.addCookie(authCookie);
	}

	/**
	 * Verifies an 'Authorization: Basic' HTTP header against a predefined static
	 * password. This method is intended for authenticating requests from trusted
	 * "other apps" or internal services that share a static secret. It extracts the
	 * Base64-encoded credentials, decodes them, and checks if the extracted
	 * password matches the `dbAuth.PASS` variable.
	 *
	 * Note: Storing sensitive passwords directly in code or simple configuration
	 * files (like `dbAuth.PASS`) is generally not recommended for high-security
	 * applications. Consider environment variables or a secrets management system
	 * for production environments. This method assumes `dbAuth.PASS` holds the
	 * static password to be checked.
	 *
	 * @param req The {@link HttpServletRequest} containing the HTTP headers.
	 * @return {@code true} if the Basic Auth header is present and the password
	 *         matches {@code dbAuth.PASS}, {@code false} otherwise.
	 */
	public static boolean verifyBasicAuthHeaderWithStaticPassword(HttpServletRequest req) {
		// 1. Get the Authorization header from the request
		String authHeader = req.getHeader("auth");

		// 2. Check if the header exists and starts with "Basic "
		if (authHeader == null) {
			return false;
		}

		// 5. Convert decoded bytes to string
		String credentials = authHeader.trim();

		// 6. Compare the extracted password with the static password from dbAuth.PASS
		if (credentials.equals(PassUtil.sha256Hash(dbAuth.DB_PASSWORD))) {
			return true; // Password matched
		} else {
			return false; // Password mismatch
		}
	}
}
