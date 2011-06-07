package controllers;

import java.util.List;

import bl.googleAuth.GmailProvider;

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
	public static void authorizeEmail(@Required(message="Email provider is required") String provider,
																	  @Required(message="Email is required") String email)
	{
		//Validate input
    
		// Go to correct provider
		if(provider != null && EmailProviders.GMAIL.toString().equals(provider))
		{
			//Check if account exists and is still valid
			boolean isAuthorized = GmailProvider.isAccountAuthorized(email);
			
			//Account not present or invalid, then get a new one and store it
			if(!isAuthorized)
			{
				String message = GmailProvider.authorizeAccount(email);
			}
		}
	}
}