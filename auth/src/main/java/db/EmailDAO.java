package db;

import entities.EmailToken;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmailDAO {

	private static final Logger log = LogManager.getLogger(EmailDAO.class);
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean insertEmailToken(Connection conn, EmailToken et) {
		String sql = "INSERT INTO email_tokens (token, user_uuid, expires_at) VALUES (?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, et.token());
			stmt.setString(2, et.user_uuid());
			stmt.setLong(3, et.expires_at());
			int rowsInserted = stmt.executeUpdate();
			return rowsInserted > 0;
		} catch (SQLException e) {
			return false;
		}
	}

	public static String userUserUuidFromVerifyToken(Connection conn, String token) {
		String sql = "SELECT user_uuid FROM email_tokens WHERE token = ? AND expires_at > (UNIX_TIMESTAMP()) LIMIT 1";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, token);
			ResultSet rs = stmt.executeQuery();
			return rs.next() ? rs.getString("user_uuid") : null;
		} catch (SQLException e) {
			log.catching(e);
			return null;
		}
	}

	public static boolean expireToken(Connection conn, String token) {
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

	public static boolean deleteEmailTokenByUser(Connection conn, String uuid) {
		String sql = "DELETE FROM email_tokens WHERE user_uuid = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, uuid);
			int affectedRows = stmt.executeUpdate();
			return affectedRows > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
