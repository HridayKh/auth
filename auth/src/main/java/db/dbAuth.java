package db;

import java.sql.Connection;
import java.sql.DriverManager;

public class dbAuth {

	public final static String DB_URL = "jdbc:mysql://db.hriday.tech:3306/Auth_Db";
	public final static String DB_USER = System.getenv("REMOTE_TOMCAT_AUTH_DB_USER");
	public final static String DB_PASSWORD = System.getenv("REMOTE_TOMCAT_AUTH_DB_PASSWORD");

	public static Connection getConnection() throws Exception {
		Class.forName("com.mysql.cj.jdbc.Driver");
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
