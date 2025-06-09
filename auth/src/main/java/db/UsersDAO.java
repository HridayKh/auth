package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import org.json.JSONObject;

import entities.User;

public class UsersDAO {

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

	public static User getUserByEmail(Connection conn, String email) {
		String sql = "SELECT * FROM users WHERE email = ? LIMIT 1";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, email);
			ResultSet rs = stmt.executeQuery();
			return rs.next() ? parseUser(rs) : null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static User getUserByEmailPass(Connection conn, String email, String password_hash) {
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

	public static boolean insertUser(Connection conn, User user) {
		String sql = "INSERT INTO users (" + "uuid, email, password_hash, is_verified, created_at, updated_at, "
				+ "full_name, profile_pic, last_login, metadata, permissions, "
				+ "acc_type, google_id, refresh_token, refresh_token_expires_at" // New columns
				+ ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; // 15 placeholders now

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			// Set values for existing columns
			stmt.setString(1, user.uuid());
			stmt.setString(2, user.email());

			// password_hash is now nullable
			if (user.passwordHash() == null) {
				stmt.setNull(3, Types.VARCHAR); // Use Types.VARCHAR for String types
			} else {
				stmt.setString(3, user.passwordHash());
			}

			stmt.setBoolean(4, user.isVerified());
			stmt.setLong(5, user.createdAt());
			stmt.setLong(6, user.updatedAt());
			stmt.setString(7, user.fullName());
			stmt.setString(8, user.profilePic());

			// last_login is nullable
			if (user.lastLogin() == null) {
				stmt.setNull(9, Types.BIGINT);
			} else {
				stmt.setLong(9, user.lastLogin());
			}

			stmt.setString(10, user.metadata().toString());
			stmt.setString(11, user.permissions().toString());

			// Set values for new columns (12 to 15)
			stmt.setString(12, user.accType()); // acc_type has a default, but we're explicitly setting it

			if (user.googleId() == null) {
				stmt.setNull(13, Types.VARCHAR);
			} else {
				stmt.setString(13, user.googleId());
			}

			if (user.refreshToken() == null) {
				stmt.setNull(14, Types.LONGVARCHAR); // Use LONGVARCHAR or VARCHAR for TEXT columns
			} else {
				stmt.setString(14, user.refreshToken());
			}

			if (user.refreshTokenExpiresAt() == null) {
				stmt.setNull(15, Types.BIGINT);
			} else {
				stmt.setLong(15, user.refreshTokenExpiresAt());
			}

			int rowsInserted = stmt.executeUpdate();
			return rowsInserted > 0;
		} catch (Exception e) {
			System.err.println("Error inserting user with uuid: " + user.uuid());
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

	private static User parseUser(ResultSet rs) throws Exception {
		String uuid = rs.getString("uuid");
		String email = rs.getString("email");
		String passwordHash = rs.getString("password_hash");
		boolean isVerified = rs.getBoolean("is_verified");
		long createdAt = rs.getLong("created_at");
		long updatedAt = rs.getLong("updated_at");

		// Handle nullable last_login
		Long lastLogin = rs.getObject("last_login") != null ? rs.getLong("last_login") : null;

		// --- New Columns ---
		// acc_type has a default "both", but you can still retrieve it and pass it
		String accType = rs.getString("acc_type");
		if (accType == null || accType.isBlank()) { // Defensive check, though DB default should handle
			accType = "both";
		}
		String googleId = rs.getString("google_id");
		String refreshToken = rs.getString("refresh_token");
		Long refreshTokenExpiresAt = rs.getObject("refresh_token_expires_at") != null
				? rs.getLong("refresh_token_expires_at")
				: null;

		// Handle profile_pic default
		String profilePic = rs.getString("profile_pic");
		if (profilePic == null || profilePic.isBlank()) {
			profilePic = "https://i.pinimg.com/736x/2f/15/f2/2f15f2e8c688b3120d3d26467b06330c.jpg";
		}

		String fullName = rs.getString("full_name");

		// Handle metadata JSON default
		String metadataStr = rs.getString("metadata");
		JSONObject metadata = new JSONObject(metadataStr != null && !metadataStr.isBlank() ? metadataStr : "{}");

		// Handle permissions JSON default
		String permissionsStr = rs.getString("permissions");
		JSONObject permissions = new JSONObject(
				permissionsStr != null && !permissionsStr.isBlank() ? permissionsStr : "{}");

		// Build the User object with all fields
		return new User.Builder(uuid, email, createdAt, updatedAt).passwordHash(passwordHash).isVerified(isVerified)
				.lastLogin(lastLogin).profilePic(profilePic).fullName(fullName).metadata(metadata)
				.permissions(permissions).accType(accType).googleId(googleId).refreshToken(refreshToken)
				.refreshTokenExpiresAt(refreshTokenExpiresAt).build();
	}

}
