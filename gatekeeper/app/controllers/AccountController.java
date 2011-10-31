package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jsonModels.EmailAccountResponse;
import jsonModels.Message;
import jsonModels.Request;
import jsonModels.Service;
import models.Account;
import models.ServiceProvider;
import models.enums.ErrorCodes;
import models.enums.Providers;
import play.Logger;
import play.Play;
import play.data.validation.Email;
import play.data.validation.Required;
import play.data.validation.Validation;
import bl.providers.GmailProvider;

import com.google.gdata.client.authn.oauth.OAuthException;

public class AccountController extends BaseContoller
{
	private static final int MAX_USER_ACCOUNTS = Integer.parseInt((String)Play.configuration.get("max.user.accounts"));
	private static final String	ACCOUNT_LOOKUP_HQL = "SELECT u FROM Account u WHERE u.userInfo.id IS ? AND u.provider IS ? ";
	private static final String	ACTIVE_ACCOUNT_LOOKUP_HQL = "SELECT u FROM Account u WHERE u.userInfo.id IS ? AND u.active IS 1 AND u.registeredEmail IS 1";
	private static final String	DEALLR_ACCOUNT_LOOKUP_HQL = "SELECT u FROM Account u WHERE u.userInfo.id IS ? AND u.active IS 1 AND u.registeredEmail IS 0";
	
	/**
	 * Registers an email account for the user.
	 * Restricts number of accounts per user to {max.user.accounts}.
	 * @param author
	 */
	public static void addEmail(@Required(message="UserId is required") Long userId,
															@Required(message="Email provider is required") String provider)
	{
		Long startTime = System.currentTimeMillis();
		
		Boolean isValidRequest = Boolean.TRUE;
		Service serviceResponse = new Service();
		List<String> authMessage = new ArrayList<String>();
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
				Logger.debug("AE: Registering a " + provider + " email account.");

				//Check if user has {max.user.accounts} accounts already
				boolean maxAccountcountReached = isMaxAccountCountReached(userId);
				
				if(!maxAccountcountReached)
				{
					//Check if account exists and is still valid
					boolean isAuthorized = GmailProvider.isAccountAuthorized(userId, "");
	
					Logger.debug("AE: isAuthorized: " + isAuthorized);
	
					//Account not present or invalid, then get a new one and store it
					if(!isAuthorized)
					{
						try
						{
							String authUrl = GmailProvider.authorizeAccount(userId, "");
							Logger.debug("Auth url: "+authUrl);
							authMessage.add(authUrl);
							response.put("AuthUrl", authMessage);
						}
						catch (OAuthException e)
						{
							Logger.debug("Error adding account: " + e.getCause() + e.getMessage());
							serviceResponse.addError(ErrorCodes.SERVER_EXCEPTION.toString(), 
																		 	 e.getMessage() + e.getCause());
						}
					}
					else
					{
						serviceResponse.addError(ErrorCodes.DUPLICATE_ACCOUNT.toString(), "Account registered already");
					}
//					//Check if account exists and is still valid
//					boolean isDuplicate = isDuplicateAccount(email);
//	
//					Logger.debug("AE: isDuplicate: " + isDuplicate);
//	
//					//Account not present or invalid, then get a new one and store it
//					if(!isDuplicate)
//					{
//						try
//						{
//							if(Providers.GMAIL.toString().equalsIgnoreCase(provider.trim()))
//							{
//									GmailProvider.createAccount(userId, email, password);
//							}
//							response.put("status", 
//									new ArrayList<String>()
//									{
//										{
//											add("ok");
//										}
//									});
//						}
//						catch (Exception e)
//						{
//							Logger.debug("Error adding account: " + e.getCause() + e.getMessage());
//							serviceResponse.addError(ErrorCodes.SERVER_EXCEPTION.toString(), 
//																			 e.getMessage() + e.getCause());
//						}
//					}
//					else
//					{
//						serviceResponse.addError(ErrorCodes.DUPLICATE_ACCOUNT.toString(), 
//																		 "Account registered already");
//					}
				}
				else
				{
					serviceResponse.addError(ErrorCodes.MAX_ACCOUNT_LIMIT_REACHED.toString(),
																	 "Cannot register more than " + MAX_USER_ACCOUNTS + " emails");
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
		if(userId != null)
		{
			parameters.put("userId", Long.toString(userId));
		}else
		{
			parameters.put("userId", "null");
		}
		parameters.put("provider", provider);
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
			List<Account> accounts = Account.find("userInfo_id", userId).fetch();
			List<EmailAccountResponse> activeAccounts = new ArrayList<EmailAccountResponse>();
			if(accounts != null && accounts.size() > 0)
			{
				for (Account account : accounts)
				{
					if(account.registeredEmail && account.active)
					{
						activeAccounts.add(new EmailAccountResponse(account.email, account.provider.name));
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
			@Required(message="Account id is required") Long accountId,
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
				response = GmailProvider.upgradeToken(userId, accountId, queryString, serviceResponse);
			}
			else
			{
				serviceResponse.addError(ErrorCodes.UNSUPPORTED_PROVIDER.toString(), provider + " not supported.");
			}
		}
		Long endTime = System.currentTimeMillis();

		Map<String, String>	parameters = new HashMap<String, String>();
		if(userId != null)
		{
			parameters.put("userId", Long.toString(userId));
		}else
		{
			parameters.put("userId", "null");
		}
		parameters.put("provider", provider);
		if(accountId != null)
		{
			parameters.put("accountId", accountId.toString());
		}
		else
		{
			parameters.put("accountId", "null");
		}
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
			@Required(message="Email is required")@Email String email)
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
		if(userId != null)
		{
			parameters.put("userId", Long.toString(userId));
		}else
		{
			parameters.put("userId", "null");
		}
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
			@Required(message="Email is required")@Email String email)
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
		if(userId != null)
		{
			parameters.put("userId", Long.toString(userId));
		}else
		{
			parameters.put("userId", "null");
		}
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
				serviceResponse.addError(ErrorCodes.ACCOUNT_NOT_FOUND.toString(), "No matching provider found.");
			}
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();			
		if(id != null)
		{
			parameters.put("id", Long.toString(id));
		}else
		{
			parameters.put("id", "null");
		}
		if(fbUserId != null)
		{
			parameters.put("fbUserId", Long.toString(fbUserId));
		}else
		{
			parameters.put("fbUserId", "null");
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
	
	/**
	 * Returns the user's deallr email address
	 * @param userId
	 */
	public static void getDeallrEmail(@Required(message="User Id is required") Long userId)
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
			final Account account = Account.find(DEALLR_ACCOUNT_LOOKUP_HQL, userId).first();
			if(account != null)
			{
				response.put("email", 
						new ArrayList<String>()
						{
							{
								add(account.email);
							}
						});
			}
			else
			{
				serviceResponse.addError(ErrorCodes.ACCOUNT_NOT_FOUND.toString(), "No matching user account found.");
			}
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();			
		if(userId != null)
		{
			parameters.put("userId", Long.toString(userId));
		}else
		{
			parameters.put("userId", "null");
		}
		Request request = new Request(isValidRequest, "getDeallrEmail", endTime - startTime, parameters);

		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));
	}
	
	/**
	 * Checks if the email address is duplicate or not.
	 * @param email
	 * @return
	 */
	private static boolean isDuplicateAccount(String email)
	{
		boolean isDuplicate = false;

		//Check if we have the account already
		List<Account> accounts = Account.find("email", email).fetch();
		if(accounts != null && accounts.size() > 0)
		{
			for (Account account : accounts)
			{
				if(account.active && account.password != null)
				{
					isDuplicate = true;
					break;
				}
			}
		}
		return isDuplicate;
	}
	
	/**
	 * Checks if the user already has max allowed email accounts registered and active.
	 * @param userId
	 * @return
	 */
	private static boolean isMaxAccountCountReached(Long userId)
	{
		boolean maxAccountcountReached = false;
		List<Account> accounts = Account.find(ACTIVE_ACCOUNT_LOOKUP_HQL, userId).fetch();
		if(accounts != null && !accounts.isEmpty() && accounts.size() >= MAX_USER_ACCOUNTS)
		{
			maxAccountcountReached = true;
		}
		return maxAccountcountReached;
	}
}
