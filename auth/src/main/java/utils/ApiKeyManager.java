package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ApiKeyManager {

	// Define roles as constants for type safety and maintainability
	public static final String ROLE_BACKEND = "BACKEND";
	public static final String ROLE_FRONTEND = "FRONTEND";
	// A single map stores all valid keys and maps them to their role.
	public static final Map<String, String> API_KEY_TO_ROLE_MAP;
	private static final Logger log = LogManager.getLogger(ApiKeyManager.class);

	static {
		Map<String, String> keys = new HashMap<>();

		// Load keys for each role using a helper method
		loadKeysForRole(keys, "BACKEND_API_KEYS", ROLE_BACKEND);
		loadKeysForRole(keys, "FRONTEND_CLIENT_IDS", ROLE_FRONTEND);

		if (keys.isEmpty()) {
			String errorMessage = "CRITICAL: No API keys were loaded. Check environment variables.";
			log.fatal(errorMessage);
			throw new IllegalStateException(errorMessage);
		}

		API_KEY_TO_ROLE_MAP = Collections.unmodifiableMap(keys);
	}

	// Private constructor to prevent instantiation
	private ApiKeyManager() {
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
			log.warn("Environment variable for API keys '{}' is not set. No keys loaded for role: {}", envVar, role);
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
		log.info("Loaded {} API key(s) for role: {}", count, role);
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
}