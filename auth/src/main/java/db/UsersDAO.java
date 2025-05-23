package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import entities.User;

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

	public static boolean insertUser(User user) {
		String sql = "INSERT INTO users (uuid, email, password_hash, is_verified, created_at, updated_at, last_login) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = dbAuth.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, user.uuid());
			stmt.setString(2, user.email());
			stmt.setString(3, user.password_hash());
			stmt.setBoolean(4, user.is_verified());
			stmt.setLong(5, user.created_at());
			stmt.setLong(6, user.updated_at());
			stmt.setLong(7, user.last_login());
			int rowsInserted = stmt.executeUpdate();
			return rowsInserted > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

}
