package servlets;

/**
 * Constants for all API endpoints used in the auth application.
 * This replaces the old ServletHandler URL constants.
 */
public final class ApiConstants {

    private ApiConstants() {
        // Utility class - prevent instantiation
    }

    // --- Authentication URLs ---
    public static final String LOGIN_URL = "/v1/auth/login"; // POST
    public static final String LOGOUT_URL = "/v1/auth/logout"; // GET
    public static final String FORGOT_PASSWORD_URL = "/v1/auth/forgot-password"; // POST

    // --- Profile URLs ---
    public static final String GET_USER_ADMIN_PROFILE_URL = "/v1/profile/admin-profile"; // GET
    public static final String UPDATE_USER_ADMIN_PROFILE_URL = "/v1/profile/update-admin-metadata"; // POST
    public static final String GET_USER_URL = "/v1/profile/get-user"; // GET
    public static final String UPDATE_USER_PROFILE_URL = "/v1/profile/update-profile"; // POST

    // --- Registration URLs ---
    public static final String REGISTER_URL = "/v1/register/register-user"; // POST
    public static final String RE_VERIFY_URL = "/v1/register/re-verify"; // POST
    public static final String VERIFY_URL = "/v1/register/verify"; // GET

    // --- Security URLs (with path parameters) ---
    public static final String GET_USER_SESSIONS_URL = "/v1/users/{userId}/sessions"; // GET
    public static final String REMOVE_USER_SESSION_URL = "/v1/users/{userId}/sessions/{sessionId}"; // POST
    public static final String UPDATE_PASSWORD_URL = "/v1/users/{userId}/password"; // POST
    
}
