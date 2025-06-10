package servlets;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

public class ServletHandler {

	// --- Authentication URLs ---
	public static final String LoginURL = "/v1/auth/login"; // POST
	public static final String LogoutURL = "/v1/auth/logout"; // GET

	// --- Profile URLs ---
	public static final String GetUserAdminProfileURL = "/v1/profile/admin-profile"; // GET
	public static final String GetUserURL = "/v1/profile/get-user"; // GET
	public static final String UpdateUserAdminProfileURL = "/v1/profile/update-admin-metadata"; // POST
	public static final String UpdateUserProfileURL = "/v1/profile/update-profile"; // POST

	// --- Registration URLs ---
	public static final String RegisterURL = "/v1/register/register-user"; // POST
	public static final String ReVerifyURL = "/v1/register/re-verify"; // POST
	public static final String VerifyURL = "/v1/register/verify"; // GET

	// --- Security URLs ---
	public static final String GetUserSessionsURL = "/v1/users/{userId}/sessions"; // GET
	public static final String RemoveUserSessionURL = "/v1/users/{userId}/sessions/{sessionId}"; // POST
	public static final String UpdatePassURL = "/v1/users/{userId}/password"; // POST

	public static class Authentication {

		@WebServlet(ServletHandler.LoginURL)
		public static class Login extends HttpServlet {
			private static final long serialVersionUID = 1L;

			@Override
			protected void doPost(HttpServletRequest req, HttpServletResponse resp)
					throws IOException, ServletException {
				LoginHandler.loginUser(req, resp);
			}
		}

		@WebServlet(ServletHandler.LogoutURL)
		public static class Logout extends HttpServlet {
			private static final long serialVersionUID = 1L;

			@Override
			protected void doGet(HttpServletRequest req, HttpServletResponse resp)
					throws IOException, ServletException {
				LogoutHandler.logoutUser(req, resp);
			}
		}
	}

	public static class Profile {

		@WebServlet(ServletHandler.GetUserAdminProfileURL)
		public static class GetUserAdminProfile extends HttpServlet {
			private static final long serialVersionUID = 1L;

			@Override
			protected void doGet(HttpServletRequest req, HttpServletResponse resp)
					throws IOException, ServletException {
				GetUserAdminProfileHandler.getUserAdminProfile(req, resp);
			}
		}

		@WebServlet(ServletHandler.GetUserURL)
		public static class GetUser extends HttpServlet {
			private static final long serialVersionUID = 1L;

			@Override
			protected void doGet(HttpServletRequest req, HttpServletResponse resp)
					throws IOException, ServletException {
				GetUserHandler.getUser(req, resp);
			}
		}

		@WebServlet(ServletHandler.UpdateUserAdminProfileURL)
		public static class UpdateUserAdminProfile extends HttpServlet {
			private static final long serialVersionUID = 1L;

			@Override
			protected void doPost(HttpServletRequest req, HttpServletResponse resp)
					throws IOException, ServletException {
				UpdateUserAdminProfileHandler.updateUserAdminProfile(req, resp);
			}
		}

		@WebServlet(ServletHandler.UpdateUserProfileURL)
		public static class UpdateUserProfile extends HttpServlet {
			private static final long serialVersionUID = 1L;

			@Override
			protected void doPost(HttpServletRequest req, HttpServletResponse resp)
					throws IOException, ServletException {
				UpdateUserProfileHandler.updateUserProfile(req, resp);
			}
		}
	}

	public static class Registration {

		@WebServlet(ServletHandler.RegisterURL)
		public static class Register extends HttpServlet {
			private static final long serialVersionUID = 1L;

			@Override
			public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
				RegisterHandler.registerUser(req, resp);
			}
		}

		@WebServlet(ServletHandler.ReVerifyURL)
		public static class ReVerify extends HttpServlet {
			private static final long serialVersionUID = 1L;

			@Override
			protected void doPost(HttpServletRequest req, HttpServletResponse resp)
					throws IOException, ServletException {
				ReVerifyHandler.reVerifyUser(req, resp);
			}
		}

		@WebServlet(ServletHandler.VerifyURL)
		public static class Verify extends HttpServlet {
			private static final long serialVersionUID = 1L;

			@Override
			public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
				VerifyHandler.verifyUser(req, resp);
			}
		}

	}

	public static class Security {

		@WebServlet(ServletHandler.GetUserSessionsURL)
		public static class GetUserSessions extends HttpServlet {
			private static final long serialVersionUID = 1L;

			@Override
			protected void doGet(HttpServletRequest req, HttpServletResponse resp)
					throws IOException, ServletException {
				GetUserSessionsHandler.getUserSessions(req, resp);
			}
		}

		@WebServlet(ServletHandler.RemoveUserSessionURL)
		public static class RemoveUserSession extends HttpServlet {
			private static final long serialVersionUID = 1L;

			@Override
			protected void doPost(HttpServletRequest req, HttpServletResponse resp)
					throws IOException, ServletException {
				RemoveUserSessionHandler.removeUserSession(req, resp);
			}
		}

		@WebServlet(ServletHandler.UpdatePassURL)
		public static class UpdatePass extends HttpServlet {
			private static final long serialVersionUID = 1L;

			@Override
			protected void doPost(HttpServletRequest req, HttpServletResponse resp)
					throws IOException, ServletException {
				UpdatePassHandler.updateUserPass(req, resp);
			}
		}
	}

}
