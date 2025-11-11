package auth;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/*")
public class FrontControllerServlet extends HttpServlet {
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       String path = req.getRequestURI().substring(req.getContextPath().length());
       // Let Tomcat serve all static files (with a dot in the path)
       if (path.contains(".")) {
           req.getServletContext().getNamedDispatcher("default").forward(req, resp);
           return;
       }
       // Block API/backend paths
       if (
           path.startsWith("/v1/") ||
           path.startsWith("/googleLoginInitiate") ||
           path.startsWith("/oauth2callback")
       ) {
           resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
           resp.setContentType("text/plain");
           resp.getWriter().write("Not forwarded by FrontControllerServlet: " + path);
           return;
       }
       // If the path starts with /auth/, strip it for React Router (browser URL stays the same)
       if (path.startsWith("/auth/")) {
           String strippedPath = path.substring("/auth".length());
           if (strippedPath.isEmpty()) strippedPath = "/";
           req.setAttribute("frontendPath", strippedPath);
           req.getRequestDispatcher("/index.html").forward(req, resp);
           return;
       }
       req.setAttribute("frontendPath", path);
       req.getRequestDispatcher("/index.html").forward(req, resp);
   }

}
