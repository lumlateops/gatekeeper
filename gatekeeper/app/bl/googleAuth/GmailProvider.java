package bl.googleAuth;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jsonModels.Error;
import jsonModels.Errors;
import jsonModels.Response;
import jsonModels.ServiceResponse;

import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthSigner;

import play.Logger;

import models.Account;
import models.EmailProviders;
import models.ErrorCodes;
import models.ServiceProvider;

public class GmailProvider
{
	//TODO Add scope to the DB as well
	private static final String	SCOPE	= "https://mail.google.com/mail/feed/atom/";
	private static final String CONSUMER_KEY;
	private static final String CONSUMER_SECRET;
	private static final ServiceProvider gmailProvider;
	
	// Initialize tokens
	static
	{
		gmailProvider = ServiceProvider.find("name", EmailProviders.GMAIL.toString()).first();
		CONSUMER_KEY = gmailProvider.consumerKey;
		CONSUMER_SECRET = gmailProvider.consumerSecret;
	}
	
	/**
	 * Checks to see if the account already exists and is authorized
	 * @param userId
	 * @param email
	 * @return
	 */
	public static ServiceResponse isAccountAuthorized(String userId, String email)
	{
		String returnMessage = "false";
		ServiceResponse serviceResponse = null;
		Map<String, List<?>> response = new HashMap<String, List<?>>();

		//Check if we have the account already
		List<Account> accounts = Account.find("email", email).fetch();
		if(accounts != null && accounts.size() == 1)
		{
			Account account = accounts.get(0);
			
			// Make sure userId matches
			if(account.userId.equalsIgnoreCase(userId))
			{
				if(account.active)
				{
					returnMessage = "true";
				}
			}
			else
			{
				returnMessage = "Email address already registered to a different user.";
				List<Error> error = new ArrayList<Error>();
				error.add(new Error(ErrorCodes.DUPLICATE_ACCOUNT.toString(), returnMessage));
				serviceResponse = new Errors(error);
			}
		}
		else if(accounts != null && accounts.size() > 1)
		{
			returnMessage = "Multiple accounts found with this email address.";
			List<Error> error = new ArrayList<Error>();
			error.add(new Error(ErrorCodes.MULTIPLE_ACCOUNTS_WITH_SAME_EMAIL.toString(), returnMessage));
			serviceResponse = new Errors(error);
		}
		else
		{
			returnMessage = "No matching account found";
			List<Error> error = new ArrayList<Error>();
			error.add(new Error(ErrorCodes.ACCOUNT_NOT_FOUND.toString(), returnMessage));
			serviceResponse = new Errors(error);
		}
		
		List<String> message = new ArrayList<String>();
		message.add(returnMessage);
		response.put("Message", message);
		serviceResponse = new Response(response);
		return serviceResponse;
	}
	
	public static boolean isDuplicateAccount(String userId, String email)
	{
		boolean isDuplicate = false;

		//Check if we have the account already
		List<Account> accounts = Account.find("email", email).fetch();
		if(accounts != null && accounts.size() > 0)
		{
			for (Account account : accounts)
			{
				if(account.active)
				{
					isDuplicate = true;
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
	public static String authorizeAccount(String userId, String email) throws OAuthException
	{
		GoogleOAuthParameters oauthParameters = getAuthParams();
		oauthParameters.setOAuthCallback("http://dev.deallr.com/account/upgradeEmailToken/" + userId + "/gmail/" + email);
		GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
		oauthHelper.getUnauthorizedRequestToken(oauthParameters);
		String requestUrl = oauthHelper.createUserAuthorizationUrl(oauthParameters);
		
		// Store the information, leaving the access token blank
		Date current = new Date(System.currentTimeMillis());
		String tokenSecret = oauthParameters.getOAuthTokenSecret();
		new Account(userId, email, gmailProvider, "", tokenSecret, true, "", 
								current, null, current, current).save();
		return requestUrl;
	}
	
	/**
	 * Upgrades the request token to an access token
	 * @param userId
	 * @param email
	 * @param queryString
	 * @return
	 */
	public static ServiceResponse upgradeToken(String userId, String email, String queryString)
	{
		Logger.debug("upgrade token called");
		String returnMessage = "Successfully upgraded token";
		ServiceResponse serviceResponse = null;
		Map<String, List<?>> response = new HashMap<String, List<?>>();
		
		try
		{
			List<Account> accounts = Account.findAll();
			if(accounts != null && accounts.size() == 1)
			{
				Account account = accounts.get(0);
				
				// Make sure userId matches
				if(account.userId.equalsIgnoreCase(userId))
				{
					GoogleOAuthParameters oauthParameters = getAuthParams();
					oauthParameters.setOAuthTokenSecret(account.dllrTokenSecret);

					GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
					oauthHelper.getOAuthParametersFromCallback(queryString, oauthParameters);

					String token = oauthHelper.getAccessToken(oauthParameters);
					
					account.dllrAuthToken = token;
					Logger.debug("Access Token: "+token);
					account.save();
				}
				else
				{
					returnMessage = "No matching account found";
					List<Error> error = new ArrayList<Error>();
					error.add(new Error(ErrorCodes.ACCOUNT_NOT_FOUND.toString(), returnMessage));
					serviceResponse = new Errors(error);
				}
			}
			else
			{
				returnMessage = "No matching account found";
				List<Error> error = new ArrayList<Error>();
				error.add(new Error(ErrorCodes.ACCOUNT_NOT_FOUND.toString(), returnMessage));
				serviceResponse = new Errors(error);
			}
			
			List<String> message = new ArrayList<String>();
			message.add(returnMessage);
			response.put("Message", message);
			serviceResponse = new Response(response);
		}
		catch (OAuthException e)
		{
			returnMessage = e.getCause() + e.getMessage();
			List<Error> error = new ArrayList<Error>();
			error.add(new Error(ErrorCodes.OAUTH_EXCEPTION.toString(), returnMessage));
			serviceResponse = new Errors(error);
		}

		return serviceResponse;
	}
	
	/**
	 * Revoke the OAuth token
	 * @param email
	 * @return
	 */
	public static ServiceResponse revokeAccess(String userId, String password, String email)
	{
		String returnMessage = "Revoked access for " + email;
		ServiceResponse serviceResponse = null;
		Map<String, List<?>> response = new HashMap<String, List<?>>();
		
		try
		{
			// TODO: Check user credentials
			boolean isAuthenticated = false;
			
			if(isAuthenticated)
			{
				// Revoke token
				GoogleOAuthParameters oauthParameters = getAuthParams();
				GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
				oauthHelper.revokeToken(oauthParameters);
				
				// Clean up database
				Account revokedAccount = ((Account)Account.find("email", email).first());
				revokedAccount.active = false;
				revokedAccount.save();
				
				List<String> message = new ArrayList<String>();
				message.add(returnMessage);
				response.put("Message", message);
				serviceResponse = new Response(response);
			}
			else
			{
				returnMessage = "Incorrect login";
				List<Error> error = new ArrayList<Error>();
				error.add(new Error(ErrorCodes.OAUTH_EXCEPTION.toString(), returnMessage));
				serviceResponse = new Errors(error);
			}
		}
		catch (OAuthException e)
		{
			returnMessage = e.getCause() + e.getMessage();
			List<Error> error = new ArrayList<Error>();
			error.add(new Error(ErrorCodes.OAUTH_EXCEPTION.toString(), returnMessage));
			serviceResponse = new Errors(error);
		}
		
		return serviceResponse;
	}
	
	private static GoogleOAuthParameters getAuthParams()
	{
		GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
		oauthParameters.setScope(SCOPE);
		oauthParameters.setOAuthConsumerKey(CONSUMER_KEY);
		oauthParameters.setOAuthConsumerSecret(CONSUMER_SECRET);
		return oauthParameters;
	}
}
