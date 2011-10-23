package bl.providers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jsonModels.Service;
import models.Account;
import models.NewAccountMessage;
import models.ServiceProvider;
import models.UserInfo;
import models.enums.ErrorCodes;
import bl.RMQProducer;
import bl.utilities.Utility;

public class BaseProvider
{
	/**
	 * Checks to see if the account already exists and is authorized
	 * @param userId
	 * @param email
	 * @return
	 */
	public static Map<String, List<?>> isAccountAuthorized(Long userId, String email,
																												 Service serviceResponse)
	{
		String returnMessage = "false";
		Map<String, List<?>> response = new HashMap<String, List<?>>();

		//Check if we have the account already
		List<Account> accounts = Account.find("email", email).fetch();
		if(accounts != null && accounts.size() == 1)
		{
			Account account = accounts.get(0);
			
			// Make sure userId matches
			if(account.userInfo.id == userId)
			{
				if(account.active && account.password != null)
				{
					returnMessage = "true";
				}
			}
			else
			{
				serviceResponse.addError(ErrorCodes.DUPLICATE_ACCOUNT.toString(), 
																 "Email address already registered to a different user.");
			}
		}
		else if(accounts != null && accounts.size() > 1)
		{
			serviceResponse.addError(ErrorCodes.MULTIPLE_ACCOUNTS_WITH_SAME_EMAIL.toString(), 
															 "Multiple accounts found with this email address.");
		}
		else
		{
			serviceResponse.addError(ErrorCodes.ACCOUNT_NOT_FOUND.toString(), 
															 "No matching account found");
		}
		
		List<String> message = new ArrayList<String>();
		message.add(returnMessage);
		response.put("isAccountAuthorized", message);
		return response;
	}
	
	/**
	 * Gets a request token for this user
	 * @param userId
	 * @param email
	 * @return
	 * @throws Exception
	 */
	public static void createAccount(Long userId, String email, String password, ServiceProvider provider) throws Exception
	{
		// Make sure the user exists
		UserInfo user = UserInfo.find("id", userId).first();
		
		if(user != null)
		{
			// Store the information, leaving the access token blank
			password = Utility.encrypt(password);
			Date currentDate = new Date(System.currentTimeMillis());
			Account newAccount = new Account(user, email, password, null, null, null,
																			 Boolean.TRUE, Boolean.TRUE, "", currentDate, 
																			 currentDate, currentDate, currentDate, provider).save();
			
			// Add new email address to queue
			RMQProducer.publishNewEmailAccountMessage(new NewAccountMessage(
																										newAccount.id, 
																										newAccount.email, 
																										newAccount.password,
																										newAccount.dllrAccessToken, 
																										newAccount.dllrTokenSecret,
																										newAccount.provider.name));
		}
		else
		{
			throw new Exception("No matching user found");
		}
	}
}
