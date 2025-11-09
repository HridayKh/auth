package db;

import entities.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
		long now = System.currentTimeMillis() / 1000L; // Current time in seconds
		long expiresAt = now + expiresInSeconds;

		// Adjusted SQL to exclude ip_address as per your request
		String sql = "INSERT INTO sessions (session_id, user_uuid, created_at, last_accessed_at, expires_at, user_agent) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, sessionId);
			stmt.setString(2, userUuid);
			stmt.setLong(3, now);
			stmt.setLong(4, now);
			stmt.setLong(5, expiresAt);
			stmt.setString(6, userAgent);
			stmt.executeUpdate();
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
		String sql = "SELECT * FROM sessions WHERE session_id = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, sessionId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return new Session(rs.getString("session_id"), rs.getString("user_uuid"),
							rs.getLong("created_at"),
							rs.getLong("last_accessed_at"), rs.getLong("expires_at"),
							rs.getString("user_agent"));
				}
			}
		}
		return null; // Session not found
	}

	/**
	 * Retrieves all sessions of a user.
	 *
	 * @param conn The database connection.
	 * @param uuid The uuid of the user whose sessions are retrieved.
	 * @return A Session object if found, or null if not found.
	 * @throws SQLException If a database access error occurs.
	 */
	public static Session[] getAllSessionsOfUser(Connection conn, String uuid) throws SQLException {
		String sql = "SELECT * FROM sessions WHERE user_uuid = ";
		List<Session> sessions = new ArrayList<>();
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, uuid);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				sessions.add(new Session(rs.getString("session_id"), rs.getString("user_uuid"),
						rs.getLong("created_at"), rs.getLong("last_accessed_at"),
						rs.getLong("expires_at"),
						rs.getString("user_agent")));
			}
			return sessions.toArray(new Session[0]);
		}
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
	public static void updateSessionLastAccessed(Connection conn, String sessionId, long newLastAccessedAt,
			long newExpiresAt) throws SQLException {
		String sql = "UPDATE sessions SET last_accessed_at = ?, expires_at = ? WHERE session_id = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setLong(1, newLastAccessedAt);
			stmt.setLong(2, newExpiresAt);
			stmt.setString(3, sessionId);
			stmt.executeUpdate();
		}
	}

	/**
	 * Invalidates a specific session by deleting it.
	 *
	 * @param conn      The database connection.
	 * @param sessionId The ID of the session to invalidate.
	 * @param uuid      The user uuid
	 * @return True if the session was invalidated, false otherwise.
	 * @throws SQLException If a database access error occurs.
	 */
	public static boolean invalidateSession(Connection conn, String sessionId) throws SQLException {
		String sql = "delete from sessions WHERE session_id = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, sessionId);
			return stmt.executeUpdate() > 0;
		}
	}

}