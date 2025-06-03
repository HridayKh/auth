package servlets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

@WebServlet("/update_password")
public class Update extends HttpServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    Cookie[] cookies = req.getCookies();
    String jwtEnc = null;

    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("hriday_tech_auth_token".equals(cookie.getName())) {
          jwtEnc = cookie.getValue();
          break;
        }
      }
    }
    if (jwtEnc == null) {
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      resp.setContentType("application/json");
      resp.getWriter().write("{\"type\":\"error\",\"message\":\"Not logged in.\"}");
      return;
    }

    try (Connection conn = dbAuth.getConnection()) {
      byte[] decodedBytes = Base64.getDecoder().decode(jwtEnc);
      String jwt = new String(decodedBytes, StandardCharsets.UTF_8);

      // Split by your literal delimiter ":|:"
      String[] parts = jwt.split(":\\|:");
      if (parts.length != 2) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"type\":\"error\",\"message\":\"Invalid token format.\"}");
        return;
      }

      String uuid = parts[0];
      String sign = parts[1];

      // Verify signature
      if (!PassUtil.signUUID(uuid).equals(sign)) {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"type\":\"error\",\"message\":\"Invalid token signature.\"}");
        return;
      }
      String old = req.getParameter("old");
      String neW = req.getParameter("new");
      if (old == null || neW == null || old.isBlank() || old.isEmpty() || neW.isEmpty() || neW.isBlank()) {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"type\":\"error\",\"message\":\"Empty or null passwords!\"}");
        return;

      }
      String oldPass = PassUtil.sha256Hash(old);
      String newPass = PassUtil.sha256Hash(neW);
      User user = UsersDAO.getUserByUUID(uuid, conn);

      if (!oldPass.equals(user.password_hash())) {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"type\":\"error\",\"message\":\"Invalid Old Password!\"}");
        return;
      }

      if (!UsersDAO.updateUserPassword(conn, uuid, newPass)) {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"type\":\"error\",\"message\":\"Unknown error occured!!\"}");
        return;

      }

      resp.setStatus(HttpServletResponse.SC_OK);
      resp.setContentType("application/json");
      resp.getWriter().write("{\"type\":\"success\",\"message\":\"Password is updated!\"}");
      return;
    } catch (Exception e) {
      e.printStackTrace();
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.setContentType("application/json");
      resp.getWriter().write("{\"type\":\"error\",\"message\":\"Internal Server Error\"}");
      return;
    }
  }

}
