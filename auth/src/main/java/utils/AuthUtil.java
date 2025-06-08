package utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import db.dbAuth;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthUtil {
	public static void setAuthCookie(HttpServletResponse resp, String userUUID) {
		try {
			String jwt = userUUID + ":|:" + PassUtil.signUUID(userUUID);
			String encodedJwt = Base64.getEncoder().encodeToString(jwt.getBytes());
			Cookie authCookie = new Cookie("hriday_tech_auth_token", encodedJwt);

			if ("yes".equals(dbAuth.PROD)) {
				authCookie.setHttpOnly(true);
				authCookie.setSecure(true);
				authCookie.setDomain("hriday.tech");
			}
			authCookie.setMaxAge(60 * 60 * 24 * 7); // 7 days
			authCookie.setPath("/");
			resp.addCookie(authCookie);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getUserUUIDFromCookie(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		if (cookies == null)
			throw new IllegalArgumentException("No cookies");

		String jwtEnc = null;
		for (Cookie cookie : cookies) {
			if ("hriday_tech_auth_token".equals(cookie.getName())) {
				jwtEnc = cookie.getValue();
				break;
			}
		}
		if (jwtEnc == null)
			throw new IllegalArgumentException("Not logged in");

		byte[] decodedBytes = Base64.getDecoder().decode(jwtEnc);
		String jwt = new String(decodedBytes, StandardCharsets.UTF_8);
		String[] parts = jwt.split(":\\|:");
		if (parts.length != 2)
			throw new IllegalArgumentException("Invalid token format");

		String uuid = parts[0];
		String sign = parts[1];

		if (!PassUtil.signUUID(uuid).equals(sign))
			throw new IllegalArgumentException("Invalid token signature");

		return uuid;
	}
}
