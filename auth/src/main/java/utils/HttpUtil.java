package utils;

import java.io.BufferedReader;
import java.io.IOException;

import org.json.JSONObject;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import entities.User;

public class HttpUtil {

	public static JSONObject readBodyJSON(HttpServletRequest req) {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = req.getReader()) {
			String line;
			while ((line = reader.readLine()) != null)
				sb.append(line);
		} catch (IOException e) {
			System.err.println("Unable to parse request body!");
			e.printStackTrace();
		}
		return new JSONObject(sb.toString());
	}

	public static void sendJson(HttpServletResponse resp, int statusCode, String type, String message)
			throws IOException {
		resp.setStatus(statusCode);
		resp.setContentType("application/json");
		JSONObject json = new JSONObject();
		json.put("type", type);
		json.put("message", message);
		resp.getWriter().write(json.toString());
	}

	public static void sendUser(HttpServletResponse resp, User user) throws IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("application/json");

		JSONObject json = new JSONObject();
		json.put("type", "success");
		json.put("uuid", user.uuid());
		json.put("email", user.email());
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
		if (user.metadata() != null) {
			json.put("metadata", user.metadata());
		}
		if (user.permissions() != null) {
			json.put("permissions", user.permissions());
		}

		resp.getWriter().write(json.toString());
	}

}
