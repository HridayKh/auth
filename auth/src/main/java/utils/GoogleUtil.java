package utils;

import java.security.SecureRandom;
import java.util.Base64;

import org.json.JSONObject;

public class GoogleUtil {
	public static String generateSecureRandomString() {
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[32];
		random.nextBytes(bytes);
		String randomString = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		return PassUtil.signString(PassUtil.sha256Hash(randomString));
	}

	public static JSONObject genStateJson(String csrf, String redirect, String source) {
		JSONObject stateJson = new JSONObject();
		stateJson.put("csrf", csrf);
		stateJson.put("redirect", redirect);
		stateJson.put("source", source);
		return stateJson;
	}
}
