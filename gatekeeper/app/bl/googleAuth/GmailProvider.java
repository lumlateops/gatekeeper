package bl.googleAuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jsonModels.Error;
import jsonModels.Errors;
import jsonModels.Response;
import jsonModels.Service;
import jsonModels.ServiceResponse;

import bl.RMQProducer;
import bl.Utility;

import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthSigner;
import com.google.gdata.client.authn.oauth.OAuthParameters.OAuthType;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import play.Logger;

import models.Account;
import models.ErrorCodes;
import models.FetchHistory;
import models.NewAccountMessage;
import models.Providers;
import models.ServiceProvider;
import models.UserInfo;

public class GmailProvider
{
	//TODO Add scope to the DB as well
	private static final String	SCOPE	= "https://mail.google.com/";
	private static final String CONSUMER_KEY;
	private static final String CONSUMER_SECRET;
	private static final ServiceProvider gmailProvider;
	private static final String	FETCH_HISTORY_LOOKUP_HQL	= "SELECT f as f FROM FetchHistory f WHERE userId IS ? and fetchStatus='complete' and fetchEndTime > DATE_SUB(NOW(), INTERVAL 1 HOUR)";
	
	// Initialize tokens
	static
	{
		gmailProvider = ServiceProvider.find("name", Providers.GMAIL.toString()).first();
		CONSUMER_KEY = gmailProvider.consumerKey;
		CONSUMER_SECRET = gmailProvider.consumerSecret;
	}
	
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
	 * Checks if the email address is duplicate or not.
	 * @param email
	 * @return
	 */
	public static boolean isDuplicateAccount(String email)
	{
		boolean isDuplicate = false;

		//Check if we have the account already
		List<Account> accounts = Account.find("email", email).fetch();
		if(accounts != null && accounts.size() > 0)
		{
			for (Account account : accounts)
			{
				if(account.active && account.password != null)
				{
					isDuplicate = true;
//					if(account.dllrAccessToken!=null && !account.dllrAccessToken.isEmpty() && account.dllrTokenSecret!=null)
//					{
//						isDuplicate = true;
//					}
//					// If an incomplete account exists then clean it up so a new one can be registered
//					else
//					{
//						isDuplicate = false;
//						account.delete();
//					}
					break;
				}
			}
		}
		return isDuplicate;
	}
	
	/**
	 * Gets a request token for this user
	 * @param userId
	 * @param email
	 * @return
	 * @throws OAuthException
	 */
	public static void createAccount(Long userId, String email, String password) throws Exception
	{
		// Make sure the user exists
		UserInfo user = UserInfo.find("id", userId).first();
		
		if(user != null)
		{
			// Store the information, leaving the access token blank
			password = Utility.encrypt(password);
			Date currentDate = new Date(System.currentTimeMillis());
			new Account(user, email, password, null, null, null, 
									Boolean.TRUE, Boolean.TRUE, "", currentDate, currentDate, 
									currentDate, currentDate, gmailProvider).save();
			
			// Add new email address to queue
			RMQProducer.publishNewEmailAccountMessage(new NewAccountMessage(userId, email, password, Providers.GMAIL.toString()));
		}
		else
		{
			throw new Exception("No matching user found");
		}
	}
	
	/**
	 * Upgrades the request token to an access token
	 * @param userId
	 * @param email
	 * @param queryString
	 * @return
	 */
	public static Map<String, List<?>> upgradeToken(Long userId, String email, 
																									String queryString, Service serviceResponse)
	{
		Logger.debug("upgrade token called");
		String returnMessage = "Successfully upgraded token";
		Map<String, List<?>> response = new HashMap<String, List<?>>();
		
		try
		{
			List<Account> accounts =  Account.find("email", email).fetch();
			if(accounts != null && accounts.size() == 1)
			{
				Account account = accounts.get(0);
				
				// Make sure userId matches
				if(account.userInfo.id == userId)
				{
					GoogleOAuthParameters oauthParameters = getAuthParams();
					oauthParameters.setOAuthTokenSecret(account.dllrTokenSecret);

					GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
					oauthHelper.getOAuthParametersFromCallback(queryString, oauthParameters);

					String token = oauthHelper.getAccessToken(oauthParameters);
					
					account.dllrAccessToken = token;
					Logger.debug("Access Token: "+token);
					account.save();
					
					// Add new email address to queue if no fetch happened within the last 60 mins
//					FetchHistory lastFetch = FetchHistory.find(FETCH_HISTORY_LOOKUP_HQL, userId).first();
//					if(lastFetch==null)
					{
//						NewAccountMessage message = new NewAccountMessage(userId, email, token, 
//																															account.dllrTokenSecret,
//																															Providers.GMAIL.toString(),	
//																															gmailProvider.consumerKey, 
//																															gmailProvider.consumerSecret);
//						RMQProducer.publishNewEmailAccountMessage(message);
					}
				}
				else
				{
					returnMessage = "No matching account found";
					serviceResponse.addError(ErrorCodes.ACCOUNT_NOT_FOUND.toString(), "No matching account found");
				}
			}
			else
			{
				serviceResponse.addError(ErrorCodes.ACCOUNT_NOT_FOUND.toString(), "No matching account found");
			}
			
			List<String> message = new ArrayList<String>();
			message.add(returnMessage);
			response.put("Message", message);
		}
		catch (OAuthException e)
		{
			serviceResponse.addError(ErrorCodes.OAUTH_EXCEPTION.toString(), 
															 e.getCause() + e.getMessage());
		}

		return response;
	}
	
	/**
	 * Revoke the OAuth token
	 * @param email
	 * @return
	 */
	public static Map<String, List<?>> revokeAccess(Long userId, String password,
																						 String email, Service serviceResponse)
	{
		Map<String, List<?>> response = new HashMap<String, List<?>>();
		
		try
		{
			// TODO: Check user credentials
			boolean isAuthenticated = true;
			
			if(isAuthenticated)
			{
				List<Account> accounts =  Account.find("email", email).fetch();
				if(accounts != null && accounts.size() == 1)
				{
					Account account = accounts.get(0);
					
					// Make sure userId matches
					if(account.userInfo.id == userId)
					{
						// Revoke token
						GoogleOAuthParameters oauthParameters = getAuthParams();
						oauthParameters.setOAuthTokenSecret(account.dllrTokenSecret);
						oauthParameters.setOAuthToken(account.dllrAccessToken);
						GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
						oauthHelper.revokeToken(oauthParameters);
						
						// Clean up database
						account.active = false;
						account.save();
						
						List<String> message = new ArrayList<String>();
						message.add("Revoked access for " + email);
						response.put("Message", message);
					}
					else
					{
						serviceResponse.addError(ErrorCodes.ACCOUNT_NOT_FOUND.toString(), "No matching account found");
					}
				}
				else
				{
					serviceResponse.addError(ErrorCodes.ACCOUNT_NOT_FOUND.toString(), "No matching account found");
				}
			}
			else
			{
				serviceResponse.addError(ErrorCodes.AUTHENTICATION_FAILED.toString(), 
																 "Incorrect login");
			}
		}
		catch (OAuthException e)
		{
			serviceResponse.addError(ErrorCodes.OAUTH_EXCEPTION.toString(), 
															 e.getCause() + e.getMessage());
		}
		
		return response;
	}
	
	private static GoogleOAuthParameters getAuthParams()
	{
		GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
		oauthParameters.setScope(SCOPE);
		oauthParameters.setOAuthConsumerKey(CONSUMER_KEY);
		oauthParameters.setOAuthConsumerSecret(CONSUMER_SECRET);
		oauthParameters.setOAuthType(OAuthType.THREE_LEGGED_OAUTH);
		return oauthParameters;
	}
}
