package controllers;

import java.util.List;

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
		//Check if we have the account already
		List<Account> accounts = Account.find("email", email).fetch();
    
		//account exists, check if its still valid
		if(accounts != null && accounts.size() == 1)
    {
    	
    }
		//Account not present or invalid, then get a new one and store it
		else
		{
    	authorizeAndStoreAccount(provider, email);
    }
	}
	
	private static void authorizeAndStoreAccount(String provider, String email)
	{
		// Go to correct provider
		if(provider != null && EmailProviders.GMAIL.toString().equals(provider))
		{
		}
	}
}