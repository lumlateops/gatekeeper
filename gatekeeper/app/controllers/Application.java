package controllers;

import java.util.List;

import com.google.gdata.client.authn.oauth.OAuthException;

import jsonModels.Message;

import bl.googleAuth.AuthPayLoad;
import bl.googleAuth.GmailProvider;

import play.Logger;
import play.data.validation.Required;
import play.mvc.Controller;

import models.Account;
import models.EmailProviders;
import models.ServiceProvider;


public class Application extends Controller
{
	public static void index()
	{
		List<ServiceProvider> providers = ServiceProvider.findAll();
		render(providers);
	}
	
	/**
	 * Checks to see if the email address is authorized already.
	 * If not it gets the request token for the address.
	 * @param author
	 */
	public static void authorizeEmail(@Required(message="UserId is required") String userId,
																		@Required(message="Email provider is required") String provider,
																	  @Required(message="Email is required") String email)
	{
		String returnMessage = "";
    Logger.debug("Authorize Email Called:" + userId + "/" + provider + "/" + email);
    
    //TODO: Validate input
    
		// Go to correct provider
		if(provider != null && EmailProviders.GMAIL.toString().equalsIgnoreCase(provider.trim()))
		{
			Logger.debug("AE: Provider is Gmail");
			
			//Check if account exists and is still valid
			boolean isAuthorized = GmailProvider.isAccountAuthorized(email);
			
			Logger.debug("AE: isAuthorized: " + isAuthorized);
			
			//Account not present or invalid, then get a new one and store it
			if(!isAuthorized)
			{
				try
				{
					AuthPayLoad authPayLoad = GmailProvider.authorizeAccount(userId, email);
					redirect(authPayLoad.getRedirectUrl());
					Logger.info("Back from redirect");
					
					//Upgrade to access token and store the account
					GmailProvider.upgradeToken(userId, email, authPayLoad);
				}
				catch (OAuthException e)
				{
					returnMessage = e.getMessage() + e.getCause();
				}
			}
			else
			{
				returnMessage = "Account registered and authorized already";
			}
		}
		renderJSON(new Message(true, returnMessage));
	}
}