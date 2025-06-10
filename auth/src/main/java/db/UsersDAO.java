package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.json.JSONObject;

import entities.User;

public class UsersDAO {

	/**
	 * Retrieves a {@link User} object by their UUID. This method fetches all stored
	 * user details, including new fields like Google ID and account type, from the
	 * database and constructs a {@link User} entity. It is typically used by
	 * authentication filters or API endpoints that require full user information
	 * after session validation.
	 *
	 * @param conn The active database connection.
	 * @param uuid The unique identifier (UUID) of the user to retrieve.
	 * @return A {@link User} object if a user with the specified UUID is found,
	 *         otherwise returns {@code null}.
	 * @throws SQLException If a database access error occurs.
	 */
	public static User getUserByUuid(Connection conn, String uuid) throws SQLException {
		// Updated SQL query to use snake_case column names
		String sql = "SELECT uuid, email, password_hash, is_verified, created_at, updated_at, last_login, "
				+ "profile_pic, full_name, metadata, permissions, google_id, acc_type, refresh_token, refresh_token_expires_at "
				+ "FROM users WHERE uuid = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, uuid);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return parseUser(rs); // Use the helper method to parse the ResultSet into a User object
				}
			}
		}
		return null; // User not found
	}

	/**
	 * Retrieves a {@link User} object by their email address. This method is
	 * typically used during the login or registration process to check for the
	 * existence of a user with a given email.
	 *
	 * @param conn  The active database connection.
	 * @param email The email address of the user to retrieve.
	 * @return A {@link User} object if a user with the specified email is found,
	 *         otherwise returns {@code null}. Returns the first matching user if
	 *         multiple exist (though email should be unique).
	 * @throws SQLException If a database access error occurs.
	 */
	public static User getUserByEmail(Connection conn, String email) throws SQLException {
		// Updated SQL query to use snake_case column names
		String sql = "SELECT * FROM users WHERE email = ? LIMIT 1"; // Limit 1 as email should be unique
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, email);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next() ? parseUser(rs) : null; // Parse if a user is found
			}
		}
	}

	/**
	 * Retrieves a {@link User} object by their email address and password hash.
	 * This method is specifically used for traditional email/password login to
	 * authenticate a user against their stored hashed password.
	 *
	 * @param conn          The active database connection.
	 * @param email         The email address of the user.
	 * @param password_hash The hashed password to verify against.
	 * @return A {@link User} object if a user matches both the email and password
	 *         hash, otherwise returns {@code null}.
	 * @throws SQLException If a database access error occurs.
	 */
	public static User getUserByEmailPass(Connection conn, String email, String password_hash) throws SQLException {
		// Updated SQL query to use snake_case column names
		String sql = "SELECT * FROM users WHERE email = ? AND password_hash = ? LIMIT 1"; // Fixed passwordHash to
																							// password_hash
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, email);
			stmt.setString(2, password_hash);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next() ? parseUser(rs) : null; // Parse if a user is found
			}
		}
	}

	/**
	 * Retrieves a {@link User} object by their email address. This method is
	 * typically used during the login or registration process to check for the
	 * existence of a user with a given email.
	 *
	 * @param conn  The active database connection.
	 * @param email The email address of the user to retrieve.
	 * @return A {@link User} object if a user with the specified email is found,
	 *         otherwise returns {@code null}. Returns the first matching user if
	 *         multiple exist (though email should be unique).
	 * @throws SQLException If a database access error occurs.
	 */
	public static User getUserByGoogleID(Connection conn, String GoogleID) throws SQLException {
		// Updated SQL query to use snake_case column names
		String sql = "SELECT * FROM users WHERE google_id = ? LIMIT 1"; // Limit 1 as email should be unique
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, GoogleID);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next() ? parseUser(rs) : null; // Parse if a user is found
			}
		}
	}

	/**
	 * Inserts a new user record into the 'users' table. This method handles setting
	 * values for all user fields, including newly added nullable fields and JSON
	 * objects (metadata, permissions), ensuring correct SQL type mapping.
	 *
	 * @param conn The active database connection.
	 * @param user The {@link User} object containing the data for the new user.
	 * @return {@code true} if the user was successfully inserted, {@code false}
	 *         otherwise.
	 * @throws SQLException If a database access error occurs during the insert
	 *                      operation.
	 */
	public static boolean insertUser(Connection conn, User user) throws SQLException {
		// Updated SQL query to use snake_case column names
		String sql = "INSERT INTO users (" + "uuid, email, password_hash, is_verified, created_at, updated_at, "
				+ "full_name, profile_pic, last_login, metadata, permissions, "
				+ "acc_type, google_id, refresh_token, refresh_token_expires_at"
				+ ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, user.uuid());
			stmt.setString(2, user.email());

			// Handle nullable passwordHash (Java object field)
			if (user.passwordHash() == null) {
				stmt.setNull(3, Types.VARCHAR);
			} else {
				stmt.setString(3, user.passwordHash());
			}

			stmt.setBoolean(4, user.isVerified());
			stmt.setLong(5, user.createdAt());
			stmt.setLong(6, user.updatedAt());
			stmt.setString(7, user.fullName());
			stmt.setString(8, user.profilePic());

			// Handle nullable lastLogin (Java object field)
			if (user.lastLogin() == null) {
				stmt.setNull(9, Types.BIGINT);
			} else {
				stmt.setLong(9, user.lastLogin());
			}

			// Store JSONObjects as strings (Java object fields)
			stmt.setString(10, user.metadata().toString());
			stmt.setString(11, user.permissions().toString());

			// Set values for new columns (Java object fields)
			stmt.setString(12, user.accType()); // acc_type has a default, but we're explicitly setting it

			// Handle nullable googleId (Java object field)
			if (user.googleId() == null) {
				stmt.setNull(13, Types.VARCHAR);
			} else {
				stmt.setString(13, user.googleId());
			}

			// Handle nullable refreshToken (Java object field)
			if (user.refreshToken() == null) {
				stmt.setNull(14, Types.LONGVARCHAR);
			} else {
				stmt.setString(14, user.refreshToken());
			}

			// Handle nullable refreshTokenExpiresAt (Java object field)
			if (user.refreshTokenExpiresAt() == null) {
				stmt.setNull(15, Types.BIGINT);
			} else {
				stmt.setLong(15, user.refreshTokenExpiresAt());
			}

			int rowsInserted = stmt.executeUpdate();
			return rowsInserted > 0;
		}
	}

	/**
	 * Updates the `is_verified` status of a user and sets their `last_login`
	 * timestamp. This method is typically called after a user successfully verifies
	 * their email address.
	 *
	 * @param conn     The active database connection.
	 * @param userUuid The UUID of the user to update.
	 * @param timeNow  The current timestamp (in seconds) to set as `last_login`.
	 * @return {@code true} if the user's verification status was successfully
	 *         updated, {@code false} otherwise.
	 * @throws SQLException If a database access error occurs.
	 */
	public static boolean updateUserVerify(Connection conn, String userUuid, long timeNow) throws SQLException {
		// Updated SQL query to use snake_case column names
		String sql = "UPDATE users SET is_verified = ?, last_login = ? WHERE uuid = ?"; // Fixed here
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setBoolean(1, true); // Set is_verified to true (1)
			stmt.setLong(2, timeNow);
			stmt.setString(3, userUuid);
			int rowsUpdated = stmt.executeUpdate();
			return rowsUpdated > 0;
		}
	}

	/**
	 * Updates the `last_login` timestamp for a specific user. This method is
	 * typically called upon successful login to track user activity.
	 *
	 * @param conn    The active database connection.
	 * @param uuid    The UUID of the user whose last login timestamp should be
	 *                updated.
	 * @param timeNow The current timestamp (in seconds) to set as `last_login`.
	 * @return {@code true} if the `last_login` timestamp was successfully updated,
	 *         {@code false} otherwise.
	 * @throws SQLException If a database access error occurs.
	 */
	public static boolean updateLastLogin(Connection conn, String uuid, long timeNow) throws SQLException {
		// Updated SQL query to use snake_case column name
		String sql = "UPDATE users SET last_login = ? WHERE uuid = ?"; // Fixed here
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setLong(1, timeNow);
			stmt.setString(2, uuid);
			int rowsUpdated = stmt.executeUpdate();
			return rowsUpdated > 0;
		}
	}

	/**
	 * Updates non-credential user profile information. Allows for partial updates
	 * of `full_name`, `profile_pic`, `metadata` (as JSON string), and `permissions`
	 * (as JSON string). Only fields with non-null values provided will be updated.
	 *
	 * @param conn        The active database connection.
	 * @param userUuid    The UUID of the user to update.
	 * @param fullName    The new full name (can be {@code null} to keep existing).
	 * @param profilePic  The new profile picture URL (can be {@code null} to keep
	 *                    existing).
	 * @param metadata    The new metadata {@link JSONObject} (can be {@code null}
	 *                    to keep existing).
	 * @param permissions The new permissions {@link JSONObject} (can be
	 *                    {@code null} to keep existing).
	 * @param updatedAt   The timestamp (in seconds) of the update.
	 * @return {@code true} if the user's profile information was successfully
	 *         updated, {@code false} otherwise.
	 * @throws SQLException If a database access error occurs.
	 */
	public static boolean updateProfileInfo(Connection conn, String userUuid, String fullName, String profilePic,
			JSONObject metadata, JSONObject permissions, long updatedAt) throws SQLException {
		// Updated SQL query to use snake_case column names
		StringBuilder sql = new StringBuilder("UPDATE users SET updated_at = ?"); // Fixed here
		int paramIndex = 1;

		if (fullName != null) {
			sql.append(", full_name = ?"); // Fixed here
		}
		if (profilePic != null) {
			sql.append(", profile_pic = ?"); // Fixed here
		}
		if (metadata != null) {
			sql.append(", metadata = ?");
		}
		if (permissions != null) {
			sql.append(", permissions = ?");
		}
		sql.append(" WHERE uuid = ?");

		try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
			pstmt.setLong(paramIndex++, updatedAt);

			if (fullName != null) {
				pstmt.setString(paramIndex++, fullName);
			}
			if (profilePic != null) {
				pstmt.setString(paramIndex++, profilePic);
			}
			if (metadata != null) {
				pstmt.setString(paramIndex++, metadata.toString()); // Store as JSON string
			}
			if (permissions != null) {
				pstmt.setString(paramIndex++, permissions.toString()); // Store as JSON string
			}
			pstmt.setString(paramIndex++, userUuid);

			return pstmt.executeUpdate() > 0;
		}
	}

	/**
	 * Updates the email address of a user. This operation is typically restricted
	 * based on the user's account type in the business logic layer.
	 *
	 * @param conn      The active database connection.
	 * @param userUuid  The UUID of the user whose email is to be updated.
	 * @param newEmail  The new email address for the user.
	 * @param updatedAt The timestamp (in seconds) of the update.
	 * @return {@code true} if the email was successfully updated, {@code false}
	 *         otherwise.
	 * @throws SQLException If a database access error occurs.
	 */
	public static boolean updateEmail(Connection conn, String userUuid, String newEmail, long updatedAt)
			throws SQLException {
		String sql = "UPDATE users SET email = ?, updated_at = ? WHERE uuid = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, newEmail);
			pstmt.setLong(2, updatedAt);
			pstmt.setString(3, userUuid);
			int rowsUpdated = pstmt.executeUpdate();
			return rowsUpdated > 0;
		}
	}

	/**
	 * Updates the user's password hash and potentially their account type. If the
	 * current account type is 'google' (meaning no email/password login
	 * capability), setting a password will change the `acc_type` to 'both'. For
	 * 'email' or 'both' accounts, it only updates the password hash.
	 *
	 * @param conn            The active database connection.
	 * @param userUuid        The UUID of the user.
	 * @param newPasswordHash The new hashed password to store.
	 * @param currentAccType  The user's current account type (e.g., 'email',
	 *                        'google', 'both').
	 * @param updatedAt       The timestamp (in seconds) of the update.
	 * @return {@code true} if the update was successful, {@code false} otherwise.
	 * @throws SQLException If a database access error occurs.
	 */
	public static boolean updatePasswordAndAccType(Connection conn, String userUuid, String newPasswordHash,
			String currentAccType, long updatedAt) throws SQLException {
		String sql;
		if ("google".equals(currentAccType)) {
			// If user is 'google' only and sets a password, change to 'both'
			// Updated SQL query to use snake_case column names
			sql = "UPDATE users SET password_hash = ?, acc_type = 'both', updated_at = ? WHERE uuid = ?"; // Fixed here
		} else {
			// For 'email' or 'both' accounts, just update password_hash
			// Updated SQL query to use snake_case column names
			sql = "UPDATE users SET password_hash = ?, updated_at = ? WHERE uuid = ?"; // Fixed here
		}

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, newPasswordHash);
			pstmt.setLong(2, updatedAt);
			pstmt.setString(3, userUuid);
			return pstmt.executeUpdate() > 0;
		}
	}

	/**
	 * Links a Google ID and associated refresh token to an existing user account.
	 * This method updates the `google_id`, `acc_type` (typically to 'both'),
	 * `refresh_token`, and `refresh_token_expires_at` fields for the specified
	 * user.
	 *
	 * @param conn                  The active database connection.
	 * @param userUuid              The UUID of the user to link the Google account
	 *                              to.
	 * @param googleId              The unique identifier provided by Google for the
	 *                              user.
	 * @param accType               The new account type for the user (e.g.,
	 *                              'both').
	 * @param refreshToken          The Google OAuth 2.0 Refresh Token. **Should be
	 *                              encrypted at rest in the database.**
	 * @param refreshTokenExpiresAt The expiration timestamp (in seconds) for the
	 *                              refresh token, or {@code null} if indefinite.
	 * @param updatedAt             The timestamp (in seconds) of the update.
	 * @return {@code true} if the Google account was successfully linked,
	 *         {@code false} otherwise.
	 * @throws SQLException If a database access error occurs.
	 */
	public static boolean linkGoogleAccount(Connection conn, String userUuid, String googleId, String accType,
			String refreshToken, Long refreshTokenExpiresAt, long updatedAt) throws SQLException {
		// Updated SQL query to use snake_case column names
		String sql = "UPDATE users SET google_id = ?, acc_type = ?, refresh_token = ?, refresh_token_expires_at = ?, updated_at = ? WHERE uuid = ?"; // Fixed
																																						// here
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, googleId);
			pstmt.setString(2, accType);
			pstmt.setString(3, refreshToken);
			if (refreshTokenExpiresAt != null) {
				pstmt.setLong(4, refreshTokenExpiresAt);
			} else {
				pstmt.setNull(4, java.sql.Types.BIGINT); // Use SQL Types for nullable Long
			}
			pstmt.setLong(5, updatedAt);
			pstmt.setString(6, userUuid);
			return pstmt.executeUpdate() > 0;
		}
	}

	/**
	 * Unlinks a Google account from a user's profile. This method sets the
	 * `google_id`, `refresh_token`, and `refresh_token_expires_at` to `NULL`, and
	 * updates the `acc_type` to the specified new type (typically 'email' if the
	 * user has an existing password).
	 *
	 * @param conn       The active database connection.
	 * @param userUuid   The UUID of the user to unlink the Google account from.
	 * @param newAccType The new account type for the user after unlinking (e.g.,
	 *                   'email').
	 * @param updatedAt  The timestamp (in seconds) of the update.
	 * @return {@code true} if the Google account was successfully unlinked,
	 *         {@code false} otherwise.
	 * @throws SQLException If a database access error occurs.
	 */
	public static boolean unlinkGoogleAccount(Connection conn, String userUuid, String newAccType, long updatedAt)
			throws SQLException {
		// Updated SQL query to use snake_case column names
		String sql = "UPDATE users SET google_id = NULL, acc_type = ?, refresh_token = NULL, refresh_token_expires_at = NULL, updated_at = ? WHERE uuid = ?"; // Fixed
																																								// here
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, newAccType);
			pstmt.setLong(2, updatedAt);
			pstmt.setString(3, userUuid);
			return pstmt.executeUpdate() > 0;
		}
	}

	/**
	 * A private helper method to parse a {@link ResultSet} row into a {@link User}
	 * object. This consolidates the logic for extracting all user fields from the
	 * database, handling nullable columns and converting JSON string fields back to
	 * {@link JSONObject}s.
	 *
	 * @param rs The {@link ResultSet} containing the current row of user data.
	 * @return A fully constructed {@link User} object.
	 * @throws SQLException If a database access error occurs when reading the
	 *                      ResultSet.
	 */
	private static User parseUser(ResultSet rs) throws SQLException {
		// Core user fields - using snake_case for ResultSet column names
		String uuid = rs.getString("uuid");
		String email = rs.getString("email");
		String passwordHash = rs.getString("password_hash"); // Fixed to snake_case
		boolean isVerified = rs.getBoolean("is_verified"); // Fixed to snake_case
		long createdAt = rs.getLong("created_at"); // Fixed to snake_case
		long updatedAt = rs.getLong("updated_at"); // Fixed to snake_case

		// Handle nullable lastLogin (using getObject for correct null handling of
		// primitives) - Fixed to snake_case
		Long lastLogin = rs.getObject("last_login", Long.class);

		// New Account Type & Google-related fields - already snake_case
		String accType = rs.getString("acc_type");
		// Defensive check, though DB default should handle if column exists and is not
		// null
		if (accType == null || accType.isBlank()) {
			accType = "password"; // Default value as per your User entity
		}
		String googleId = rs.getString("google_id"); // Nullable string
		String refreshToken = rs.getString("refresh_token"); // Nullable string
		// Handle nullable refreshTokenExpiresAt - Fixed to snake_case
		Long refreshTokenExpiresAt = rs.getObject("refresh_token_expires_at", Long.class);

		// Optional profile fields with defaults - Fixed to snake_case
		String profilePic = rs.getString("profile_pic");
		// Apply default if null or empty string, as per your User.Builder's default
		if (profilePic == null || profilePic.isBlank()) {
			profilePic = "https://i.pinimg.com/736x/2f/15/f2/2f15f2e8c688b3120d3d26467b06330c.jpg";
		}
		String fullName = rs.getString("full_name"); // Nullable string - Fixed to snake_case

		// Handle metadata and permissions JSON objects - already snake_case
		String metadataStr = rs.getString("metadata");
		JSONObject metadata = new JSONObject(metadataStr != null && !metadataStr.isBlank() ? metadataStr : "{}");

		String permissionsStr = rs.getString("permissions");
		JSONObject permissions = new JSONObject(
				permissionsStr != null && !permissionsStr.isBlank() ? permissionsStr : "{}");

		// Build the User object using its Builder pattern (using Java object field
		// names)
		return new User.Builder(uuid, email, createdAt, updatedAt).passwordHash(passwordHash).isVerified(isVerified)
				.lastLogin(lastLogin).profilePic(profilePic).fullName(fullName).metadata(metadata)
				.permissions(permissions).accType(accType).googleId(googleId).refreshToken(refreshToken)
				.refreshTokenExpiresAt(refreshTokenExpiresAt).build();
	}

	
	public static boolean updateUser(Connection conn, User user) throws SQLException {
		// Updated SQL query to use snake_case column names
		String sql = "UPDATE users SET " + "email = ?, " + "password_hash = ?, " + "is_verified = ?, "
				+ "created_at = ?, " + "updated_at = ?, " + "last_login = ?, " + "acc_type = ?, " + "google_id = ?, "
				+ "refresh_token = ?, " + "refresh_token_expires_at = ?, " + "profile_pic = ?, " + "full_name = ?, "
				+ "metadata = ?, " + "permissions = ? " + "WHERE uuid = ?";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, user.email());

			// Handle nullable passwordHash
			if (user.passwordHash() == null) {
				pstmt.setNull(2, Types.VARCHAR);
			} else {
				pstmt.setString(2, user.passwordHash());
			}

			pstmt.setBoolean(3, user.isVerified());
			pstmt.setLong(4, user.createdAt()); // Should not change, but included for completeness
			pstmt.setLong(5, user.updatedAt());
			pstmt.setObject(6, user.lastLogin(), Types.BIGINT); // Use setNull/setObject for nullable Long
			pstmt.setString(7, user.accType());

			// Handle nullable googleId
			if (user.googleId() == null) {
				pstmt.setNull(8, Types.VARCHAR);
			} else {
				pstmt.setString(8, user.googleId());
			}

			// Handle nullable refreshToken
			if (user.refreshToken() == null) {
				pstmt.setNull(9, Types.VARCHAR);
			} else {
				pstmt.setString(9, user.refreshToken());
			}

			// Handle nullable refreshTokenExpiresAt
			if (user.refreshTokenExpiresAt() == null) {
				pstmt.setNull(10, Types.BIGINT); // Use Types.BIGINT for Long
			} else {
				pstmt.setLong(10, user.refreshTokenExpiresAt());
			}

			pstmt.setString(11, user.profilePic());
			pstmt.setString(12, user.fullName());
			pstmt.setString(13, user.metadata().toString());
			pstmt.setString(14, user.permissions().toString());
			pstmt.setString(15, user.uuid());

			int rowsUpdated = pstmt.executeUpdate();
			return rowsUpdated > 0;
		}
	}
}
