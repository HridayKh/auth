package auth;

import db.dbAuth;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class CORSFilter implements Filter {
	private static final Logger log = LogManager.getLogger(CORSFilter.class);

	@Override
	public void init(FilterConfig filterConfig) {
		log.info("CORSFilter initialized.");
	}

	@Override
	public void destroy() {
		log.info("CORSFilter destroyed.");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {

		HttpServletResponse res = (HttpServletResponse) response;
		HttpServletRequest req = (HttpServletRequest) request;

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
//			String ip = req.getRemoteAddr();
//			int len = ip.length();
//			log.info("CORSFilter Hit by {} at {}", ip != null && len > 0 ? ip.substring(0, len/2) : "unknown IP", req.getRequestURI());
			log.info("CORSFilter Hit by {} at {}", req.getRemoteAddr(), req.getRequestURI());
			chain.doFilter(request, response);
		} catch (ServletException e) {
			log.catching(e);
		} catch (IOException e) {
			log.catching(e);
		} finally {
			res.setHeader("Access-Control-Allow-Origin", dbAuth.FRONT_HOST);
			res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
			res.setHeader("Access-Control-Allow-Headers", "Content-Type, X-HridayKh-In-Client-ID, Authorization");
			res.setHeader("Access-Control-Allow-Credentials", "true");
			res.setHeader("Access-Control-Max-Age", "3600");
		}
	}

}
