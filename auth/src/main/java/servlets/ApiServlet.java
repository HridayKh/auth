package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import servlets.authentication.ForgotPassHandler;
import servlets.authentication.LoginHandler;
import servlets.authentication.LogoutHandler;
import servlets.profile.GetUserHandler;
import servlets.profile.GetUserInternalHandler;
import servlets.profile.UpdateUserInternalHandler;
import servlets.profile.UpdateUserProfileHandler;
import servlets.registration.ReVerifyHandler;
import servlets.registration.RegisterHandler;
import servlets.registration.VerifyHandler;
import servlets.security.GetUserSessionsHandler;
import servlets.security.RemoveUserSessionHandler;
import servlets.security.UpdatePassHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/v1/*")
public class ApiServlet extends HttpServlet {

	private static final Map<String, Map<String, RouteHandler>> routes = new HashMap<>();

	static {
		addRoute("POST", ApiConstants.USERS_CREATE, RegisterHandler::registerUser);

		addRoute("GET", ApiConstants.USERS_INFO_GET, GetUserHandler::getUser);
		addRoute("PATCH", ApiConstants.USERS_INFO_UPDATE, UpdateUserProfileHandler::updateUserProfile);
		addRoute("POST", ApiConstants.USERS_PASSWORD_UPDATE, UpdatePassHandler::updateUserPass);

		addRoute("GET", ApiConstants.USERS_INTERNAL_INFO_GET, GetUserInternalHandler::getUserAdminProfile);
		addRoute("PATCH", ApiConstants.USERS_INTERNAL_INFO_UPDATE, UpdateUserInternalHandler::updateUserAdminProfile);

		addRoute("GET", ApiConstants.USERS_EMAIL_VERIFY, VerifyHandler::verifyUser);
		addRoute("POST", ApiConstants.USERS_EMAIL_VERIFY_RESEND, ReVerifyHandler::reVerifyUser);

		addRoute("POST", ApiConstants.USERS_PASSWORD_RESET_INIT, ForgotPassHandler::forgotPass);
		addRoute("PUT", ApiConstants.USERS_PASSWORD_RESET_UPDATE, ForgotPassHandler::forgotPass);

		addRoute("GET", ApiConstants.USERS_SESSIONS_LIST, GetUserSessionsHandler::getUserSessions);
		addRoute("POST", ApiConstants.USERS_SESSIONS_CREATE, LoginHandler::loginUser);
		addRoute("DELETE", ApiConstants.USERS_SESSION_DELETE, RemoveUserSessionHandler::removeUserSession);
		addRoute("DELETE", ApiConstants.USERS_SESSIONS_DELETE_CURRENT, LogoutHandler::logoutUser);
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
		void handle(HttpServletRequest req, HttpServletResponse resp, Map<String, String> pathParams) throws IOException, ServletException;
	}
}
