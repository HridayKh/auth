package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.util.Base64;

import auth.PassUtil;
import db.UsersDAO;
import db.dbAuth;
import entities.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class Login extends HttpServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    String email = req.getParameter("email");
    String pass = req.getParameter("pass");
    try (Connection conn = dbAuth.getConnection()) {
      User user = UsersDAO.getUser(email, PassUtil.sha256Hash(pass), conn);
      if (user == null) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.setContentType("application/json");
        resp.getWriter()
            .write("{\"type\":\"error\",\"message\":\"User with this email/password does not exist\"}");
        return;
      }
      if (!user.is_verified()) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.setContentType("application/json");
        resp.getWriter()
            .write("{\"type\":\"error\",\"message\":\"Please verify your email\"}");
        return;
      }
      String jwt = user.uuid() + ":|:" + PassUtil.signUUID(user.uuid());
      // make client jwt
      // return jwt
      resp.setStatus(HttpServletResponse.SC_OK);
      resp.setContentType("application/json");
      resp.getWriter()
          .write("{\"type\":\"success\",\"message\":\"Logged In Successfully!\"}");

      Cookie authCookie = new Cookie("hriday_tech_auth_token", Base64.getEncoder().encodeToString(jwt.getBytes()));
      if (System.getenv("prod").equals("yes")) {
        authCookie.setHttpOnly(true); // Can't be accessed by JavaScript
        authCookie.setSecure(true); // Only sent over HTTPS
        authCookie.setDomain("hriday.tech"); // Valid across all subdomains
      }
      authCookie.setMaxAge(60 * 60 * 24 * 7); // 7 days in seconds
      authCookie.setPath("/"); // Valid for all paths
      resp.addCookie(authCookie);
      return;
    } catch (Exception e) {
      e.printStackTrace();
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.setContentType("application/json");
      resp.getWriter()
          .write("{\"type\":\"error\",\"message\":\"Internal Server Error\"}");
      return;

    }
  }
}
