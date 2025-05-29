package db;

import java.sql.Connection;
import java.sql.PreparedStatement;

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

}
