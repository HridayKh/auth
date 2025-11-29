package db;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class dbAuth  {
	public final static String DB_URL = "jdbc:mysql://db.hridaykh.in:3306/Auth_Db";
	public final static String DB_USER = System.getenv("AUTH_DB_USER");
	public final static String DB_PASSWORD = System.getenv("AUTH_DB_PASSWORD");

	public static final String CLIENT_ID = System.getenv("GOOGLE_CLIENT_ID");
	public static final String CLIENT_SECRET = System.getenv("GOOGLE_CLIENT_SECRET");

	public final static String BACK_HOST = System.getenv("VITE_AUTH_BACKEND");
	public final static String FRONT_HOST = System.getenv("VITE_AUTH_FRONTEND");

	public final static String Mailgun = System.getenv("MAILGUN_KEY");
	public final static String PROD = System.getenv("VITE_PROD");

	private static final Logger log = LogManager.getLogger(dbAuth.class);

	private static final HikariDataSource dataSource;
	static {
		HikariConfig config = new HikariConfig();
		// config.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
		config.setDriverClassName("com.mysql.cj.jdbc.Driver");
		config.setJdbcUrl(DB_URL);
		config.setUsername(DB_USER);
		config.setPassword(DB_PASSWORD);

		config.setMaximumPoolSize(10);
		config.setMinimumIdle(5);

		config.setPoolName("SecretsHikariCP");
		dataSource = new HikariDataSource(config);
		log.info("Database connection pool initiated.");
	}

	public static Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	public static void shutdown() {
		if (dataSource != null && !dataSource.isClosed())
			dataSource.close();
		log.info("Database connection pool shut down.");
	}
}
