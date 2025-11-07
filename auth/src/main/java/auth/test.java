package auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class test  {
	private static final Logger log = LogManager.getLogger(test.class);

	public static void doGet(HttpServletRequest ignoredReq, HttpServletResponse ignoredResp, Map<String, String> ignoredParams) {
		log.info("\n\nInfo level log\n\n");
	}
}