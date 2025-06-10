package servlets;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import servlets.authentication.LoginHandler;
import servlets.authentication.LogoutHandler;
import servlets.profile.GetUserHandler;
import servlets.registration.ReVerifyHandler;
import servlets.registration.RegisterHandler;
import servlets.registration.VerifyHandler;
import servlets.security.UpdatePassHandler;

public class ServletHandler {

	public static final String RegisterURL = "/v1/register";
	public static final String VerifyURL = "/v1/verify";
	public static final String ReVerifyURL = "/v1/reVerify";
	public static final String LoginURL = "/v1/login";
	public static final String GetUserURL = "/v1/getUser";
	public static final String UpdateURL = "/v1/update_password";
	public static final String LogoutURL = "/v1/logout";

	@WebServlet(RegisterURL)
	public static class Register extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
			RegisterHandler.registerUser(req, resp);
		}
	}

	@WebServlet(VerifyURL)
	public static class Verify extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
			VerifyHandler.verifyUser(req, resp);
		}
	}

	@WebServlet(ReVerifyURL)
	public static class ReVerify extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
			ReVerifyHandler.reVerifyUser(req, resp);
		}
	}

	@WebServlet(LoginURL)
	public static class Lgin extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
			LoginHandler.loginUser(req, resp);
		}
	}

	@WebServlet(GetUserURL)
	public static class GetUser extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
			GetUserHandler.getUser(req, resp);
		}
	}

	@WebServlet(LogoutURL)
	public static class Logout extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
			LogoutHandler.logoutUser(req, resp);
		}

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
			doPost(req, resp);
		}
	}

	@WebServlet(UpdateURL)
	public static class Update extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
			UpdatePassHandler.updateUserPass(req, resp);
		}
	}

}
