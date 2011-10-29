package bl.providers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jsonModels.Service;
import models.Account;
import models.ServiceProvider;
import models.UserInfo;
import models.enums.ErrorCodes;
import models.enums.Providers;
import play.Logger;
import play.libs.WS;
import play.libs.OAuth.ServiceInfo;
import play.libs.OAuth.TokenPair;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;

import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthParameters.OAuthType;

public class GmailProvider extends BaseProvider
{
	private static final String	GOOGLE_EMAIL_END_POINT	= "https://www.googleapis.com/userinfo/email?alt=json";
	private static final String	SCOPE	= "https://mail.google.com/ https://www.googleapis.com/auth/userinfo.email";
	private static final String CONSUMER_KEY;
	private static final String CONSUMER_SECRET;
	private static final ServiceProvider gmailProvider;
	private static final GoogleOAuthHelper oauthHelper;
	
	// Initialize tokens
	static
	{
		gmailProvider = ServiceProvider.find("name", Providers.GMAIL.toString()).first();
		oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
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
	 * Checks to see if the user account is authorized
	 * @param userId
	 * @param email
	 * @return
	 */
	public static boolean isAccountAuthorized(Long userId, String email)
	{
		boolean isAuthorized = false;

		//Check if we have the account already
		List<Account> accounts = Account.find("email", email).fetch();
		if(accounts != null && accounts.size() == 1)
		{
			Account account = accounts.get(0);

			// Make sure userId matches
			if(account.userInfo.id == userId && account.active)
			{
				isAuthorized = true;
			}
		}
		return isAuthorized;
	}
	
	/**
	 * Gets a request token for this user
	 * @param userId
	 * @param email
	 * @return
	 * @throws OAuthException
	 */
	public static String authorizeAccount(Long userId, String email) throws OAuthException
	{
		GoogleOAuthParameters oauthParameters = getAuthParams();
		oauthParameters.setOAuthCallback("http://gatekeeper.deallr.com:9000/account/upgradeEmailToken/" + userId + "/gmail/");
		oauthHelper.getUnauthorizedRequestToken(oauthParameters);
		String requestUrl = oauthHelper.createUserAuthorizationUrl(oauthParameters);

		// Store the information, leaving the access token blank
		Date current = new Date(System.currentTimeMillis());
		String tokenSecret = oauthParameters.getOAuthTokenSecret();
		UserInfo userInfo = UserInfo.find("id", userId).first();
		new Account(userInfo, email, null, null, tokenSecret, null, true, true, null, 
							  current, null, current, current, gmailProvider).save();
		return requestUrl;
	}
	
	/**
	 * Upgrades the request token to an access token
	 * @param userId
	 * @param email
	 * @param queryString
	 * @return
	 */
	public static Map<String, List<?>> upgradeToken(Long userId, Long accountId, 
																									String queryString, Service serviceResponse)
	{
		String returnMessage = "Successfully upgraded token";
		Map<String, List<?>> response = new HashMap<String, List<?>>();
		
		try
		{
			List<Account> accounts =  Account.find("id", accountId).fetch();
			if(accounts != null && accounts.size() == 1)
			{
				Account account = accounts.get(0);
				
				// Make sure userId matches
				if(account.userInfo.id == userId)
				{
					//Get access token
					GoogleOAuthParameters oauthParameters = getAuthParams();
					oauthParameters.setOAuthTokenSecret(account.dllrTokenSecret);
					oauthHelper.getOAuthParametersFromCallback(queryString, oauthParameters);
					String token = oauthHelper.getAccessToken(oauthParameters);
					
					//Get authorized email account
					String email = "";
					ServiceInfo oauthInfo = new ServiceInfo(oauthHelper.getRequestTokenUrl(), 
																									oauthHelper.getAccessTokenUrl(), 
																									null, CONSUMER_KEY, CONSUMER_SECRET);
					TokenPair oauthTokens = new TokenPair(token, account.dllrTokenSecret);
					try
					{
						Map<String, Object> parameters = new HashMap<String, Object>();
						parameters.put("oauth_version", "1.0");
						parameters.put("oauth_nonce", oauthParameters.getOAuthNonce());
						parameters.put("oauth_timestamp", oauthParameters.getOAuthTimestamp());
						parameters.put("oauth_consumer_key", oauthParameters.getOAuthConsumerKey());
						parameters.put("oauth_token", oauthParameters.getOAuthToken());
						parameters.put("oauth_signature_method", oauthParameters.getOAuthSignatureMethod());
						parameters.put("oauth_signature", oauthParameters.getOAuthSignature());
						parameters.put("v", "2");
						
						WSRequest url = WS.url(GOOGLE_EMAIL_END_POINT).params(parameters);
						HttpResponse httpResponse = url.get();
						Logger.debug("Response status: " + httpResponse.getStatus());
						String json = httpResponse.getString();
						Logger.debug(""+json);
					}
					catch(Exception ex)
					{
						Logger.error(ex, returnMessage);
					}
					
					//Update account details
					account.dllrAccessToken = token;
//					account.email = email;
					account.save();
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
			serviceResponse.addError(ErrorCodes.OAUTH_EXCEPTION.toString(), e.getCause() + e.getMessage());
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
	
	/**
	 * Helper method to get all the oauth parameters in one place
	 * @return
	 */
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
