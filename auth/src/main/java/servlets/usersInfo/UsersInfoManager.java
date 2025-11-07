package servlets.usersInfo;

import db.UsersDAO;
import db.dbAuth;
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

public class UsersInfoManager {

	private static final Logger log = LogManager.getLogger(UsersInfoManager.class);

	public static void getUserInfo(HttpServletRequest req, HttpServletResponse resp, Map<String, String> ignoredParams) throws IOException {
		try (Connection conn = dbAuth.getConnection()) {
			String userUuid = AuthUtil.getUserUUIDFromAuthCookie(req, resp, conn);
			if (userUuid == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "User not logged in.");
				return;
			}
			User user = UsersDAO.getUserByUuid(conn, userUuid);
			if (user == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "error", "Logged in user not found.");
				AuthUtil.clearAuthCookie(resp);
				return;
			}
			HttpUtil.sendUser(resp, user);
		} catch (Exception e) {
			log.catching(e);
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Internal Server Error");
		}
	}

	public static void updateUserInfo(HttpServletRequest req, HttpServletResponse resp, Map<String, String> ignoredParams) throws IOException {
		Connection conn = null;
		try {
			conn = dbAuth.getConnection();
			conn.setAutoCommit(false);
			String userUuid = AuthUtil.getUserUUIDFromAuthCookie(req, resp, conn);
			if (userUuid == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "Unauthorized: No valid session.");
				AuthUtil.clearAuthCookie(resp);
				return;
			}

			// parse incoming JSON to DTO
			JSONObject jsonRequest = HttpUtil.readBodyJSON(req);
			UserProfileUpdateDTO updateDTO = new UserProfileUpdateDTO();
			if (jsonRequest.has("email"))
				updateDTO.setEmail(jsonRequest.getString("email"));
			if (jsonRequest.has("profile_pic"))
				updateDTO.setProfilePic(jsonRequest.getString("profile_pic"));
			if (jsonRequest.has("full_name"))
				updateDTO.setFullName(jsonRequest.getString("full_name"));
			User currentUser = UsersDAO.getUserByUuid(conn, userUuid);
			if (currentUser == null) {
				conn.rollback(); // Rollback if user not found
				HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "error", "User not found.");
				return;
			}

			long now = System.currentTimeMillis() / 1000; // Current timestamp in seconds
			boolean emailUpdated = false;
			boolean profileInfoUpdated = false;

			// --- Handle Email Update ---
			String newEmail = updateDTO.getEmail().toLowerCase();
			if (!newEmail.isBlank() && !currentUser.email().toLowerCase().equals(newEmail)) {
				if (!"password".equals(currentUser.accType())) {
					conn.rollback();
					HttpUtil.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, "error", "Email change is not allowed if you have a google account linked, unlink it first.");
					return;
				}
				if (!newEmail.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
					conn.rollback();
					HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Invalid email format.");
					return;
				}
				User existingUserWithNewEmail = UsersDAO.getUserByEmail(conn, newEmail);
				if (existingUserWithNewEmail != null && !existingUserWithNewEmail.uuid().equals(userUuid)) {
					conn.rollback();
					HttpUtil.sendJson(resp, HttpServletResponse.SC_CONFLICT, "error", "Email already in use by another account.");
					return;
				}

				if (UsersDAO.updateEmail(conn, userUuid, newEmail, now)) {
					emailUpdated = true;
				} else {
					conn.rollback();
					HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Failed to update email.");
					return;
				}
			}

			// --- Handle Profile Pic and Full Name Update ---
			String newProfilePic = updateDTO.getProfilePic();
			String newFullName = updateDTO.getFullName();
			if (newProfilePic != null || newFullName != null) {
				if (UsersDAO.updateProfileInfo(conn, userUuid, newFullName, newProfilePic, null, null, now)) {
					profileInfoUpdated = true;
				} else {
					conn.rollback();
					HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Failed to update profile info.");
					return;
				}
			}

			if (!emailUpdated && !profileInfoUpdated) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "No changes provided or no update necessary.");
			} else {
				conn.commit();
				HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "Profile updated successfully.");
			}

		} catch (Exception e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					log.catching(ex);
				}
			}
			log.catching(e);
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Internal Server Error during profile update.");
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
					conn.close();
				} catch (SQLException e) {
					log.catching(e);
				}
			}
		}
	}

}

class UserProfileUpdateDTO {
	private String email = null;
	private String profilePic = null;
	private String fullName = null;

	// Getters and Setters
	public String getEmail() {
		return email.toLowerCase();
	}

	public void setEmail(String email) {
		this.email = email.toLowerCase();
	}

	public String getProfilePic() {
		return profilePic;
	}

	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
}
