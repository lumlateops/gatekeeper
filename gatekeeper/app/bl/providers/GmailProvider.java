package bl.providers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jsonModels.Service;
import models.Account;
import models.ServiceProvider;
import models.enums.ErrorCodes;
import models.enums.Providers;
import play.Logger;

import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthParameters.OAuthType;

public class GmailProvider extends BaseProvider
{
	//TODO Add scope to the DB as well
	private static final String	SCOPE	= "https://mail.google.com/";
	private static final String CONSUMER_KEY;
	private static final String CONSUMER_SECRET;
	private static final ServiceProvider gmailProvider;
	
	// Initialize tokens
	static
	{
		gmailProvider = ServiceProvider.find("name", Providers.GMAIL.toString()).first();
		CONSUMER_KEY = gmailProvider.consumerKey;
		CONSUMER_SECRET = gmailProvider.consumerSecret;
	}
	
	/**
	 * Add a gmail account
	 * @param userId
	 * @param email
	 * @return
	 * @throws Exception
	 */
	public static void createAccount(Long userId, String email, String password) throws Exception
	{
		createAccount(userId, email, password, gmailProvider);
	}
	
	/**
	 * Upgrades the request token to an access token
	 * @param userId
	 * @param email
	 * @param queryString
	 * @return
	 */
	@Deprecated
	public static Map<String, List<?>> upgradeToken(Long userId, String email, 
																									String queryString, Service serviceResponse)
	{
		Logger.debug("upgrade token called");
		String returnMessage = "Successfully upgraded token";
		Map<String, List<?>> response = new HashMap<String, List<?>>();
		
//		try
//		{
//			List<Account> accounts =  Account.find("email", email).fetch();
//			if(accounts != null && accounts.size() == 1)
//			{
//				Account account = accounts.get(0);
//				
//				// Make sure userId matches
//				if(account.userInfo.id == userId)
//				{
//					GoogleOAuthParameters oauthParameters = getAuthParams();
//					oauthParameters.setOAuthTokenSecret(account.dllrTokenSecret);
//
//					GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
//					oauthHelper.getOAuthParametersFromCallback(queryString, oauthParameters);
//
//					String token = oauthHelper.getAccessToken(oauthParameters);
//					
//					account.dllrAccessToken = token;
//					Logger.debug("Access Token: "+token);
//					account.save();
//				}
//				else
//				{
//					returnMessage = "No matching account found";
//					serviceResponse.addError(ErrorCodes.ACCOUNT_NOT_FOUND.toString(), "No matching account found");
//				}
//			}
//			else
//			{
//				serviceResponse.addError(ErrorCodes.ACCOUNT_NOT_FOUND.toString(), "No matching account found");
//			}
//			
//			List<String> message = new ArrayList<String>();
//			message.add(returnMessage);
//			response.put("Message", message);
//		}
//		catch (OAuthException e)
//		{
//			serviceResponse.addError(ErrorCodes.OAUTH_EXCEPTION.toString(), e.getCause() + e.getMessage());
//		}

		return response;
	}
	
	/**
	 * Revoke the OAuth token
	 * @param email
	 * @return
	 */
	@Deprecated
	public static Map<String, List<?>> revokeAccess(Long userId, String password,
																						 String email, Service serviceResponse)
	{
		Map<String, List<?>> response = new HashMap<String, List<?>>();
		
//		try
//		{
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
//						// Revoke token
//						GoogleOAuthParameters oauthParameters = getAuthParams();
//						oauthParameters.setOAuthTokenSecret(account.dllrTokenSecret);
//						oauthParameters.setOAuthToken(account.dllrAccessToken);
//						GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
//						oauthHelper.revokeToken(oauthParameters);
						
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
//		}
//		catch (OAuthException e)
//		{
//			serviceResponse.addError(ErrorCodes.OAUTH_EXCEPTION.toString(), 
//															 e.getCause() + e.getMessage());
//		}
		
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
