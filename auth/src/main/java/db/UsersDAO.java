package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import entities.User;

public class UsersDAO {

	public static boolean userExists(Connection conn, String email) {
		String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, email);
			ResultSet rs = stmt.executeQuery();
			return rs.next();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean insertUser(Connection conn, User user) {
		String sql = "INSERT INTO users (uuid, email, password_hash, is_verified, created_at, updated_at, last_login) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, user.uuid());
			stmt.setString(2, user.email());
			stmt.setString(3, user.password_hash());
			stmt.setBoolean(4, user.is_verified());
			stmt.setLong(5, user.created_at());
			stmt.setLong(6, user.updated_at());
			stmt.setLong(7, user.last_login());
			int rowsInserted = stmt.executeUpdate();
			return rowsInserted > 0;
		} catch (Exception e) {
			System.err.println("insertUser failed for uuid: " + user.uuid());
			e.printStackTrace();
			return false;
		}
	}

	public static boolean updateUserVerify(Connection conn, String userUuid) {
		String sql = "UPDATE users SET is_verified = 1 WHERE uuid=?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, userUuid);
			int rowsInserted = stmt.executeUpdate();
			return rowsInserted > 0;
		} catch (Exception e) {
			System.err.println("updateUserVerify failed for uuid: " + userUuid);
			e.printStackTrace();
			return false;
		}
	}

	public static User getUserByEmail(Connection conn, String email, String password_hash) {
		String sql = "SELECT * FROM users WHERE email = ? AND password_hash = ? LIMIT 1";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, email);
			stmt.setString(2, password_hash);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				String uuid = rs.getString("uuid");
				String EMAIL = rs.getString("email");
				String passwordHash = rs.getString("password_hash");
				boolean isVerified = rs.getBoolean("is_verified");
				long createdAt = rs.getLong("created_at");
				long updatedAt = rs.getLong("updated_at");
				long lastLogin = rs.getLong("last_login");

				User user = new User(uuid, EMAIL, passwordHash, isVerified, createdAt, updatedAt, lastLogin);
				return user;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static User getUserByUUID(Connection conn, String uuid) {
		String sql = "SELECT * FROM users WHERE uuid = ? LIMIT 1";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, uuid);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				String Uuid = rs.getString("uuid");
				String email = rs.getString("email");
				String passwordHash = rs.getString("password_hash");
				boolean isVerified = rs.getBoolean("is_verified");
				long createdAt = rs.getLong("created_at");
				long updatedAt = rs.getLong("updated_at");
				long lastLogin = rs.getLong("last_login");

				User user = new User(Uuid, email, passwordHash, isVerified, createdAt, updatedAt, lastLogin);
				return user;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean updateUserPassword(Connection conn, String userUuid, String passHash, long timeNow) {
		String sql = "UPDATE users SET password_hash = ? , updated_at = ? WHERE uuid = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, passHash);
			stmt.setLong(2, timeNow);
			stmt.setString(3, userUuid);
			int rowsInserted = stmt.executeUpdate();
			return rowsInserted > 0;
		} catch (Exception e) {
			System.err.println("updateUserPassword failed for uuid: " + userUuid);
			e.printStackTrace();
			return false;
		}
	}

	public static boolean updateLastLogin(Connection conn, String uuid, long timeNow) {
		String sql = "UPDATE users SET last_login = ? WHERE uuid = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setLong(1, timeNow);
			stmt.setString(2, uuid);
			int rowsInserted = stmt.executeUpdate();
			return rowsInserted > 0;
		} catch (Exception e) {
			System.err.println("updateLastLogin failed for uuid: " + uuid);
			e.printStackTrace();
			return false;
		}
	}

}
