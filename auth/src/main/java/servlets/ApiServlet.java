package servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import servlets.authentication.ForgotPassHandler;
import servlets.authentication.LoginHandler;
import servlets.authentication.LogoutHandler;
import servlets.profile.GetUserAdminProfileHandler;
import servlets.profile.GetUserHandler;
import servlets.profile.UpdateUserAdminProfileHandler;
import servlets.profile.UpdateUserProfileHandler;
import servlets.registration.ReVerifyHandler;
import servlets.registration.RegisterHandler;
import servlets.registration.VerifyHandler;
import servlets.security.GetUserSessionsHandler;
import servlets.security.RemoveUserSessionHandler;
import servlets.security.UpdatePassHandler;

@WebServlet("/v1/*")
public class ApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// Route definitions with HTTP method and handler
	private static final Map<String, Map<String, RouteHandler>> routes = new HashMap<>();

	static {
		// Authentication routes
		addRoute("POST", ApiConstants.LOGIN_URL, (req, resp, params) -> LoginHandler.loginUser(req, resp));
		addRoute("GET", ApiConstants.LOGOUT_URL, (req, resp, params) -> LogoutHandler.logoutUser(req, resp));
		addRoute("POST", ApiConstants.FORGOT_PASSWORD_URL,
				(req, resp, params) -> ForgotPassHandler.forgotPass(req, resp));

		// Profile routes
		addRoute("GET", ApiConstants.GET_USER_ADMIN_PROFILE_URL,
				(req, resp, params) -> GetUserAdminProfileHandler.getUserAdminProfile(req, resp));
		addRoute("GET", ApiConstants.GET_USER_URL, (req, resp, params) -> GetUserHandler.getUser(req, resp));
		addRoute("POST", ApiConstants.UPDATE_USER_ADMIN_PROFILE_URL,
				(req, resp, params) -> UpdateUserAdminProfileHandler.updateUserAdminProfile(req, resp));
		addRoute("POST", ApiConstants.UPDATE_USER_PROFILE_URL,
				(req, resp, params) -> UpdateUserProfileHandler.updateUserProfile(req, resp));

		// Registration routes
		addRoute("POST", ApiConstants.REGISTER_URL, (req, resp, params) -> RegisterHandler.registerUser(req, resp));
		addRoute("POST", ApiConstants.RE_VERIFY_URL, (req, resp, params) -> ReVerifyHandler.reVerifyUser(req, resp));
		addRoute("GET", ApiConstants.VERIFY_URL, (req, resp, params) -> VerifyHandler.verifyUser(req, resp));

		// Security routes with path parameters
		addRoute("GET", ApiConstants.GET_USER_SESSIONS_URL, (req, resp, params) -> {
			req.setAttribute("userId", params.get("userId"));
			GetUserSessionsHandler.getUserSessions(req, resp);
		});
		addRoute("POST", ApiConstants.REMOVE_USER_SESSION_URL, (req, resp, params) -> {
			req.setAttribute("userId", params.get("userId"));
			req.setAttribute("sessionId", params.get("sessionId"));
			RemoveUserSessionHandler.removeUserSession(req, resp);
		});
		addRoute("POST", ApiConstants.UPDATE_PASSWORD_URL, (req, resp, params) -> {
			req.setAttribute("userId", params.get("userId"));
			UpdatePassHandler.updateUserPass(req, resp);
		});
	}

	private static void addRoute(String method, String path, RouteHandler handler) {
		routes.computeIfAbsent(method, k -> new HashMap<>()).put(path, handler);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String method = req.getMethod();
		String path = req.getRequestURI();

		// Remove context path if present
		String contextPath = req.getContextPath();
		if (path.startsWith(contextPath)) {
			path = path.substring(contextPath.length());
		}

		Map<String, RouteHandler> methodRoutes = routes.get(method);
		if (methodRoutes == null) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}

		// Try exact match first
		RouteHandler handler = methodRoutes.get(path);
		if (handler != null) {
			handler.handle(req, resp, new HashMap<>());
			return;
		}

		// Try pattern matching for parameterized routes
		for (Map.Entry<String, RouteHandler> entry : methodRoutes.entrySet()) {
			String routePattern = entry.getKey();
			if (routePattern.contains("{")) {
				Map<String, String> params = matchRoute(routePattern, path);
				if (params != null) {
					entry.getValue().handle(req, resp, params);
					return;
				}
			}
		}

		resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	private Map<String, String> matchRoute(String routePattern, String actualPath) {
		// Convert route pattern to regex
		String regex = routePattern.replaceAll("\\{([^}]+)\\}", "([^/]+)");
		Pattern pattern = Pattern.compile("^" + regex + "$");
		Matcher matcher = pattern.matcher(actualPath);

		if (!matcher.matches()) {
			return null;
		}

		Map<String, String> params = new HashMap<>();
		Pattern paramPattern = Pattern.compile("\\{([^}]+)\\}");
		Matcher paramMatcher = paramPattern.matcher(routePattern);

		int groupIndex = 1;
		while (paramMatcher.find()) {
			String paramName = paramMatcher.group(1);
			String paramValue = matcher.group(groupIndex++);
			params.put(paramName, paramValue);
		}

		return params;
	}

	@FunctionalInterface
	private interface RouteHandler {
		void handle(HttpServletRequest req, HttpServletResponse resp, Map<String, String> pathParams)
				throws IOException, ServletException;
	}
}
