package db;

import entities.Session; // Import your Session record
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types; // Needed for setting NULL values

public class SessionDAOOLD {

	/**
	 * Inserts a new session into the database.
	 *
	 * @param conn    The database connection.
	 * @param session The Session object to insert.
	 * @return true if the session was successfully inserted, false otherwise.
	 */
	public static boolean insertSession(Connection conn, Session session) {
		String sql = "INSERT INTO sessions "
				+ "(session_id, user_uuid, created_at, last_accessed_at, expires_at, user_agent, is_active) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, session.sessionId());
			stmt.setString(2, session.userUuid());
			stmt.setLong(3, session.createdAt());
			stmt.setLong(4, session.lastAccessedAt());
			stmt.setLong(5, session.expiresAt());
			if (session.userAgent() == null) {
				stmt.setNull(6, Types.VARCHAR);
			} else {
				stmt.setString(6, session.userAgent());
			}
			stmt.setBoolean(7, session.isActive());
			int rowsInserted = stmt.executeUpdate();
			return rowsInserted > 0;
		} catch (SQLException e) {
			System.err.println("Error inserting session with ID: " + session.sessionId());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Retrieves a session by its session ID.
	 *
	 * @param conn      The database connection.
	 * @param sessionId The ID of the session to retrieve.
	 * @return The Session object if found, or null if not found or an error occurs.
	 */
	public static Session getSessionById(Connection conn, String sessionId) {
		String sql = "SELECT session_id, user_uuid, created_at, last_accessed_at, expires_at, user_agent, is_active "
				+ "FROM sessions WHERE session_id = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, sessionId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					String sId = rs.getString("session_id");
					String uUuid = rs.getString("user_uuid");
					long cAt = rs.getLong("created_at");
					long laAt = rs.getLong("last_accessed_at");
					long eAt = rs.getLong("expires_at");

					// user_agent is nullable
					String uAgent = rs.getString("user_agent"); // getString returns null if DB value is NULL

					boolean isActive = rs.getBoolean("is_active");

					return new Session(sId, uUuid, cAt, laAt, eAt, uAgent, isActive);
				}
			}
		} catch (SQLException e) {
			System.err.println("Error retrieving session with ID: " + sessionId);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Updates the last_accessed_at timestamp for a given session.
	 *
	 * @param conn              The database connection.
	 * @param sessionId         The ID of the session to update.
	 * @param newLastAccessedAt The new timestamp for last_accessed_at.
	 * @return true if the session was successfully updated, false otherwise.
	 */
	public static boolean updateLastAccessedAt(Connection conn, String sessionId, long newLastAccessedAt) {
		String sql = "UPDATE sessions SET last_accessed_at = ? WHERE session_id = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setLong(1, newLastAccessedAt);
			stmt.setString(2, sessionId);
			int rowsUpdated = stmt.executeUpdate();
			return rowsUpdated > 0;
		} catch (SQLException e) {
			System.err.println("Error updating last_accessed_at for session ID: " + sessionId);
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Invalidates a session by setting its is_active flag to FALSE.
	 *
	 * @param conn      The database connection.
	 * @param sessionId The ID of the session to invalidate.
	 * @return true if the session was successfully invalidated, false otherwise.
	 */
	public static boolean invalidateSession(Connection conn, String sessionId) {
		String sql = "UPDATE sessions SET is_active = FALSE WHERE session_id = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, sessionId);
			int rowsUpdated = stmt.executeUpdate();
			return rowsUpdated > 0;
		} catch (SQLException e) {
			System.err.println("Error invalidating session with ID: " + sessionId);
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Deletes a session from the database.
	 *
	 * @param conn      The database connection.
	 * @param sessionId The ID of the session to delete.
	 * @return true if the session was successfully deleted, false otherwise.
	 */
	public static boolean deleteSession(Connection conn, String sessionId) {
		String sql = "DELETE FROM sessions WHERE session_id = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, sessionId);
			int rowsDeleted = stmt.executeUpdate();
			return rowsDeleted > 0;
		} catch (SQLException e) {
			System.err.println("Error deleting session with ID: " + sessionId);
			e.printStackTrace();
			return false;
		}
	}
}