package bl.notifiers;
 
import java.util.List;

import models.Deal;
import models.UserInfo;
import play.Logger;
import play.mvc.Mailer;
 

public class Mails extends Mailer
{
	private static final String	DEAL_HQL = "SELECT d AS d FROM Deal d WHERE d.userInfo.id IS ? AND d.dealEmail.emailCategory.id IN (1, 5) ORDER BY d.createdAt";
	
	public static void welcome(String username, String email)
	{
		setSubject("Welcome %s", username);
		addRecipient(email);
		setFrom("Deallr <no-reply@deallr.com>");
		Logger.info("Sending welcome email to %s at address %s", username, email);
		send(username);
	}
	
	public static void deals(UserInfo user)
	{
		List<Object> freshDeals = Deal.find(DEAL_HQL, user.id).from(0).fetch(3);
		setSubject("%s, Deallr has new deals for you.", user.username);
		addRecipient(user.fbEmailAddress);
//		addRecipient("ravi@corp.deallr.com");
//		addRecipient("vipul@corp.deallr.com");
//		addRecipient("prachi@corp.deallr.com");
		setFrom("Deallr <no-reply@deallr.com>");
		Logger.info("Sending new deals email to %s at address %s", user.username, user.fbEmailAddress);
		send(user, freshDeals);
	}
}
