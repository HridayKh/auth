package utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class ApiKeyManager {

	private static final Logger LOGGER = Logger.getLogger(ApiKeyManager.class.getName());

	// Define roles as constants for type safety and maintainability
	public static final String ROLE_BACKEND = "BACKEND";
	public static final String ROLE_FRONTEND = "FRONTEND";

	// A single map stores all valid keys and maps them to their role.
	public static final Map<String, String> API_KEY_TO_ROLE_MAP;

	static {
		Map<String, String> keys = new HashMap<>();

		// Load keys for each role using a helper method
		loadKeysForRole(keys, "BACKEND_API_KEYS", ROLE_BACKEND);
		loadKeysForRole(keys, "FRONTEND_CLIENT_IDS", ROLE_FRONTEND);

		if (keys.isEmpty()) {
			String errorMessage = "CRITICAL: No API keys were loaded. Check environment variables.";
			LOGGER.severe(errorMessage);
			throw new IllegalStateException(errorMessage);
		}

		API_KEY_TO_ROLE_MAP = Collections.unmodifiableMap(keys);
	}

	/**
	 * Reads a comma-separated list of API keys from an environment variable and
	 * maps each key to the specified role.
	 *
	 * @param map    The map to populate.
	 * @param envVar The name of the environment variable (e.g., "ADMIN_API_KEYS").
	 * @param role   The role to assign to these keys (e.g., ROLE_ADMIN).
	 */
	private static void loadKeysForRole(Map<String, String> map, String envVar, String role) {
		String keysCsv = System.getenv(envVar);
		if (keysCsv == null || keysCsv.trim().isEmpty()) {
			// This is now treated as a warning, not a critical failure,
			// unless NO keys are loaded at all.
			LOGGER.warning(
					"Environment variable for API keys '" + envVar + "' is not set. No keys loaded for role: " + role);
			return;
		}

		int count = 0;
		String[] apiKeys = keysCsv.split(",");
		for (String apiKey : apiKeys) {
			String trimmedKey = apiKey.trim();
			if (!trimmedKey.isEmpty()) {
				map.put(trimmedKey, role);
				count++;
			}
		}
		LOGGER.info("Loaded " + count + " API key(s) for role: " + role);
	}

	/**
	 * Validates an API key and returns its associated role.
	 *
	 * @param apiKey The raw API key from the client.
	 * @return The role (e.g., "ADMIN", "USER") if the key is valid, otherwise null.
	 */
	public static String getRoleForApiKey(String apiKey) {
		if (apiKey == null || apiKey.isEmpty()) {
			return null;
		}
		return API_KEY_TO_ROLE_MAP.get(apiKey);
	}

	// Private constructor to prevent instantiation
	private ApiKeyManager() {
	}
}