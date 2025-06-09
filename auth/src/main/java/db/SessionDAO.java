// db/SessionDAO.java (REVISED)
package db;

import entities.Session; // Import your Session record
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SessionDAO {

	/**
	 * Creates a new session record in the database.
	 *
	 * @param conn             The database connection.
	 * @param userUuid         The UUID of the user associated with this session.
	 * @param userAgent        The user agent string from which the session was
	 *                         created.
	 * @param expiresInSeconds The duration in seconds for which the session should
	 *                         be valid.
	 * @return The newly generated session_id.
	 * @throws SQLException If a database access error occurs.
	 */
	public static String createSession(Connection conn, String userUuid, String userAgent, int expiresInSeconds)
			throws SQLException {
		String sessionId = UUID.randomUUID().toString();
		System.out.println("DAO Session ID: " + sessionId);
		long now = System.currentTimeMillis() / 1000L; // Current time in seconds
		long expiresAt = now + expiresInSeconds;

		// Adjusted SQL to exclude ip_address as per your request
		String sql = "INSERT INTO sessions (session_id, user_uuid, created_at, last_accessed_at, expires_at, user_agent, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, sessionId);
			pstmt.setString(2, userUuid);
			pstmt.setLong(3, now);
			pstmt.setLong(4, now);
			pstmt.setLong(5, expiresAt);
			pstmt.setString(6, userAgent);
			pstmt.setBoolean(7, true); // is_active
			pstmt.executeUpdate();
		}
		return sessionId;
	}

	/**
	 * Retrieves a session by its session_id.
	 *
	 * @param conn      The database connection.
	 * @param sessionId The session_id to retrieve.
	 * @return A Session object if found, or null if not found.
	 * @throws SQLException If a database access error occurs.
	 */
	public static Session getSessionById(Connection conn, String sessionId) throws SQLException {
		// Adjusted SQL to exclude ip_address as per your request
		String sql = "SELECT session_id, user_uuid, created_at, last_accessed_at, expires_at, user_agent, is_active FROM sessions WHERE session_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, sessionId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return new Session(rs.getString("session_id"), rs.getString("user_uuid"), rs.getLong("created_at"),
							rs.getLong("last_accessed_at"), rs.getLong("expires_at"), rs.getString("user_agent"),
							rs.getBoolean("is_active"));
				}
			}
		}
		return null; // Session not found
	}

	/**
	 * Updates the last_accessed_at and expires_at for a given session.
	 *
	 * @param conn              The database connection.
	 * @param sessionId         The ID of the session to update.
	 * @param newLastAccessedAt The new timestamp for last_accessed_at (in seconds).
	 * @param newExpiresAt      The new expiration timestamp for the session (in
	 *                          seconds).
	 * @return True if the update was successful, false otherwise.
	 * @throws SQLException If a database access error occurs.
	 */
	public static boolean updateSessionLastAccessed(Connection conn, String sessionId, long newLastAccessedAt,
			long newExpiresAt) throws SQLException {
		String sql = "UPDATE sessions SET last_accessed_at = ?, expires_at = ? WHERE session_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setLong(1, newLastAccessedAt);
			pstmt.setLong(2, newExpiresAt);
			pstmt.setString(3, sessionId);
			return pstmt.executeUpdate() > 0;
		}
	}

	/**
	 * Invalidates a specific session by setting is_active to FALSE.
	 *
	 * @param conn      The database connection.
	 * @param sessionId The ID of the session to invalidate.
	 * @return True if the session was invalidated, false otherwise.
	 * @throws SQLException If a database access error occurs.
	 */
	public static boolean invalidateSession(Connection conn, String sessionId) throws SQLException {
		String sql = "UPDATE sessions SET is_active = FALSE WHERE session_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, sessionId);
			return pstmt.executeUpdate() > 0;
		}
	}

	/**
	 * Invalidates all sessions for a given user, except the current one. This is
	 * for "Sign out other devices" functionality.
	 *
	 * @param conn             The database connection.
	 * @param userUuid         The UUID of the user whose sessions to invalidate.
	 * @param currentSessionId The ID of the current session to exclude from
	 *                         invalidation.
	 * @return The number of sessions invalidated.
	 * @throws SQLException If a database access error occurs.
	 */
	public static int invalidateAllUserSessionsExceptCurrent(Connection conn, String userUuid, String currentSessionId)
			throws SQLException {
		String sql = "UPDATE sessions SET is_active = FALSE WHERE user_uuid = ? AND session_id != ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, userUuid);
			pstmt.setString(2, currentSessionId);
			return pstmt.executeUpdate();
		}
	}

	/**
	 * Invalidates all sessions for a given user. This is for a complete logout from
	 * all devices.
	 *
	 * @param conn     The database connection.
	 * @param userUuid The UUID of the user whose sessions to invalidate.
	 * @return The number of sessions invalidated.
	 * @throws SQLException If a database access error occurs.
	 */
	public static int invalidateAllUserSessions(Connection conn, String userUuid) throws SQLException {
		String sql = "UPDATE sessions SET is_active = FALSE WHERE user_uuid = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, userUuid);
			return pstmt.executeUpdate();
		}
	}
}