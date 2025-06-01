package servlets;

import java.io.IOException;
import java.sql.Connection;

import auth.PassUtil;
import db.UsersDAO;
import db.dbAuth;
import entities.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
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
      String jwt = "{\"uuid\":\"" + user.uuid() + "\",\"sign\":\"" + PassUtil.signUUID(user.uuid()) + "\"}";
      // make client jwt
      // return jwt
      resp.setStatus(HttpServletResponse.SC_OK);
      resp.setContentType("application/json");
      resp.getWriter()
          .write("{\"type\":\"success\",\"message\":\"Logged In Successfully!\",\"jwt\":" + jwt + "}");
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
