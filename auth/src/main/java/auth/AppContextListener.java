package auth;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppContextListener implements ServletContextListener {

	private static final Logger log = LogManager.getLogger(AppContextListener.class);

	public void contextInitialized(ServletContextEvent sce) {
		log.info("Application context initialized.");
	}

	public void contextDestroyed(ServletContextEvent sce) {
		log.info("Initiate Application context destruction.");
		db.dbAuth.shutdown();
		log.info("Application context destroyed.");
	}
}
