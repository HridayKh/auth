package servlets.profile;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import org.json.JSONObject;
import org.json.JSONException;

import db.UsersDAO;
import db.dbAuth;
import dtos.UserMetadataPermissionsUpdateDTO;
import entities.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.AuthUtil;
import utils.HttpUtil;

public class GetUserAdminProfileHandler {

	public static void getUserAdminProfile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");

		Connection conn = null;
		try {
			conn = dbAuth.getConnection();
			conn.setAutoCommit(false); // Start transaction for atomic updates

			boolean appAuthenticated = AuthUtil.verifyBasicAuthHeaderWithStaticPassword(req);
			if (!appAuthenticated) {
				conn.rollback(); // Rollback any potential connection state
				// Send 401 Unauthorized and prompt for Basic Auth credentials
				resp.setHeader("WWW-Authenticate", "Basic realm=\"API Access\"");
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error",
						"Unauthorized: Invalid API key/password.");
				return;
			}

			// Authenticate user. For "other apps," consider API keys/OAuth tokens.
			String userUuid = AuthUtil.getUserUUIDFromAuthCookie(req, resp, conn);
			if (userUuid == null) {
				conn.rollback(); // Rollback any potential connection state
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error",
						"Unauthorized: No valid session/API key.");
				return;
			}

			// 1. Read and parse incoming JSON request body
			StringBuilder jsonBody = new StringBuilder();
			try (java.io.BufferedReader reader = req.getReader()) {
				String line;
				while ((line = reader.readLine()) != null) {
					jsonBody.append(line);
				}
			}

			JSONObject jsonRequest = new JSONObject(jsonBody.toString());
			UserMetadataPermissionsUpdateDTO updateDTO = new UserMetadataPermissionsUpdateDTO();

			// Manually populate DTO, handling nullable JSONObjects and boolean flags
			if (jsonRequest.has("metadata")) {
				updateDTO.setMetadata(jsonRequest.getJSONObject("metadata"));
			}
			if (jsonRequest.has("permissions")) {
				updateDTO.setPermissions(jsonRequest.getJSONObject("permissions"));
			}
			// Populate merge flags (default to false if not present)
			updateDTO.setMetadataMerge(jsonRequest.optBoolean("metadataMerge", false));
			updateDTO.setPermissionsMerge(jsonRequest.optBoolean("permissionsMerge", false));

			long now = System.currentTimeMillis() / 1000; // Current timestamp in seconds

			// Fetch current user's complete data to get existing metadata/permissions
			User currentUser = UsersDAO.getUserByUuid(conn, userUuid);
			if (currentUser == null) {
				conn.rollback();
				HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "error", "User not found.");
				return;
			}

			// Prepare the JSONObjects that will be passed to the DAO
			JSONObject finalMetadata = currentUser.metadata(); // Start with current metadata
			JSONObject finalPermissions = currentUser.permissions(); // Start with current permissions

			// --- Apply Metadata Merge Logic ---
			if (updateDTO.getMetadata() != null) { // Only process if new metadata was provided
				if (updateDTO.isMetadataMerge()) {
					// Merge: Iterate over incoming metadata and put into current metadata
					// This performs a shallow merge (overwriting existing keys, adding new ones)
					Iterator<String> keys = updateDTO.getMetadata().keys();
					while (keys.hasNext()) {
						String key = keys.next();
						finalMetadata.put(key, updateDTO.getMetadata().get(key));
					}
				} else {
					// Replace: If no merge flag or false, replace entirely
					finalMetadata = updateDTO.getMetadata();
				}
			} else {
				// If updateDTO.getMetadata() is null, it means no 'metadata' key was in the
				// request.
				// In this case, we retain the existing metadata.
				// If a null was explicitly intended, the client would send {"metadata": null}.
			}

			// --- Apply Permissions Merge Logic ---
			if (updateDTO.getPermissions() != null) { // Only process if new permissions were provided
				if (updateDTO.isPermissionsMerge()) {
					// Merge: Iterate over incoming permissions and put into current permissions
					Iterator<String> keys = updateDTO.getPermissions().keys();
					while (keys.hasNext()) {
						String key = keys.next();
						finalPermissions.put(key, updateDTO.getPermissions().get(key));
					}
				} else {
					// Replace: If no merge flag or false, replace entirely
					finalPermissions = updateDTO.getPermissions();
				}
			} else {
				// If updateDTO.getPermissions() is null, it means no 'permissions' key was in
				// the request.
				// In this case, we retain the existing permissions.
			}

			// Call the DAO method, passing the final (merged or replaced) JSONObjects
			boolean updated = UsersDAO.updateProfileInfo(conn, userUuid, null, // fullName not handled here
					null, // profilePic not handled here
					finalMetadata, // Pass final metadata
					finalPermissions, // Pass final permissions
					now);

			if (updated) {
				conn.commit(); // Commit transaction if successful
				HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success",
						"Metadata and permissions updated successfully.");
			} else {
				conn.rollback(); // Rollback if DAO update failed
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
						"Failed to update metadata or permissions. No changes, or database error.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			if (conn != null) {
				try {
					conn.rollback(); // Rollback on SQL error
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
					"Database error during metadata/permissions update.");
		} catch (JSONException e) { // Catch org.json.JSONException specifically for parsing issues
			e.printStackTrace();
			if (conn != null) {
				try {
					conn.rollback(); // Rollback on JSON parsing error
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error",
					"Invalid JSON format for request body.");
		} catch (Exception e) {
			e.printStackTrace();
			if (conn != null) {
				try {
					conn.rollback(); // Rollback on any other error
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
					"Internal Server Error during metadata/permissions update.");
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true); // Reset auto-commit state
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}