package auth;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import servlets.ApiConstants;
import utils.ApiKeyManager;
import utils.HttpUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@WebFilter("/v1/*")
public class ApiKeyFilter implements Filter {

	private static final String API_KEY_HEADER = "X-HridayKh-In-Auth-Key";
	private static final String CLIENT_ID_HEADER = "X-HridayKh-In-Client-ID";

	private final PathAccessControl pathAccessControl = new PathAccessControl();
	private final AuthenticationService authService = new AuthenticationService();

	@Override
	public void init(FilterConfig filterConfig) {
		System.out.println("ApiKeyFilter initialized");
	}

	@Override
	public void destroy() {
		System.out.println("ApiKeyFilter destroyed");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		try {
			String requestPath = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

			AccessType requiredAccess = pathAccessControl.getRequiredAccess(requestPath);

			AuthResult authResult = authService.authenticate(httpRequest, requiredAccess);

			if (authResult.isAuthenticated()) {
				request.setAttribute("clientId", authResult.getClientId());
				request.setAttribute("clientType", authResult.getClientType());
			} else {
				HttpUtil.sendJson(httpResponse, authResult.getStatusCode(), "error", authResult.getErrorMessage());
				return;
			}

		} catch (Exception e) {
			System.err.println("Authentication error: " + e.getMessage());
			HttpUtil.sendJson(httpResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Authentication service error");
		}

		chain.doFilter(request, response);
	}

	// ================================
	// Core Types and Result Classes
	// ================================

	private enum AccessType {
		PUBLIC, FRONTEND, BACKEND, ADMIN
	}

	private interface AuthenticationStrategy {
		AuthResult authenticate(HttpServletRequest request);
	}

	// ================================
	// Path Access Control
	// ================================

	private static class AuthResult {
		private final boolean authenticated;
		private final String clientId;
		private final String clientType;
		private final String errorMessage;
		private final int statusCode;

		private AuthResult(boolean authenticated, String clientId, String clientType, String errorMessage, int statusCode) {
			this.authenticated = authenticated;
			this.clientId = clientId;
			this.clientType = clientType;
			this.errorMessage = errorMessage;
			this.statusCode = statusCode;
		}

		public static AuthResult allowed(String clientId, String clientType) {
			return new AuthResult(true, clientId, clientType, null, 200);
		}

		public static AuthResult denied(String errorMessage) {
			return denied(errorMessage, HttpServletResponse.SC_UNAUTHORIZED);
		}

		public static AuthResult denied(String errorMessage, int statusCode) {
			return new AuthResult(false, null, null, errorMessage, statusCode);
		}

		public boolean isAuthenticated() {
			return authenticated;
		}

		public String getClientId() {
			return clientId;
		}

		public String getClientType() {
			return clientType;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public int getStatusCode() {
			return statusCode;
		}
	}

	// ================================
	// Authentication Service & Strategies
	// ================================

	private static class PathAccessControl {
		private final Set<String> publicPaths = Set.of();

		private final Set<String> frontendPaths = Set.of(ApiConstants.USERS_SESSIONS_CREATE, ApiConstants.USERS_SESSIONS_DELETE_CURRENT, ApiConstants.USERS_PASSWORD_RESET_INIT, ApiConstants.USERS_INFO_GET, ApiConstants.USERS_INFO_UPDATE, ApiConstants.USERS_CREATE, ApiConstants.USERS_VERIFY_EMAIL, ApiConstants.USERS_VERIFY_EMAIL_RESEND, ApiConstants.USERS_PASSWORD_UPDATE, ApiConstants.USERS_SESSIONS_LIST, ApiConstants.USERS_SESSION_DELETE);

		private final Set<String> backendPaths = Set.of(ApiConstants.USERS_INTERNAL_INFO_GET, ApiConstants.USERS_INTERNAL_INFO_UPDATE);

		private final Set<String> adminPaths = Set.of();

		public AccessType getRequiredAccess(String path) {
			if (matches(path, publicPaths)) return AccessType.PUBLIC;
			if (matches(path, frontendPaths)) return AccessType.FRONTEND;
			if (matches(path, adminPaths)) return AccessType.ADMIN;
			if (matches(path, backendPaths)) return AccessType.BACKEND;
			return AccessType.ADMIN;
		}

		private boolean matches(String path, Set<String> patterns) {
			return patterns.contains(path) || patterns.stream().anyMatch(pattern -> matchesPattern(path, pattern));
		}

		private boolean matchesPattern(String path, String pattern) {
			if (!pattern.contains("{")) return false;
			String regex = pattern.replaceAll("\\{[^}]+}", "[^/]+");
			return path.matches("^" + regex + "$");
		}
	}

	// ================================
	// Authentication Strategy Implementations
	// ================================

	private static class PublicAuthStrategy implements AuthenticationStrategy {
		@Override
		public AuthResult authenticate(HttpServletRequest request) {
			return AuthResult.allowed("public", "public");
		}
	}

	private static class FrontendAuthStrategy implements AuthenticationStrategy {
		@Override
		public AuthResult authenticate(HttpServletRequest request) {
			String clientId = request.getHeader(CLIENT_ID_HEADER);

			if (clientId != null && !clientId.trim().isEmpty()) {

				System.out.println(ApiKeyManager.API_KEY_TO_ROLE_MAP.toString());

				String role = ApiKeyManager.getRoleForApiKey(clientId);
				if (ApiKeyManager.ROLE_FRONTEND.equals(role)) {
					return AuthResult.allowed(clientId, "frontend");
				}
			}

			return AuthResult.denied("Valid client ID required", HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

	private static class BackendAuthStrategy implements AuthenticationStrategy {
		@Override
		public AuthResult authenticate(HttpServletRequest request) {
			String apiKey = request.getHeader(API_KEY_HEADER);
			String role = ApiKeyManager.getRoleForApiKey(apiKey);
			if (ApiKeyManager.ROLE_BACKEND.equals(role)) {
				return AuthResult.allowed(apiKey, "admin");
			}
			return AuthResult.denied("Valid API key required", HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

	private static class AdminAuthStrategy implements AuthenticationStrategy {
		@Override
		public AuthResult authenticate(HttpServletRequest request) {
			String apiKey = request.getHeader(API_KEY_HEADER);
			String role = ApiKeyManager.getRoleForApiKey(apiKey);
			if (ApiKeyManager.ROLE_ADMIN.equals(role)) {
				return AuthResult.allowed(apiKey, "admin");
			}
			return AuthResult.denied("Admin privileges required", HttpServletResponse.SC_FORBIDDEN);
		}
	}

	private static class AuthenticationService {
		private final Map<AccessType, AuthenticationStrategy> strategies = new HashMap<>();

		public AuthenticationService() {
			strategies.put(AccessType.PUBLIC, new PublicAuthStrategy());
			strategies.put(AccessType.FRONTEND, new FrontendAuthStrategy());
			strategies.put(AccessType.BACKEND, new BackendAuthStrategy());
			strategies.put(AccessType.ADMIN, new AdminAuthStrategy());
		}

		public AuthResult authenticate(HttpServletRequest request, AccessType accessType) {
			AuthenticationStrategy strategy = strategies.get(accessType);
			return strategy != null ? strategy.authenticate(request) : AuthResult.denied("Invalid access type");
		}
	}
}