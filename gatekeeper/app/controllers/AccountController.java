package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jsonModels.Message;
import jsonModels.Request;
import jsonModels.Service;
import models.Account;
import models.ErrorCodes;
import models.Providers;
import models.ServiceProvider;
import bl.googleAuth.GmailProvider;

import play.Logger;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;

public class AccountController extends Controller
{
	private static final String	ACCOUNT_LOOKUP_HQL = "SELECT u FROM Account u WHERE u.userInfo.id IS ? AND u.provider IS ? ";
	
	@Before
	public static void logRequest()
	{
		Logger.debug("-----------------BEGIN REQUEST INFO-----------------");
		play.mvc.Http.Request currentRequest = play.mvc.Http.Request.current();
		Logger.debug("Request end point: " + currentRequest.action);
		Map<String, String[]> requestParams = currentRequest.params.all();
		for (String key : requestParams.keySet())
		{
			Logger.debug(key + ": '"+ requestParams.get(key)[0] + "'");
		}
	}
	
	@After
	public static void logResponse()
	{
		play.mvc.Http.Response currentResponse = play.mvc.Http.Response.current();
		Logger.debug("Response status: " + currentResponse.status);
	}
	
	/**
	 * Checks to see if the email address is authorized already.
	 * If not it gets the request token for the address.
	 * @param author
	 */
	public static void addEmail(@Required(message="UserId is required") Long userId,
			@Required(message="Email provider is required") String provider,
			@Required(message="Email is required") String email,
			@Required(message="Password is required") String password)
	{
		Long startTime = System.currentTimeMillis();
		
		Boolean isValidRequest = Boolean.TRUE;
		Service serviceResponse = new Service();
		Map<String, List<?>> response = new HashMap<String, List<?>>();
		
		if(Validation.hasErrors())
		{
			isValidRequest = Boolean.FALSE;
			for (play.data.validation.Error validationError : Validation.errors())
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), validationError.message());
			}
		}
		else
		{
			// Go to correct provider
			if(provider != null && Providers.GMAIL.toString().equalsIgnoreCase(provider.trim()))
			{
				Logger.debug("AE: Provider is Gmail");

				//Check if account exists and is still valid
				boolean isDuplicate = GmailProvider.isDuplicateAccount(email);

				Logger.debug("AE: isDuplicate: " + isDuplicate);

				//Account not present or invalid, then get a new one and store it
				if(!isDuplicate)
				{
					try
					{
						GmailProvider.createAccount(userId, email, password);
						response.put("status", 
								new ArrayList<String>()
								{
									{
										add("ok");
									}
								});
					}
					catch (Exception e)
					{
						Logger.debug("Error adding account: " + e.getCause() + e.getMessage());
						serviceResponse.addError(ErrorCodes.SERVER_EXCEPTION.toString(), 
																		 e.getMessage() + e.getCause());
					}
				}
				else
				{
					serviceResponse.addError(ErrorCodes.DUPLICATE_ACCOUNT.toString(), 
																	 "Account registered already");
				}
			}
			else
			{
				serviceResponse.addError(ErrorCodes.UNSUPPORTED_PROVIDER.toString(),
																 provider + " not supported.");
			}
		}
		Long endTime = System.currentTimeMillis();

		Map<String, String>	parameters = new HashMap<String, String>();
		parameters.put("userId", Long.toString(userId));
		parameters.put("provider", provider);
		parameters.put("email", email);
		parameters.put("password", password);
		Request request = new Request(isValidRequest, "addEmail", endTime-startTime, parameters);
		
		serviceResponse.setRequest(request);
		if(isValidRequest && response != null && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		
		renderJSON(new Message(serviceResponse));
	}
	
	/**
	 * Returns a list of all the registered email accounts for this user
	 * @param userId
	 */
	public static void listAllUserEmails(@Required(message="userId is required") Long userId)
	{
		Long startTime = System.currentTimeMillis();
		
		Logger.debug("**" + userId);

		Boolean isValidRequest = Boolean.TRUE;
		Service serviceResponse = new Service();
		Map<String, List<?>> response = new HashMap<String, List<?>>();
		
		if(Validation.hasErrors() || userId == null)
		{
			isValidRequest = Boolean.FALSE;
			for (play.data.validation.Error validationError : Validation.errors())
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), validationError.message());
			}
		}
		else
		{
			List<Account> accounts = Account.find("userId", userId).fetch();
			List<Account> activeAccounts = new ArrayList<Account>();
			if(accounts != null && accounts.size() > 0)
			{
				for (Account account : accounts)
				{
					if(account.registeredEmail && account.active)
					{
						activeAccounts.add(account);
					}
				}
			}
			response.put("Accounts", activeAccounts);
		}
		Long endTime = System.currentTimeMillis();

		Map<String, String>	parameters = new HashMap<String, String>();
		if(userId != null)
		{
			parameters.put("userId", Long.toString(userId));
		}
		else
		{
			parameters.put("userId", "null");
		}
		Request request = new Request(isValidRequest, "listAllUserEmails", endTime-startTime, parameters);
		
		serviceResponse.setRequest(request);
		if(isValidRequest && response != null && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		
		renderJSON(new Message(serviceResponse));
	}
	
	/**
	 * Upgrades a request token to an access token
	 * @param userId
	 * @param provider
	 * @param email
	 * @param requestToken
	 */
	public static void upgradeToken(@Required(message="userId is required") Long userId,
			@Required(message="Email provider is required") String provider,
			@Required(message="Email is required") String email,
			@Required(message="Query string needed for upgrading")String queryString)
	{
		Long startTime = System.currentTimeMillis();
		
		Boolean isValidRequest = Boolean.TRUE;
		Service serviceResponse = new Service();
		Map<String, List<?>> response = null;
		
		if(Validation.hasErrors())
		{
			isValidRequest = Boolean.FALSE;
			for (play.data.validation.Error validationError : Validation.errors())
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), validationError.message());
			}
		}
		else
		{
			// Go to correct provider
			if(provider != null && Providers.GMAIL.toString().equalsIgnoreCase(provider.trim()))
			{
				//Upgrade to access token and store the account
				response = GmailProvider.upgradeToken(userId, email, queryString, serviceResponse);
			}
			else
			{
				serviceResponse.addError(ErrorCodes.UNSUPPORTED_PROVIDER.toString(), provider + " not supported.");
			}
		}
		Long endTime = System.currentTimeMillis();

		Map<String, String>	parameters = new HashMap<String, String>();
		parameters.put("userId", Long.toString(userId));
		parameters.put("provider", provider);
		parameters.put("email", email);
		parameters.put("queryString", queryString);
		Request request = new Request(isValidRequest, "upgradeToken", endTime-startTime, parameters);
		
		serviceResponse.setRequest(request);
		if(isValidRequest && response != null && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		
		renderJSON(new Message(serviceResponse));
	}

	/**
	 * Revokes a access token
	 * @param userId
	 * @param provider
	 * @param email
	 */
	public static void revokeAccess(@Required(message="UserId is required") Long userId,
			@Required(message="UserId is required") String password,
			@Required(message="Email provider is required") String provider,
			@Required(message="Email is required") String email)
	{
		Long startTime = System.currentTimeMillis();

		Boolean isValidRequest = Boolean.TRUE;
		Service serviceResponse = new Service();
		Map<String, List<?>> response = null;
		
		if(Validation.hasErrors())
		{
			isValidRequest = Boolean.FALSE;
			for (play.data.validation.Error validationError : Validation.errors())
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), validationError.message());
			}
		}
		else
		{
			// Go to correct provider
			if(provider != null && Providers.GMAIL.toString().equalsIgnoreCase(provider.trim()))
			{
				//Upgrade to access token and store the account
				response = GmailProvider.revokeAccess(userId, password, email, serviceResponse);
			}
			else
			{
				serviceResponse.addError(ErrorCodes.UNSUPPORTED_PROVIDER.toString(), provider + " not supported.");
			}
		}
		Long endTime = System.currentTimeMillis();

		Map<String, String>	parameters = new HashMap<String, String>();
		parameters.put("userId", Long.toString(userId));
		parameters.put("provider", provider);
		parameters.put("email", email);
		Request request = new Request(isValidRequest, "revokeAccess", endTime-startTime, parameters);
		
		serviceResponse.setRequest(request);
		if(isValidRequest && response != null && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		
		renderJSON(new Message(serviceResponse));
	}

	/**
	 * Verifies if we still have access to this account
	 * @param userId
	 * @param provider
	 * @param email
	 */
	public static void verifyAccount(@Required(message="UserId is required") Long userId,
			@Required(message="Email provider is required") String provider,
			@Required(message="Email is required") String email)
	{
		Long startTime = System.currentTimeMillis();
		
		Boolean isValidRequest = Boolean.TRUE;
		Service serviceResponse = new Service();
		Map<String, List<?>> response = null;
		
		if(Validation.hasErrors())
		{
			isValidRequest = Boolean.FALSE;
			for (play.data.validation.Error validationError : Validation.errors())
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), validationError.message());
			}
		}
		else
		{
			// Go to correct provider
			if(provider != null && Providers.GMAIL.toString().equalsIgnoreCase(provider.trim()))
			{
				//Upgrade to access token and store the account
				response = GmailProvider.isAccountAuthorized(userId, email, serviceResponse);
			}
			else
			{
				serviceResponse.addError(ErrorCodes.UNSUPPORTED_PROVIDER.toString(), provider + " not supported.");
			}
		}
		Long endTime = System.currentTimeMillis();

		Map<String, String>	parameters = new HashMap<String, String>();
		parameters.put("userId", Long.toString(userId));
		parameters.put("provider", provider);
		parameters.put("email", email);
		Request request = new Request(isValidRequest, "verifyAccount", endTime-startTime, parameters);
		
		serviceResponse.setRequest(request);
		if(isValidRequest && response != null && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		
		renderJSON(new Message(serviceResponse));
	}
	
	/**
	 * Registers a new FBToken for the user
	 * @param id
	 * @param fbUserId
	 * @param fbAuthToken
	 */
	public static void updateFBToken(@Required(message="User Id is required") Long id,
																	 Long fbUserId, 
																	 @Required(message="FB auth token is required") String fbAuthToken)
	{
		Long startTime = System.currentTimeMillis();
		Boolean isValidRequest = Boolean.TRUE;
		
		Service serviceResponse = new Service();
		Map<String, List<?>> response = new HashMap<String, List<?>>();
		
		if(Validation.hasErrors())
		{
			isValidRequest = false;
			for (play.data.validation.Error validationError : Validation.errors())
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), validationError.message());
			}
		}
		else
		{
			ServiceProvider provider = ServiceProvider.find("name", "Facebook").first();
			if(provider != null)
			{
				Account fbAccount = Account.find(ACCOUNT_LOOKUP_HQL, id, provider).first();
				if(fbAccount == null)
				{
					serviceResponse.addError(ErrorCodes.ACCOUNT_NOT_FOUND.toString(), "No matching user found.");
				}
				else
				{
					fbAccount.dllrAccessToken = fbAuthToken;
					fbAccount.save();
					response.put("status", 
							new ArrayList<String>()
							{
								{
									add("ok");
								}
							});
				}
			}
			else
			{
				serviceResponse.addError(ErrorCodes.ACCOUNT_NOT_FOUND.toString(), "No matching user found.");
			}
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();			
		parameters.put("id", Long.toString(id));
		if(fbUserId != null)
		{
			parameters.put("fbUserId", Long.toString(fbUserId));
		}
		parameters.put("fbAuthToken", fbAuthToken);
		Request request = new Request(isValidRequest, "updateFBToken", endTime - startTime, parameters);

		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));
	}
}
