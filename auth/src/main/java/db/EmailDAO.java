package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import entities.EmailToken;

public class EmailDAO {

	public static boolean insertEmailToken(EmailToken et, Connection conn) {
		String sql = "INSERT INTO email_tokens (token, user_uuid, expires_at) VALUES (?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, et.token());
			stmt.setString(2, et.user_uuid());
			stmt.setLong(3, et.expires_at());
			int rowsInserted = stmt.executeUpdate();
			return rowsInserted > 0;
		} catch (Exception e) {
			System.err.println("insertEmailTOken failed for uuid: " + et.user_uuid());
			e.printStackTrace();
			return false;
		}
	}

	public static String verifyToken(String token, Connection conn) {
		String sql = "SELECT user_uuid FROM email_tokens WHERE token = ? AND expires_at > (UNIX_TIMESTAMP() * 1000) LIMIT 1";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, token);
			ResultSet rs = stmt.executeQuery();
			return rs.next() ? rs.getString("user_uuid") : null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean expireToken(String token, Connection conn) {
		String sql = "UPDATE email_tokens SET expires_at = (UNIX_TIMESTAMP() * 1000) WHERE token = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, token);
			int affectedRows = stmt.executeUpdate();
			return affectedRows > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
