package bl.providers;

import models.ServiceProvider;
import models.enums.Providers;

public class YahooProvider extends BaseProvider
{
	private static final ServiceProvider yahooProvider;
	
	//Initialize tokens
	static
	{
		yahooProvider = ServiceProvider.find("name", Providers.YAHOO.toString()).first();
	}
	
	/**
	 * Creates a yahoo account for this user
	 * @param userId
	 * @param email
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public static void createAccount(Long userId, String email, String password) throws Exception
	{
		createAccount(userId, email, password, yahooProvider);
	}
}
