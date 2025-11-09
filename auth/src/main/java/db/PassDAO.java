package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PassDAO {

	public static boolean startPassReset(Connection conn, String token, String user_uuid, long expires_at) {
		String sql = "INSERT INTO password_resets (token, user_uuid, expires_at) VALUES (?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, token);
			stmt.setString(2, user_uuid);
			stmt.setLong(3, expires_at);
			int rowsInserted = stmt.executeUpdate();
			return rowsInserted > 0;
		} catch (SQLException e) {
			return false;
		}
	}

	public static String userUuidFromPassToken(Connection conn, String token) {
		String sql = "SELECT user_uuid FROM password_resets WHERE token = ? AND expires_at > (UNIX_TIMESTAMP()) LIMIT 1";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, token);
			ResultSet rs = stmt.executeQuery();
			return rs.next() ? rs.getString("user_uuid") : null;
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean expirePassReset(Connection conn, String token) {
		String sql = "UPDATE password_resets SET expires_at = (UNIX_TIMESTAMP() * 1000) WHERE token = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, token);
			int affectedRows = stmt.executeUpdate();
			return affectedRows > 0;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean deletePassResetToken(Connection conn, String uuid) {
		String sql = "DELETE FROM password_resets WHERE user_uuid = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, uuid);
			int affectedRows = stmt.executeUpdate();
			return affectedRows > 0;
		} catch (Exception e) {
			return false;
		}
	}

}
