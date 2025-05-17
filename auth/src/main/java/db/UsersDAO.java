package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsersDAO {

	public static boolean userExists(String email) {
		String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
		try (Connection conn = dbAuth.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, email);
			ResultSet rs = stmt.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean insertUser(String id, String username, String email, String passwordHash, long createdAt) {
		String sql = "INSERT INTO users (uuid, email, password_hash, is_verified, created_at) VALUES (?, ?, ?, ?, ?)";

		try (Connection conn = dbAuth.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, id);
			stmt.setString(2, username);
			stmt.setString(3, email);
			stmt.setString(4, passwordHash);
			stmt.setLong(5, createdAt);

			int rowsInserted = stmt.executeUpdate();
			return rowsInserted > 0;

		} catch (SQLException e) {
			e.printStackTrace(); // log properly in real apps
			return false;
		}
	}

}
