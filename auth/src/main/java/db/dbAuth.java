package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class dbAuth {
	public final static String DB_URL = "jdbc:mysql://db.hriday.tech:3306/Auth_Db";
	public final static String DB_USER = System.getenv("AUTH_DB_USER");
	public final static String DB_PASSWORD = System.getenv("AUTH_DB_PASSWORD");
	
	public static final String CLIENT_ID = System.getenv("GOOGLE_CLIENT_ID");
	public static final String CLIENT_SECRET = System.getenv("GOOGLE_CLIENT_SECRET");

	public final static String BACK_HOST = System.getenv("VITE_AUTH_BACKEND");
	public final static String FRONT_HOST = System.getenv("VITE_AUTH_FRONTEND");

	public final static String Mailgun = System.getenv("MAILGUN_KEY");
	public final static String PROD = System.getenv("VITE_PROD");

	public static Connection getConnection() throws SQLException {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return DriverManager.getConnection(dbAuth.DB_URL, dbAuth.DB_USER, dbAuth.DB_PASSWORD);
	}
//mysql> CREATE TABLE users (
//    ->     uuid VARCHAR(36) PRIMARY KEY,
//    ->     email VARCHAR(255) NOT NULL UNIQUE,
//    ->     password_hash VARCHAR(255) NOT NULL,
//    ->     is_verified BOOLEAN DEFAULT FALSE,
//    ->     created_at BIGINT NOT NULL,
//    ->     updated_at BIGINT NOT NULL,
//    ->     last_login BIGINT
//    -> );
//mysql> CREATE TABLE email_tokens (
//    ->     token VARCHAR(255) PRIMARY KEY,
//    ->     user_uuid VARCHAR(36) NOT NULL,
//    ->     expires_at BIGINT NOT NULL,
//    ->     FOREIGN KEY (user_uuid) REFERENCES users(uuid) ON DELETE CASCADE
//    -> );

}
