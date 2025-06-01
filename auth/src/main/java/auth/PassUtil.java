package auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import db.dbAuth;

public class PassUtil {

  public static String sha256Hash(String password) {
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    byte[] hashBytes = md.digest(password.getBytes());

    StringBuilder hexString = new StringBuilder(2 * hashBytes.length);
    for (byte b : hashBytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1)
        hexString.append('0');
      hexString.append(hex);
    }
    return hexString.toString();
  }

  public static String signUUID(String uuid) throws Exception {
    Mac hmac = Mac.getInstance("HmacSHA256");
    SecretKeySpec keySpec = new SecretKeySpec(sha256Hash(dbAuth.DB_PASSWORD).getBytes(), "HmacSHA256");
    hmac.init(keySpec);
    byte[] rawHmac = hmac.doFinal(uuid.getBytes());
    return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
  }
}
