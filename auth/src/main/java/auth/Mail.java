package auth;

import java.io.IOException;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.MessageResponse;
import com.mailgun.util.Constants;
import com.mailgun.model.message.Message.MessageBuilder;

public class Mail {
	public static String sendMail(String sendTo, String subject, String content) throws IOException {
		System.out.println("\n\n" + System.getenv("Mailgun") + "\n\n");
		MailgunMessagesApi mailgunMessagesApi = MailgunClient.config(Constants.EU_REGION_BASE_URL, System.getenv("Mailgun")).createApi(MailgunMessagesApi.class);
		MessageBuilder msgBUild = Message.builder().from("Auth from Hriday.Tech" + "<no-reply@auth.hriday.tech>")
				.to(sendTo).subject(subject);
		Message message = null;
		message = msgBUild.html(content).build();
		MessageResponse ms = mailgunMessagesApi.sendMessage("auth.hriday.tech", message);
		return ms.toString();
	}
}
