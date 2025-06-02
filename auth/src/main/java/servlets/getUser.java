package servlets;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.Base64;

import auth.PassUtil;
import db.UsersDAO;
import db.dbAuth;
import entities.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/getUser")
public class getUser extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

      // Fetch user info by UUID
      User user = UsersDAO.getUserByUUID(uuid, conn);
      if (user == null) {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"type\":\"error\",\"message\":\"User not found.\"}");
        return;
      }

      // Prepare user info JSON (example, customize as needed)
      String userJson = String.format(
          "{\"uuid\":\"%s\",\"email\":\"%s\"}", user.uuid(), user.email());

      resp.setStatus(HttpServletResponse.SC_OK);
      resp.setContentType("application/json");
      resp.getWriter().write(userJson);

    } catch (Exception e) {
      e.printStackTrace();
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      resp.setContentType("application/json");
      resp.getWriter().write("{\"type\":\"error\",\"message\":\"Internal Server Error\"}");
    }
  }
}
