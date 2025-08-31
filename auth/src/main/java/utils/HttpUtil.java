package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Base64;

import org.json.JSONObject;

import db.dbAuth;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import entities.User;

public class HttpUtil {
	private static final String USER_COOKIE_NAME = "hridaykh_in_user_info";

	public static JSONObject readBodyJSON(HttpServletRequest req) {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = req.getReader()) {
			String line;
			while ((line = reader.readLine()) != null)
				sb.append(line);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new JSONObject(sb.toString());
	}

	public static void sendJson(HttpServletResponse resp, int status, String type, String message) throws IOException {
		resp.setStatus(status);
		resp.setContentType("application/json");
		JSONObject json = new JSONObject();
		json.put("message", message);
		json.put("type", type);
		resp.getWriter().write(json.toString());
	}

	public static void sendJsonReVerify(HttpServletResponse res, int stat, String type, String message)
			throws IOException {
		res.setStatus(stat);
		res.setContentType("application/json");
		JSONObject json = new JSONObject();
		json.put("reverify", true);
		json.put("message", message);
		json.put("type", type);
		res.getWriter().write(json.toString());
	}

	public static void sendUser(HttpServletResponse resp, User user) throws IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("application/json");

		JSONObject json = new JSONObject();
		json.put("type", "success");
		json.put("uuid", user.uuid());
		json.put("email", user.email().toLowerCase());
		json.put("is_verified", user.isVerified());
		json.put("created_at", user.createdAt());
		json.put("updated_at", user.updatedAt());

		// Optional fields
		if (user.lastLogin() != null) {
			json.put("last_login", user.lastLogin());
		}
		if (user.profilePic() != null) {
			json.put("profile_pic", user.profilePic());
		}
		if (user.fullName() != null) {
			json.put("full_name", user.fullName());
		}
		if (user.metadata() != null && user.metadata().has("public")) {
			Object userPublic = user.metadata().get("public");
			if (userPublic instanceof String) {
				json.put("metadata_public", (String) userPublic);
			} else if (userPublic instanceof JSONObject) {
				json.put("metadata_public", (JSONObject) userPublic);
			} else {
				// Handle other types if necessary, e.g., convert to string representation
				json.put("metadata_public", userPublic.toString());
			}
		}
		if (user.permissions() != null) {
			json.put("permissions", user.permissions());
		}
		if (user.accType() != null) {
			json.put("accType", user.accType());
		}

		resp.getWriter().write(json.toString());
	}

	public static void sendUserMetadataPerms(HttpServletResponse resp, User user) throws IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("application/json");

		JSONObject json = new JSONObject();
		json.put("type", "success");
		if (user.metadata() != null) {
			json.put("metadata", user.metadata());
		} else {
			json.put("metadata", new JSONObject());
		}
		if (user.permissions() != null) {
			json.put("permissions", user.permissions());
		} else {
			json.put("permissions", new JSONObject());
		}
		resp.getWriter().write(json.toString());
	}

	public static void createAndSetUserCookie(HttpServletResponse resp, User user) throws SQLException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("application/json");

		JSONObject json = new JSONObject();
		json.put("uuid", user.uuid());
		json.put("email", user.email().toLowerCase());
		json.put("created_at", user.createdAt());

		if (user.profilePic() != null) {
			json.put("profile_pic", user.profilePic());
		}
		if (user.fullName() != null) {
			json.put("full_name", user.fullName());
		}
		if (user.metadata() != null && user.metadata().has("public")) {
			Object userPublic = user.metadata().get("public");
			if (userPublic instanceof String) {
				json.put("metadata_public", (String) userPublic);
			} else if (userPublic instanceof JSONObject) {
				json.put("metadata_public", (JSONObject) userPublic);
			} else {
				// Handle other types if necessary, e.g., convert to string representation
				json.put("metadata_public", userPublic.toString());
			}
		}
		if (user.permissions() != null) {
			json.put("permissions", user.permissions());
		}
		if (user.accType() != null) {
			json.put("accType", user.accType());
		}
		Cookie userCookie = new Cookie(USER_COOKIE_NAME,
				Base64.getEncoder().encodeToString(json.toString().getBytes(StandardCharsets.UTF_8)));
		if ("yes".equals(dbAuth.PROD)) {
			userCookie.setSecure(true);
			userCookie.setDomain(AuthUtil.COOKIE_DOMAIN);
		}
		userCookie.setMaxAge(AuthUtil.SESSION_EXPIRY_SECONDS);
		userCookie.setPath("/");
		resp.addCookie(userCookie);
	}

	public static void clearUserCookie(HttpServletResponse resp) {
		Cookie userCookie = new Cookie(USER_COOKIE_NAME, "");
		userCookie.setMaxAge(0);
		userCookie.setPath("/");
		userCookie.setHttpOnly(true);
		if ("yes".equals(dbAuth.PROD)) {
			userCookie.setDomain(AuthUtil.COOKIE_DOMAIN);
			userCookie.setSecure(true);
		}
		resp.addCookie(userCookie);
	}
}
