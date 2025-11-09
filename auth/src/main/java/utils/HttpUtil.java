package utils;

import entities.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;

public class HttpUtil {

	public static JSONObject readBodyJSON(HttpServletRequest req) {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = req.getReader()) {
			String line;
			while ((line = reader.readLine()) != null)
				sb.append(line);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.length() > 0 ? new JSONObject(sb.toString()) : new JSONObject();
	}

	public static void sendJson(HttpServletResponse resp, int status, String type, String message)
		throws IOException {
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

		// Basic fields
		json.put("type", "success");
		json.put("message", "User fetched successfully");

		// User details
		json.put("uuid", user.uuid()); // not nullable
		json.put("email", user.email().toLowerCase()); // not nullable
		json.put("is_verified", user.isVerified());
		json.put("created_at", user.createdAt()); // not nullable
		json.put("updated_at", user.updatedAt()); // not nullable
		json.put("last_login", user.lastLogin());

		json.put("accType", user.accType());

		json.put("profile_pic", user.profilePic());
		json.put("full_name", user.fullName());
		json.put("metadata", user.metadata());
		json.put("permissions", user.permissions());

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

}
