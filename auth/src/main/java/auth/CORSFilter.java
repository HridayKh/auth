package auth;

import db.dbAuth;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/*")
public class CORSFilter implements Filter {

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletResponse res = (HttpServletResponse) response;
		HttpServletRequest req = (HttpServletRequest) request;

		// Always set CORS headers, even on error
		res.setHeader("Access-Control-Allow-Origin", dbAuth.FRONT_HOST);
		res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
		res.setHeader("Access-Control-Allow-Headers", "Content-Type, X-HridayKh-In-Client-ID, Authorization");
		res.setHeader("Access-Control-Allow-Credentials", "true");
		res.setHeader("Access-Control-Max-Age", "3600");

		if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
			res.setStatus(HttpServletResponse.SC_OK);
			return;
		}

		try {
			chain.doFilter(request, response);
		} finally {
			res.setHeader("Access-Control-Allow-Origin", dbAuth.FRONT_HOST);
			res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
			res.setHeader("Access-Control-Allow-Headers",
					"Content-Type, X-HridayKh-In-Client-ID, Authorization");
			res.setHeader("Access-Control-Allow-Credentials", "true");
			res.setHeader("Access-Control-Max-Age", "3600");
		}
	}

	@Override
	public void destroy() {
	}
}
