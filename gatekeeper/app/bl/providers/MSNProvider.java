package bl.providers;

import models.ServiceProvider;
import models.enums.Providers;

public class MSNProvider extends BaseProvider
{
	private static final ServiceProvider msnProvider;
	
	//Initialize tokens
	static
	{
		msnProvider = ServiceProvider.find("name", Providers.MSN.toString()).first();
	}
	
	/**
	 * Creates a MSN account for this user
	 * @param userId
	 * @param email
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public static void createAccount(Long userId, String email, String password) throws Exception
	{
		createAccount(userId, email, password, msnProvider);
	}
}
