package auth;

import java.io.IOException;
import java.util.Set;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import servlets.ServletHandler; // To get URL constants
import utils.ApiKeyManager;
import utils.HttpUtil; // Assuming you have this for sending error responses

// Apply this filter to all /v1/* URLs where your API endpoints are
@WebFilter("/v1/*")
public class ApiKeyFilter implements Filter {

	private static final String API_KEY_HEADER_NAME = "X-Hriday-Tech-App-Key";
	private static final String CLIENT_ID_HEADER_NAME = "X-Hriday-Tech-Client-ID";

	private static final Set<String> PUBLIC_PATHS = Set.of(
	// ServletHandler.HealthCheckURL // Example: if you had a public health check
	);

	private static final Set<String> IDENTIFIED_FRONTEND_PATHS = Set.of(ServletHandler.LoginURL,
			ServletHandler.RegisterURL, ServletHandler.VerifyURL, ServletHandler.ReVerifyURL, ServletHandler.LogoutURL,
			ServletHandler.GetUserURL, // User gets their own profile
			ServletHandler.UpdateUserProfileURL, // User updates their own profile
			ServletHandler.UpdatePassURL, // User updates their own password
			ServletHandler.GetUserSessionsURL, // User gets their own sessions
			ServletHandler.RemoveUserSessionURL // User removes their own session
	);

	private static final Set<String> SECRET_API_KEY_PATHS = Set.of(ServletHandler.GetUserAdminProfileURL,
			ServletHandler.UpdateUserAdminProfileURL);

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

		// --- 1. Handle Public Paths ---
		if (PUBLIC_PATHS.contains(path)) {
			chain.doFilter(request, response);
			return;
		}

		String secretApiKey = httpRequest.getHeader(API_KEY_HEADER_NAME);
		String backendClientId = null;
		if (secretApiKey != null) {
			backendClientId = ApiKeyManager.validateApiKey(secretApiKey);
		}

		if (backendClientId != null) {
			httpRequest.setAttribute("clientId", backendClientId);
			httpRequest.setAttribute("clientType", "backend");

			if (SECRET_API_KEY_PATHS.contains(path) && !ApiKeyManager.isAdminApp(backendClientId)) {
				HttpUtil.sendJson(httpResponse, HttpServletResponse.SC_FORBIDDEN, "error",
						"Forbidden: Insufficient application privileges.");
				return;
			}
			chain.doFilter(request, response);
			return;
		}

		// --- 3. Check for Public Client ID (from frontend apps) ---
		// This code only runs if no valid SECRET_API_KEY was found
		String publicClientId = httpRequest.getHeader(CLIENT_ID_HEADER_NAME);

		if (IDENTIFIED_FRONTEND_PATHS.contains(path)) {
			if (publicClientId != null && !publicClientId.isEmpty()) {
				// This is just for identification, not validation.
				// You might want a whitelist of known publicClientIds if strict.
				httpRequest.setAttribute("clientId", publicClientId);
				httpRequest.setAttribute("clientType", "frontend");
				chain.doFilter(request, response);
				return;
			} else {
				// For identified frontend paths, a client ID might be mandatory.
				// Decide if you want to allow requests without it or deny.
				HttpUtil.sendJson(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "error",
						"Unauthorized: Client ID required for frontend access.");
				return;
			}
		}

		// --- 4. If none of the above conditions met, it's an unauthorized access ---
		// This means it's a path that expects a secret API key, but none was provided
		// or valid.
		// Or it's an unidentified frontend access to a path that needs identification.
		HttpUtil.sendJson(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "error",
				"Unauthorized: Invalid or missing API Key/Client ID.");
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		System.out.println("ApiKeyFilter initialized.");
	}

	@Override
	public void destroy() {
		System.out.println("ApiKeyFilter destroyed.");
	}
}