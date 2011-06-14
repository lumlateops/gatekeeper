package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gdata.client.authn.oauth.OAuthException;

import jsonModels.Errors;
import jsonModels.Message;
import jsonModels.Parameter;
import jsonModels.Service;
import jsonModels.Request;

import bl.googleAuth.AuthPayLoad;
import bl.googleAuth.GmailProvider;

import play.Logger;
import play.data.validation.Required;
import play.mvc.Controller;

import models.Account;
import models.EmailProviders;
import models.ServiceProvider;

/**
 * The main controller for the application. This is the only controller in the
 * app for now but it will be split up later on as it gets more complicated.
 * 
 * @author prachi
 */
public class Application extends Controller
{
	public static void index()
	{
		List<ServiceProvider> providers = ServiceProvider.findAll();
		render(providers);
	}
	
	/**
	 * Returns a list of the providers supported by the system
	 */
	public static void listAllProviders()
	{
		Long startTime = System.currentTimeMillis();
		List<ServiceProvider> providers = ServiceProvider.findAll();
		Long endTime = System.currentTimeMillis();
		Request request = new Request(Boolean.TRUE, "listAllProviders", endTime-startTime, Collections.EMPTY_MAP);
		renderJSON(new Message(new Service(request, providers)));
	}
	
	/**
	 * Returns a list active providers supported by the system
	 */
	public static void listActiveProviders()
	{
		Long startTime = System.currentTimeMillis();
		List<ServiceProvider> allProviders = ServiceProvider.findAll();
		List<ServiceProvider> activeProviders = new ArrayList<ServiceProvider>();
		for (ServiceProvider serviceProvider : allProviders)
		{
			if(serviceProvider.active)
			{
				activeProviders.add(serviceProvider);
			}
		}
		Long endTime = System.currentTimeMillis();
		Request request = new Request(Boolean.TRUE, "listActiveProviders", endTime-startTime, Collections.EMPTY_MAP);
		renderJSON(new Message(new Service(request, activeProviders)));
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
		Long startTime = System.currentTimeMillis();
		
		Boolean isSuccess = Boolean.TRUE;
		String returnMessage = "";
    Logger.debug("Authorize Email Called:" + userId + "/" + provider + "/" + email);
    
    //TODO: Validate input
    
		// Go to correct provider
		if(provider != null && EmailProviders.GMAIL.toString().equalsIgnoreCase(provider.trim()))
		{
			Logger.debug("AE: Provider is Gmail");
			
			//Check if account exists and is still valid
			boolean isAuthorized = GmailProvider.isAccountAuthorized(userId, email);
			
			Logger.debug("AE: isAuthorized: " + isAuthorized);
			
			//Account not present or invalid, then get a new one and store it
			if(!isAuthorized)
			{
				try
				{
					String authUrl = GmailProvider.authorizeAccount(userId, email);
					Logger.info(authUrl);
					redirect(authUrl);
				}
				catch (OAuthException e)
				{
					isSuccess = Boolean.FALSE;
					returnMessage = e.getMessage() + e.getCause();
				}
			}
			else
			{
				returnMessage = "Account registered and authorized already";
			}
		}
		
		Long endTime = System.currentTimeMillis();
		
		Map<String, String>	parameters = new HashMap<String, String>();
		parameters.put("userId", userId);
		parameters.put("provider", provider);
		parameters.put("email", email);
		Request request = new Request(isSuccess, "authorizeEmail", endTime-startTime, parameters);
		
		renderJSON(new Message(new Service(request, returnMessage)));
	}
	
	/**
	 * Upgrades a request token to an access token
	 * @param userId
	 * @param provider
	 * @param email
	 * @param requestToken
	 */
	public static void upgradeToken(@Required(message="UserId is required") String userId,
																	@Required(message="Email provider is required") String provider,
																	@Required(message="Email is required") String email,
																	@Required(message="Query string needed for upgrading")String queryString)
	{
		Long startTime = System.currentTimeMillis();
		
		String returnMessage = "Token upgraded successfully";
    
		// Go to correct provider
		if(provider != null && EmailProviders.GMAIL.toString().equalsIgnoreCase(provider.trim()))
		{
			//Upgrade to access token and store the account
			returnMessage = GmailProvider.upgradeToken(userId, email, queryString);
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String>	parameters = new HashMap<String, String>();
		parameters.put("userId", userId);
		parameters.put("provider", provider);
		parameters.put("email", email);
		parameters.put("queryString", queryString);
		Request request = new Request(Boolean.TRUE, "upgradeToken", endTime-startTime, parameters);
		renderJSON(new Message(new Service(request, returnMessage)));
	}
	
	/**
	 * Revokes a access token
	 * @param userId
	 * @param provider
	 * @param email
	 */
	public static void revokeAccess(@Required(message="UserId is required") String userId,
																	@Required(message="UserId is required") String password,
																	@Required(message="Email provider is required") String provider,
																	@Required(message="Email is required") String email)
	{
		Long startTime = System.currentTimeMillis();
		
		String returnMessage = "Access revoked successfully";
    
		// Go to correct provider
		if(provider != null && EmailProviders.GMAIL.toString().equalsIgnoreCase(provider.trim()))
		{
			//Upgrade to access token and store the account
			returnMessage = GmailProvider.revokeAccess(userId, password, email);
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String>	parameters = new HashMap<String, String>();
		parameters.put("userId", userId);
		parameters.put("provider", provider);
		parameters.put("email", email);
		Request request = new Request(Boolean.TRUE, "revokeAccess", endTime-startTime, parameters);
		renderJSON(new Message(new Service(request, returnMessage)));
	}
	
	/**
	 * Verifies if we still have access to this account
	 * @param userId
	 * @param provider
	 * @param email
	 */
	public static void verifyAccount(@Required(message="UserId is required") String userId,
																	 @Required(message="Email provider is required") String provider,
																	 @Required(message="Email is required") String email)
	{
		Long startTime = System.currentTimeMillis();
		
		boolean isValid = false;
    
		// Go to correct provider
		if(provider != null && EmailProviders.GMAIL.toString().equalsIgnoreCase(provider.trim()))
		{
			//Upgrade to access token and store the account
			isValid = GmailProvider.isAccountAuthorized(userId, email);
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String>	parameters = new HashMap<String, String>();
		parameters.put("userId", userId);
		parameters.put("provider", provider);
		parameters.put("email", email);
		Request request = new Request(Boolean.TRUE, "revokeAccess", endTime-startTime, parameters);
		renderJSON(new Message(new Service(request, isValid)));
	}
	
	/**
	 * Login!
	 * @param userId
	 * @param provider
	 * @param email
	 */
	public static void login(@Required(message="username is required") String username,
													 @Required(message="Password is required") String password)
	{
		Long startTime = System.currentTimeMillis();
		
		boolean isValid = false;
    
		//TODO Authenticate the user
		
		Long endTime = System.currentTimeMillis();
		
		Map<String, String>	parameters = new HashMap<String, String>();
		parameters.put("username", username);
		parameters.put("password", password);
		Request request = new Request(Boolean.TRUE, "login", endTime-startTime, parameters);
		renderJSON(new Message(new Service(request, isValid)));
	}
}