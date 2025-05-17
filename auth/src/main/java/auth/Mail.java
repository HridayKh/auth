package auth;

import java.io.IOException;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.MessageResponse;
import com.mailgun.model.message.Message.MessageBuilder;

public class Mail {
	public static String sendMail(String sendTo, String subject, String content) throws IOException {
		String key = System.getenv("Mailgun");
		MailgunClient.config(key);
		MailgunMessagesApi mailgunMessagesApi = MailgunClient.config(key).createApi(MailgunMessagesApi.class);
		MessageBuilder msgBUild = Message.builder().from("Auth from Hriday.Tech" + "<no-reply@auth.hriday.tech>")
				.to(sendTo).subject(subject);
		Message message = null;
		message = msgBUild.html(content).build();
		MessageResponse ms = mailgunMessagesApi.sendMessage("auth.hriday.tech", message);
		return ms.toString();
	}
}
