package auth;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.sentry.Sentry;

public class AppContextListener implements ServletContextListener {

	private static final Logger log = LogManager.getLogger(AppContextListener.class);

	public void contextInitialized(ServletContextEvent sce) {
		Sentry.init(options -> {
			options.setDsn("https://6f8c6e7ce86811df962456b9e68d1834@o4509022198431744.ingest.de.sentry.io/4509818434551888");
			options.setTracesSampleRate(1.0);
			options.setDebug(true);
		});
		log.info("Application context initialized.");
	}

	public void contextDestroyed(ServletContextEvent sce) {
		log.info("Initiate Application context destruction.");
		db.dbAuth.shutdown();
		log.info("Application context destroyed.");
	}
}
