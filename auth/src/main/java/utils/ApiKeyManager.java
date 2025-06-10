package utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ApiKeyManager {

	private static final Map<String, String> VALID_API_KEYS;

	static {
		Map<String, String> keys = new HashMap<>();
		String myWebAppKey = System.getenv("MY_WEB_APP_AUTH_API_KEY");
		String myMobileAppKey = System.getenv("MY_MOBILE_APP_AUTH_API_KEY");
		String myAdminPanelKey = System.getenv("MY_ADMIN_PANEL_AUTH_API_KEY");

		if (myWebAppKey != null && !myWebAppKey.isEmpty()) {
			keys.put("my-web-app", myWebAppKey);
		} else {
			System.err.println("MY_WEB_APP_AUTH_API_KEY environment variable not set!");
			// Consider throwing an exception or making the app fail startup if keys are
			// mandatory.
		}
		if (myMobileAppKey != null && !myMobileAppKey.isEmpty()) {
			keys.put("my-mobile-app", myMobileAppKey);
		} else {
			System.err.println("MY_MOBILE_APP_AUTH_API_KEY environment variable not set!");
		}
		if (myAdminPanelKey != null && !myAdminPanelKey.isEmpty()) {
			keys.put("my-admin-panel", myAdminPanelKey);
		} else {
			System.err.println("MY_ADMIN_PANEL_AUTH_API_KEY environment variable not set!");
		}

		VALID_API_KEYS = Collections.unmodifiableMap(keys); // Make it immutable
	}

	/**
	 * Validates an incoming API key.
	 * 
	 * @param apiKey The raw API key received from the client.
	 * @return The client ID (e.g., "my-web-app") if valid, otherwise null.
	 */
	public static String validateApiKey(String apiKey) {
		if (apiKey == null || apiKey.isEmpty()) {
			return null;
		}
		// Iterate through known keys to find a match
		for (Map.Entry<String, String> entry : VALID_API_KEYS.entrySet()) {
			if (apiKey.equals(entry.getValue())) {
				return entry.getKey(); // Return the client ID
			}
		}
		return null; // No matching key found
	}

	/**
	 * Returns a list of API keys that are allowed for "admin-only" operations. For
	 * example, only "my-admin-panel" might be allowed to call updateAdminProfile.
	 * This is a simple way to manage granular permissions if needed.
	 */
	public static boolean isAdminApp(String clientId) {
		// Define which client IDs have "admin" privileges for certain endpoints
		return "my-admin-panel".equals(clientId);
	}
}