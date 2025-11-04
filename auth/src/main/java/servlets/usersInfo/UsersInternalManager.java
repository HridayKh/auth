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

public class UsersInternalManager {

	private static final Logger log = LogManager.getLogger(UsersInternalManager.class);

	public static void getUserInternalInfo(HttpServletRequest req, HttpServletResponse resp, Map<String, String> ignoredParams) throws IOException {
		try (Connection conn = dbAuth.getConnection()) {
			String userUuid = AuthUtil.getUserUUIDFromAuthCookie(req, resp, conn);
			if (userUuid == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "Not Logged In");
				AuthUtil.clearAuthCookie(resp);
				return;
			}
			User user = UsersDAO.getUserByUuid(conn, userUuid);
			if (user == null) {
				HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "error", "User not found");
				AuthUtil.clearAuthCookie(resp);
				return;
			}
			HttpUtil.sendUserMetadataPerms(resp, user);
		} catch (Exception e) {
			log.catching(e);
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Internal Server Error");
		}
	}

	public static void updateUserInternalInfo(HttpServletRequest req, HttpServletResponse resp, Map<String, String> ignoredParams) throws IOException {
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");

		Connection conn = null;
		try {
			conn = dbAuth.getConnection();
			conn.setAutoCommit(false);

			// User cookie is passed on as a secure way to identify the user instead of trusting to provide the correct user login.
			String userUuid = AuthUtil.getUserUUIDFromAuthCookie(req, resp, conn);
			if (userUuid == null) {
				conn.rollback();
				HttpUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "User not logged in.");
				AuthUtil.clearAuthCookie(resp);
				return;
			}

			User currentUser = UsersDAO.getUserByUuid(conn, userUuid);
			if (currentUser == null) {
				conn.rollback();
				HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, "error", "User not found.");
				return;
			}

			JSONObject newInternal = HttpUtil.readBodyJSON(req);
			JSONObject finalInternal = deepMerge(newInternal, currentUser.internal());

			if (UsersDAO.updateInternalInfo(conn, userUuid, finalInternal, System.currentTimeMillis() / 1000)) {
				conn.commit();
				HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "success", "Metadata and permissions updated successfully.");
			} else {
				conn.rollback();
				HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Failed to update metadata or permissions. No changes, or database error.");
			}

		} catch (Exception e) {
			log.catching(e);
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					log.catching(ex);
				}
			}
			HttpUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Internal Server Error during metadata/permissions update.");
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

	private static JSONObject deepMerge(JSONObject source, JSONObject target) {
		if (source == null)
			return target;
		for (String key : source.keySet()) {
			Object sourceValue = source.get(key);
			if (sourceValue == JSONObject.NULL || sourceValue == null) {
				target.remove(key);
				continue;
			}
			if (sourceValue instanceof JSONObject sourceJsonObject && target.opt(key) instanceof JSONObject) {
				JSONObject targetValue = target.getJSONObject(key);
				deepMerge(targetValue, sourceJsonObject);
			} else {
				target.put(key, sourceValue);
			}
		}
		return target;
	}

}