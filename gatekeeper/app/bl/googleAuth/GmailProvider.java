package bl.googleAuth;

import java.util.Date;
import java.util.List;

import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthSigner;

import play.Logger;

import models.Account;
import models.EmailProviders;
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
	
	public static boolean isAccountAuthorized(String userId, String email)
	{
		boolean isAuthorized = false;

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
					isAuthorized = true;
				}
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
	public static String authorizeAccount(String userId, String email) throws OAuthException
	{
		GoogleOAuthParameters oauthParameters = getAuthParams();
		oauthParameters.setOAuthCallback("http://dev.deallr.com/upgrade/" + userId + "/gmail/" + email);
		GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
		oauthHelper.getUnauthorizedRequestToken(oauthParameters);
		String requestUrl = oauthHelper.createUserAuthorizationUrl(oauthParameters);
		
		return requestUrl;
	}
	
	/**
	 * Upgrades the request token to an access token
	 * @param userId
	 * @param email
	 * @param queryString
	 * @return
	 */
	public static String upgradeToken(String userId, String email, String queryString)
	{
		Logger.info("upgrade token called");
		String returnMessage = "Successfully upgraded token";
		try
		{
			GoogleOAuthParameters oauthParameters = getAuthParams();
			GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
			oauthHelper.getOAuthParametersFromCallback(queryString, oauthParameters);
			
			String token = oauthHelper.getAccessToken(oauthParameters);
			String tokenSecret = oauthParameters.getOAuthTokenSecret();
			
			Logger.info("Number of accounts in database before adding: "+Account.findAll().size());
			
			// Store the information
			Date current = new Date(System.currentTimeMillis());
			new Account(userId, email, gmailProvider, token, tokenSecret, true, "", 
									current, null, current, current).save();
			
			Logger.info("Account upgraded, number of accounts in database: " + Account.findAll().size());
			
		}
		catch (OAuthException e)
		{
			returnMessage = e.getCause() + e.getMessage();
		}
		return returnMessage;
	}
	
	/**
	 * Revoke the OAuth token
	 * @param email
	 * @return
	 */
	public static String revokeAccess(String userId, String password, String email)
	{
		String returnMessage = "Revoked access for " + email;
		
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
			}
			else
			{
				returnMessage = "Incorrect login";
			}
		}
		catch (OAuthException e)
		{
			returnMessage = e.getCause() + e.getMessage();
		}
		
		return returnMessage;
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
