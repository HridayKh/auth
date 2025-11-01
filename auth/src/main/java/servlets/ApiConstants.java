package servlets;

/**
 * Constants for all API endpoints used in the auth application.
 * This replaces the old ServletHandler URL constants.
 */
public final class ApiConstants {

	public static final String USERS_CREATE = "/v1/users"; // POST

	public static final String USERS_INFO_GET = "/v1/users/me"; // GET
	public static final String USERS_INFO_UPDATE = "/v1/users/me"; // PATCH
	public static final String USERS_PASSWORD_UPDATE = "/v1/users/me/password"; // POST

	public static final String USERS_INTERNAL_INFO_GET = "/v1/users/internal/me"; // GET
	public static final String USERS_INTERNAL_INFO_UPDATE = "/v1/users/internal/me"; // PATCH

	// `USERS_EMAIL_VERIFY` to be opened directly in browser by user
	public static final String USERS_VERIFY_EMAIL = "/v1/users/verify"; // GET
	public static final String USERS_VERIFY_EMAIL_RESEND = "/v1/users/verify/resend"; // POST

	public static final String USERS_PASSWORD_RESET_INIT = "/v1/users/password-resets"; // POST
	public static final String USERS_PASSWORD_RESET_UPDATE = "/v1/users/password-resets"; // PUT

	public static final String USERS_SESSIONS_LIST = "/v1/users/sessions/me"; // GET
	public static final String USERS_SESSIONS_CREATE = "/v1/users/sessions"; // POST
	public static final String USERS_SESSION_DELETE = "/v1/users/sessions/{sessionId}"; // DELETE
	public static final String USERS_SESSIONS_DELETE_CURRENT = "/v1/users/sessions/current"; // DELETE

	private ApiConstants() {
		// Utility class - prevent instantiation
	}

}
