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
        // Don't forward API/backend or static file requests
        if (
            path.startsWith("/v1/") ||
            path.startsWith("/googleLoginInitiate") ||
            path.startsWith("/oauth2callback") ||
            path.contains(".") // crude check for static files (js, css, png, etc)
        ) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.setContentType("text/plain");
            resp.getWriter().write("Not forwarded by FrontControllerServlet: " + path);
            return;
        }
        req.getRequestDispatcher("/index.html").forward(req, resp);
    }
}
