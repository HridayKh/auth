package auth;

import io.sentry.Sentry;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@WebServlet("/test")
public class test extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LogManager.getLogger(test.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		log.trace("Trace level log");
		log.debug("Debug level log");
		log.info("Info level log");
		log.warn("Warn level log");
		log.error("Error level log");
		log.fatal("Fatal level log");
		try {
			throw new Exception("a This is a test.");
		} catch (Exception e) {
			Sentry.captureException(e);
		}
	}
}