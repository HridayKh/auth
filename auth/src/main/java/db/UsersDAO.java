package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.json.JSONObject;

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
		String sql = "INSERT INTO users (uuid, email, password_hash, is_verified, created_at, updated_at, full_name, profile_pic, last_login, metadata, permissions) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, user.uuid());
			stmt.setString(2, user.email());
			stmt.setString(3, user.passwordHash());
			stmt.setBoolean(4, user.isVerified());
			stmt.setLong(5, user.createdAt());
			stmt.setLong(6, user.updatedAt());
			stmt.setString(7, user.fullName());
			stmt.setString(8, user.profilePic());
			if (user.lastLogin() == null)
				stmt.setNull(9, java.sql.Types.BIGINT);
			else
				stmt.setLong(9, user.lastLogin());
			stmt.setString(10, user.metadata().toString());
			stmt.setString(11, user.permissions().toString());
			int rowsInserted = stmt.executeUpdate();
			return rowsInserted > 0;
		} catch (Exception e) {
			System.err.println("insertUser failed for uuid: " + user.uuid());
			e.printStackTrace();
			return false;
		}
	}

	public static boolean updateUserVerify(Connection conn, String userUuid, long timeNow) {
		String sql = "UPDATE users SET is_verified = 1 , last_login = ? WHERE uuid=?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setLong(1, timeNow);
			stmt.setString(2, userUuid);
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
			return rs.next() ? parseUser(rs) : null;
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
			return rs.next() ? parseUser(rs) : null;
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

	private static User parseUser(ResultSet rs) throws Exception {
		String uuid = rs.getString("uuid");
		String email = rs.getString("email");
		String passwordHash = rs.getString("password_hash");
		boolean isVerified = rs.getBoolean("is_verified");
		long createdAt = rs.getLong("created_at");
		long updatedAt = rs.getLong("updated_at");

		Long lastLogin = rs.getObject("last_login") != null ? rs.getLong("last_login") : null;
		String profilePic = rs.getString("profile_pic");
		if (profilePic == null || profilePic.isBlank())
			profilePic = "https://i.pinimg.com/736x/2f/15/f2/2f15f2e8c688b3120d3d26467b06330c.jpg";

		String fullName = rs.getString("full_name");

		String metadataStr = rs.getString("metadata");
		JSONObject metadata = new JSONObject(metadataStr != null && !metadataStr.isBlank() ? metadataStr : "{}");

		String permissionsStr = rs.getString("permissions");
		JSONObject permissions = new JSONObject(
				permissionsStr != null && !permissionsStr.isBlank() ? permissionsStr : "{}");

		return new User.Builder(uuid, email, passwordHash, createdAt, updatedAt).isVerified(isVerified)
				.lastLogin(lastLogin).profilePic(profilePic).fullName(fullName).metadata(metadata)
				.permissions(permissions).build();
	}

}
