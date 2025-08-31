package utils;

import java.io.IOException;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.Message.MessageBuilder;
import com.mailgun.model.message.MessageResponse;
import com.mailgun.util.Constants;

import db.dbAuth;

public class MailUtil {

	public static String templateVerifyMail(String link) {
		return """
				<!DOCTYPE html><html><head><meta charset="UTF-8">
				<title>Please Verify your E-Mail for HridayKh.in</title>
				<style>
				  body { font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px; }
				  .container { max-width: 600px; margin: auto; background: #fff; border-radius: 8px; padding: 20px; box-shadow: 0 0 10px rgba(0,0,0,0.05); }
				  h2 { color: #333; } p { color: #555; }
				  .btn { display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px; }
				</style></head><body><div class="container">
				<h2>Welcome to HridayKh.in!</h2>
				<p>Thank you for signing up. Please confirm your email address to activate your account.</p>
				<p><a class="btn" href="%s">Verify Email</a></p>
				<p>If you didn't sign up, you can safely ignore this email.</p>
				<p style="font-size: 12px; color: #999;">This link will expire in 24 hours.</p>
				</div></body></html>
				"""
				.formatted(link);
	}

	public static String sendMail(String sendTo, String subject, String content) throws IOException {
		MailgunMessagesApi mailgunMessagesApi = MailgunClient.config(Constants.EU_REGION_BASE_URL, dbAuth.Mailgun)
				.createApi(MailgunMessagesApi.class);
		MessageBuilder msgBUild = Message.builder().from("HridayKh.in Auth" + "<no-reply@auth.hridaykh.in>").to(sendTo)
				.subject(subject);
		Message message = null;
		message = msgBUild.html(content).build();
		MessageResponse ms = mailgunMessagesApi.sendMessage("auth.hridaykh.in", message);
		return ms.toString();
	}

}
