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
import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;

import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthParameters.OAuthType;
import com.google.gson.JsonElement;

public class GmailProvider extends BaseProvider
{
	private static final String	GOOGLE_EMAIL_INFO_SCOPE	= (String)Play.configuration.get("google.email.info.scope"); //"https://www.googleapis.com/userinfo/email?alt=json";
	private static final String	GOOGLE_MAIL_SCOPE	= (String)Play.configuration.get("google.mail.scope"); //"https://mail.google.com/ https://www.googleapis.com/auth/userinfo.email";
	private static final String	CALLBACK_URL_BEGIN	= (String)Play.configuration.get("google.callback.url.begin"); //"http://dev.deallr.com/account/upgradeEmailToken/"
	private static final String	CALLBACK_URL_END	= (String)Play.configuration.get("google.callback.url.end"); //"/gmail/"
	private static final String CONSUMER_KEY;
	private static final String CONSUMER_SECRET;
	private static final ServiceProvider gmailProvider;
	private static final GoogleOAuthHelper oauthHelper;
	
	private static final String	ACCOUNT_LOOKUP_HQL = "SELECT u FROM Account u WHERE u.id IS ? AND u.userInfo.id IS ?";
	
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
		String callbackUrl = CALLBACK_URL_BEGIN + userId + CALLBACK_URL_END;
		GoogleOAuthParameters oauthParameters = getAuthParams();
		oauthParameters.setOAuthCallback(callbackUrl);
		oauthHelper.getUnauthorizedRequestToken(oauthParameters);
		String requestUrl = oauthHelper.createUserAuthorizationUrl(oauthParameters);

		// Store the information, leaving the access token blank
		Date current = new Date(System.currentTimeMillis());
		String tokenSecret = oauthParameters.getOAuthTokenSecret();
		UserInfo userInfo = UserInfo.find("id", userId).first();
		new Account(userInfo, null, null, null, tokenSecret, null, true, true, null, 
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
			// Make sure userId matches
			Account account =  Account.find(ACCOUNT_LOOKUP_HQL, accountId, userId).first();
			if(account != null)
			{
				//Get access token
				GoogleOAuthParameters oauthParameters = getAuthParams();
				oauthParameters.setOAuthTokenSecret(account.dllrTokenSecret);
				oauthHelper.getOAuthParametersFromCallback(queryString, oauthParameters);
				String token = oauthHelper.getAccessToken(oauthParameters);
				oauthParameters.setOAuthToken(token);
				String header = oauthHelper.getAuthorizationHeader(GOOGLE_EMAIL_INFO_SCOPE, "GET", oauthParameters);
				
				//Get authorized email account
				String email = "";
				try
				{
					HttpResponse httpResponse = WS.url(GOOGLE_EMAIL_INFO_SCOPE).setHeader("Authorization", header).get();
					if(httpResponse.getStatus() != 200)
					{
						returnMessage = "Couldn't get email address for account.";
						serviceResponse.addError(ErrorCodes.OAUTH_EXCEPTION.toString(), "Couldn't get email address for account.");
					}
					else
					{
						// {"data":{"email": "praachee@gmail.com","isVerified": true}}
						JsonElement json = httpResponse.getJson();
						JsonElement data = json.getAsJsonObject().get("data");
						boolean isVerified = data.getAsJsonObject().get("isVerified").getAsBoolean();
						if(isVerified)
						{
							email = data.getAsJsonObject().get("email").getAsString();
							
							List<String> emailMessage = new ArrayList<String>();
							emailMessage.add(email);
							response.put("email", emailMessage);
						}
						else
						{
							returnMessage = "Email account being upgraded is not verified.";
							serviceResponse.addError(ErrorCodes.OAUTH_EXCEPTION.toString(), "Email account being added is not verified.");
						}
					}
				}
				catch(Exception ex)
				{
					Logger.error(ex.getMessage() + " :: " + ex.getCause(), ex);
					returnMessage = "Error upgrading token.";
					serviceResponse.addError(ErrorCodes.OAUTH_EXCEPTION.toString(), "Error getting access to Gmail account.");
				}
				
				//Update account details
				account.dllrAccessToken = token;
				account.email = email;
				account.save();
			}
			else
			{
				returnMessage = "No matching account found";
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
		oauthParameters.setScope(GOOGLE_MAIL_SCOPE);
		oauthParameters.setOAuthConsumerKey(CONSUMER_KEY);
		oauthParameters.setOAuthConsumerSecret(CONSUMER_SECRET);
		oauthParameters.setOAuthType(OAuthType.THREE_LEGGED_OAUTH);
		return oauthParameters;
	}
}
