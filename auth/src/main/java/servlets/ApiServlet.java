package servlets;

import auth.test;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import servlets.userPasswords.UsersPassReset;
import servlets.userPasswords.UsersPassUpdater;
import servlets.userSessions.UsersSessionCreate;
import servlets.userSessions.UsersSessionDelete;
import servlets.userSessions.UsersSessionDeleteCurrent;
import servlets.userSessions.UsersSessionList;
import servlets.usersCreate.UsersCreator;
import servlets.usersCreate.UsersVerifier;
import servlets.usersInfo.UsersInfoManager;
import servlets.usersInfo.UsersInternalManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/v1/*")
public class ApiServlet extends HttpServlet {
	private static final Logger log = LogManager.getLogger(ApiServlet.class);

	private static final Map<String, Map<String, RouteHandler>> routes = new HashMap<>();

	static {
		addRoute("GET", "/v1/test", test::doGet);
		// UsersCreate
		addRoute("POST", ApiConstants.USERS_CREATE, UsersCreator::createUser);

		addRoute("GET", ApiConstants.USERS_VERIFY_EMAIL, UsersVerifier::verifyUser);
		addRoute("POST", ApiConstants.USERS_VERIFY_EMAIL_RESEND, UsersVerifier::resendVerifyEmail);

		// UsersInfo
		addRoute("GET", ApiConstants.USERS_INFO, UsersInfoManager::getUserInfo);
		addRoute("PATCH", ApiConstants.USERS_INFO, UsersInfoManager::updateUserInfo);

		addRoute("GET", ApiConstants.USERS_INTERNAL_INFO, UsersInternalManager::getUserInternalInfo);
		addRoute("PATCH", ApiConstants.USERS_INTERNAL_INFO, UsersInternalManager::updateUserInternalInfo);

		// UsersPasswords
		addRoute("POST", ApiConstants.USERS_PASSWORD_RESET, UsersPassReset::initReset);
		addRoute("PUT", ApiConstants.USERS_PASSWORD_RESET, UsersPassReset::completeReset);
		addRoute("POST", ApiConstants.USERS_PASSWORD_UPDATE, UsersPassUpdater::updateUserPass);

		// UsersSessions
		addRoute("GET", ApiConstants.USERS_SESSIONS_LIST, UsersSessionList::listUserSessions);
		addRoute("POST", ApiConstants.USERS_SESSIONS_CREATE, UsersSessionCreate::createUserSession);
		addRoute("DELETE", ApiConstants.USERS_SESSION_DELETE, UsersSessionDelete::deleteUserSession);
		addRoute("DELETE", ApiConstants.USERS_SESSIONS_DELETE_CURRENT, UsersSessionDeleteCurrent::deleteCurrentUserSession);

		addRoute("DELETE", ApiConstants.USERS_UNLINK_GOOGLE, UnlinkGoggle::unlinkGoogleAccount);

		log.info("API Servlet started with {} routes", routes.size());
	}

	private static void addRoute(String method, String path, RouteHandler handler) {
		routes.computeIfAbsent(method, k -> new HashMap<>()).put(path, handler);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) {
		String method = req.getMethod();
		String path = req.getRequestURI();

		String contextPath = req.getContextPath();
		if (path.startsWith(contextPath)) {
			path = path.substring(contextPath.length());
		}

		try {
			Map<String, RouteHandler> methodRoutes = routes.get(method);
			if (methodRoutes == null) {
				resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
				return;
			}

			RouteHandler handler = methodRoutes.get(path);
			if (handler != null) {
				log.info("Matched exact route: {}", path);
				handler.handle(req, resp, new HashMap<>());
				return;
			}

			for (Map.Entry<String, RouteHandler> entry : methodRoutes.entrySet()) {
				String routePattern = entry.getKey();
				if (routePattern.contains("{")) {
					Map<String, String> params = matchRoute(routePattern, path);
					if (params != null) {
						log.info("Matched route: {} with params {}", routePattern, params);
						entry.getValue().handle(req, resp, params);
						return;
					}
				}
			}
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		} catch (IOException e) {
			log.catching(e);
		}
	}

	private Map<String, String> matchRoute(String routePattern, String actualPath) {
		// Convert route pattern to regex
		String regex = routePattern.replaceAll("\\{([^}]+)}", "([^/]+)");
		Pattern pattern = Pattern.compile("^" + regex + "$");
		Matcher matcher = pattern.matcher(actualPath);

		if (!matcher.matches()) {
			return null;
		}

		Map<String, String> params = new HashMap<>();
		Pattern paramPattern = Pattern.compile("\\{([^}]+)}");
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
		void handle(HttpServletRequest req, HttpServletResponse resp, Map<String, String> pathParams) throws IOException;
	}
}
