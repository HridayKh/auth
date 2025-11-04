package servlets.usersInfo;

import db.UsersDAO;
import db.dbAuth;
import dtos.UserProfileUpdateDTO;
import entities.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import utils.AuthUtil;
import utils.HttpUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class UsersInfoUpdater {

	private static final Logger log = LogManager.getLogger(UsersInfoUpdater.class);

	public static void updateUserInfo(HttpServletRequest req, HttpServletResponse resp, Map<String, String> ignoredParams) throws IOException {
		Connection conn = null;
		try {
			conn = dbAuth.getConnection();
			conn.setAutoCommit(false); // Start transaction for atomic updates

			String userUuid = AuthUtil.getUserUUIDFromAuthCookie(req, resp, conn);
			if (userUuid == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error",
					"Unauthorized: No valid session.");
				return;
			}

			// 1. Read and parse incoming JSON to DTO
			JSONObject jsonRequest = HttpUtil.readBodyJSON(req);
			UserProfileUpdateDTO updateDTO = new UserProfileUpdateDTO();

			// Manually populate DTO, only if fields are present in the JSON
			if (jsonRequest.has("email")) {
				updateDTO.setEmail(jsonRequest.getString("email"));
			}
			if (jsonRequest.has("profile_pic")) {
				updateDTO.setProfilePic(jsonRequest.getString("profile_pic"));
			}
			if (jsonRequest.has("full_name")) {
				updateDTO.setFullName(jsonRequest.getString("full_name"));
			}

			long now = System.currentTimeMillis() / 1000; // Current timestamp in seconds

			// Fetch current user details to apply business rules
			User currentUser = UsersDAO.getUserByUuid(conn, userUuid);
			if (currentUser == null) {
				conn.rollback(); // Rollback if user not found
				HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "error", "User not found.");
				return;
			}

			boolean profileInfoUpdated = false;
			boolean emailUpdated = false;

			// --- Handle Email Update ---
			String newEmail = updateDTO.getEmail().toLowerCase();
			// Only proceed if email is actually changing
			if (!newEmail.isEmpty() && !currentUser.email().equals(newEmail)) {
				if (!"password".equals(currentUser.accType())) {
					conn.rollback();
					HttpUtil.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, "error",
						"Email change is not allowed if you have a google account linked, unlink it first.");
					return;
				}
				// Validate new email format (optional, but recommended)
				if (!isValidEmail(newEmail.toLowerCase())) {
					conn.rollback();
					HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Invalid email format.");
					return;
				}
				// Check if new email already exists for another user
				User existingUserWithNewEmail = UsersDAO.getUserByEmail(conn, newEmail.toLowerCase());
				if (existingUserWithNewEmail != null && !existingUserWithNewEmail.uuid().equals(userUuid)) {
					conn.rollback();
					HttpUtil.sendJson(resp, HttpServletResponse.SC_CONFLICT, "error",
						"Email already in use by another account.");
					return;
				}

				if (UsersDAO.updateEmail(conn, userUuid, newEmail.toLowerCase(), now)) {
					emailUpdated = true;
				} else {
					conn.rollback();
					HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
						"Failed to update email.");
					return;
				}
			}

			// --- Handle Profile Pic and Full Name Update ---
			String newProfilePic = updateDTO.getProfilePic();
			String newFullName = updateDTO.getFullName();

			// Only call updateProfileInfo if at least one of these fields is provided
			if (newProfilePic != null || newFullName != null) {
				// Pass null for metadata and permissions if they are not part of this update
				// (assuming updateProfileInfo handles nulls by not updating those columns)
				if (UsersDAO.updateProfileInfo(conn, userUuid, newFullName, newProfilePic, null, null, now)) {
					profileInfoUpdated = true;
				} else {
					conn.rollback();
					HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
						"Failed to update profile info.");
					return;
				}
			}

			// If no fields were provided in the request, or nothing changed
			if (!emailUpdated && !profileInfoUpdated) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success",
					"No changes provided or no update necessary.");
			} else {
				conn.commit(); // Commit transaction if all updates were successful
				HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "Profile updated successfully.");
			}

		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback(); // Rollback on SQL error
				} catch (SQLException ex) {
					log.catching(ex);
				}
			}
			log.catching(e);
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
				"Database error during profile update.");
		} catch (Exception e) {
			if (conn != null) {
				try {
					conn.rollback(); // Rollback on any other error
				} catch (SQLException ex) {
					log.catching(ex);
				}
			}
			log.catching(e);
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
				"Internal Server Error during profile update.");
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true); // Reset auto-commit
					conn.close();
				} catch (SQLException e) {
					log.catching(e);
				}
			}
		}
	}

	// Basic email format validation (can be more robust with regex)
	private static boolean isValidEmail(String email) {
		return email != null && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
	}
}