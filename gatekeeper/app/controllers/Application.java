package controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jsonModels.DealEmailResponse;
import jsonModels.LoginResponse;
import jsonModels.Message;
import jsonModels.Request;
import jsonModels.Service;
import models.Account;
import models.Deal;
import models.ErrorCodes;
import models.FetchHistory;
import models.Providers;
import models.ServiceProvider;
import models.SortFields;
import models.SortOrder;
import models.UserInfo;
import play.Logger;
import play.data.validation.Email;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Password;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import bl.googleAuth.GmailProvider;

/**
 * The main controller for the application. This is the only controller in the
 * app for now but it will be split up later on as it gets more complicated.
 * 
 * @author prachi
 */
public class Application extends Controller
{
	private static final int		PAGE_SIZE									= 20;
	private static final String	EMAIL_LOOKUP_HQL					= "SELECT u FROM Account u WHERE u.userId IS ? AND active IS ? AND registeredEmail IS ?";
	private static final String	USER_DEAL_LOOKUP_HQL			= "SELECT d AS d FROM Deal d WHERE d.userId IS ? ORDER BY ";
	private static final String	DEAL_LOOKUP_HQL						= "SELECT d AS d FROM Deal d WHERE d.userId IS ? AND d.id IN ";
	private static final String	UNREAD_DEAL_LOOKUP_HQL		= "SELECT d AS d FROM Deal d WHERE d.userId IS ? AND d.dealRead='false' ";
	private static final String	ACCOUNT_LOOKUP_HQL				= "SELECT u FROM Account u WHERE u.userId IS ? AND u.provider IS ? ";
	private static final String	FETCH_HISTORY_LOOKUP_HQL	= "SELECT f FROM FetchHistory f WHERE f.userId IS ? and f.fetchStatus='complete'";// and f.fetchEndTime<=currentTime-60";

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
		Map<String, List<?>> response = new HashMap<String, List<?>>(); 
		response.put("providers", providers);

		Service serviceResponse = new Service();
		serviceResponse.setRequest(request);
		serviceResponse.setResponse(response);
		renderJSON(new Message(serviceResponse));
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
		Map<String, List<?>> response = new HashMap<String, List<?>>(); 
		response.put("providers", activeProviders);

		Service serviceResponse = new Service();
		serviceResponse.setRequest(request);
		serviceResponse.setResponse(response);
		renderJSON(new Message(serviceResponse));
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
						GmailProvider.authorizeAccount(userId, email, password);
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
	 * Login!
	 * @param userId
	 * @param provider
	 * @param email
	 */
	public static void login(@Required(message = "Username or email is required") String username,
			@Required(message = "Password is required") @Password String password)
	{
		String authenticityToken = session.getAuthenticityToken();
		Logger.debug(authenticityToken);
		
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
			// Look up by username or Deallr email or FB email
			List<UserInfo> userList = UserInfo.find("username", username).fetch();
			if (userList == null || userList.size() == 0)
			{
				userList = UserInfo.find("emailAddress", username).fetch();
				if (userList == null || userList.size() == 0)
				{
					userList = UserInfo.find("fbEmailAddress", username).fetch();
				}
			}
				
			if (userList != null && userList.size() == 1)
			{
				final UserInfo userInfo = userList.get(0);
				if(userInfo.password != null && userInfo.password.equals(password))
				{
					//Check if the user has any registered email accounts
					Boolean hasEmail = Boolean.FALSE;
					List<Account> accounts = Account.find(EMAIL_LOOKUP_HQL, userInfo.id, Boolean.TRUE, Boolean.TRUE).fetch();
					//Check if atleast one email account registered
					if(accounts != null && accounts.size() > 0)
					{
						hasEmail = Boolean.TRUE;
					}
					//Get the fbAuthToken
					String fbAuthToken = "";
					for (Account account : accounts)
					{
						if(account.provider.name.equalsIgnoreCase(Providers.FACEBOOK.toString()))
						{
							fbAuthToken = account.dllrAccessToken;
							break;
						}
					}
					List<LoginResponse> message = new ArrayList<LoginResponse>();
					message.add(new LoginResponse(Long.toString(userInfo.id),
																				userInfo.username, userInfo.fbFullName,
																				fbAuthToken, hasEmail));
					response.put("user", message);
				}
				else
				{
					serviceResponse.addError(ErrorCodes.AUTHENTICATION_FAILED.toString(), "User name and password do not match.");
				}
			}
			else
			{
				serviceResponse.addError(ErrorCodes.NO_SUCH_USER.toString(), "User not registered.");
			}
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("username", username);
		parameters.put("password", password);
		Request request = new Request(isValidRequest, "login", endTime - startTime, parameters);
		
		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		
		renderJSON(new Message(serviceResponse));
	}
	
	/**
	 * Login using the FBUserId
	 * @param fbUserId
	 */
	public static void fbIdlogin(@Required(message = "Facebook Id is required") Long fbUserId)
	{
		String authenticityToken = session.getAuthenticityToken();
		Logger.debug(authenticityToken);
		
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
			// Look up by FB ID
			List<UserInfo> userList = UserInfo.find("fbUserId", fbUserId).fetch();
				
			if (userList != null && userList.size() == 1)
			{
				final UserInfo userInfo = userList.get(0);
				//Check if the user has any registered email accounts
				Boolean hasEmail = Boolean.FALSE;
				List<Account> accounts = Account.find(EMAIL_LOOKUP_HQL, userInfo.id, Boolean.TRUE, Boolean.TRUE).fetch();
				if(accounts != null && accounts.size() > 0)
				{
					hasEmail = Boolean.TRUE;
				}
				//Get the FBAuthToken from registered accounts
				String fbAuthToken = "";
				for (Account account : accounts)
				{
					if(account.provider.id == Providers.FACEBOOK.ordinal()+1)
					{
						fbAuthToken = account.dllrAccessToken;
					}
				}
				//Find the user's FB account
				List<LoginResponse> message = new ArrayList<LoginResponse>();
				message.add(new LoginResponse(Long.toString(userInfo.id),
																			userInfo.username, userInfo.fbFullName,
																			fbAuthToken, hasEmail));
				response.put("user", message);
				
				// Add email address to queue if no fetch happened within the last 60 mins
//				FetchHistory lastFetch = FetchHistory.find(FETCH_HISTORY_LOOKUP_HQL, userInfo.id).first();
//				if(lastFetch==null)
//				{
//					ServiceProvider gmailProvider = ServiceProvider.find("name", Providers.GMAIL.toString()).first();
//					NewAccountMessage rmqMessage = new NewAccountMessage(userInfo.id, email, token, 
//																														account.dllrTokenSecret,
//																														Providers.GMAIL.toString(),	
//																														gmailProvider.consumerKey, 
//																														gmailProvider.consumerSecret);
//					RMQProducer.publishNewEmailAccountMessage(rmqMessage);
//				}
			}
			else
			{
				serviceResponse.addError(ErrorCodes.NO_SUCH_USER.toString(), "User not registered.");
			}
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("fbUserId", Long.toString(fbUserId));
		Request request = new Request(isValidRequest, "fbIdlogin", endTime - startTime, parameters);
		
		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		
		renderJSON(new Message(serviceResponse));
	}

	/**
	 * Checks to see if the username is already taken.
	 * @param userName
	 */
	public static void checkUsernameAvailable(@Required(message = "UserName is required") @MinSize(4) @MaxSize(100) String username)
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
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), 
																 validationError.message());
			}
		}
		else
		{
			List<UserInfo> userInfo = UserInfo.find("userName", username).fetch(1);
			
			if (userInfo.size() > 0)
			{
				response.put("isAvailable", 
						new ArrayList<String>()
						{
							{
								add(Boolean.FALSE.toString());
							}
						});
			}
			else
			{
				response.put("isAvailable", 
						new ArrayList<String>()
						{
							{
								add(Boolean.TRUE.toString());
							}
						});
			}
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("username", username);

		serviceResponse.setRequest(new Request(isValidRequest, "checkUsernameAvailable", 
																					 endTime-startTime, parameters));
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));
	}

	/**
	 * 
	 * @param userName
	 * @param password
	 * @param gender
	 * @param fbEmailAddress
	 * @param fbFullName
	 * @param fbUserId
	 * @param fbLocationName
	 * @param fbLocationId
	 */
	public static void addUser(@MinSize(4) @MaxSize(100) String username,
														 @MinSize(5) @Password String password,
														 String gender,
														 @Required(message="Facebook Email is required") @Email String fbEmailAddress,
														 @Required(message="Facebook name is required") String fbFullName,
														 @Required(message="Facebook Id is required") @MinSize(5) Long fbUserId,
														 String fbLocationName,
														 Long fbLocationId,
														 @Required(message="Facebook auth token is required")String fbAuthToken)
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
			List<UserInfo> userInfo = Collections.emptyList();
			if(username != null && password != null)
			{
				userInfo = UserInfo.find("userName", username).fetch();
			}
			else
			{
				userInfo = UserInfo.find("fbUserId", fbUserId).fetch();
			}
			
			if(userInfo != null && userInfo.size() > 0)
			{
				serviceResponse.addError(ErrorCodes.DUPLICATE_USER.toString(), "This username is already registered.");
			}
			else
			{
				String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
				SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
				Date currentDate = null;
				try
				{
					currentDate = formatter.parse(formatter.format(new Date(System.currentTimeMillis())));
				}
				catch (ParseException e)
				{
					//Should not happen, go ahead without a date
				}
				// Create new user
				UserInfo newUser = new UserInfo(username, password, Boolean.TRUE, Boolean.FALSE, 
						fbEmailAddress, fbUserId, fbFullName, fbLocationName, fbLocationId,
					  gender, currentDate, currentDate, getUniqueDeallrEmailAddress(username, fbEmailAddress));
				newUser.save();
				
				// Save FB auth token
				ServiceProvider provider = ServiceProvider.find("name", Providers.FACEBOOK.toString()).first();
			  new Account(newUser.id, newUser.emailAddress, password, "", "", fbAuthToken, 
			  						Boolean.FALSE, Boolean.TRUE, "", currentDate, currentDate, currentDate,
										currentDate, provider).save();
				
				Logger.info(Account.find("email", newUser.emailAddress).first().toString());
				
				// construct service response
				List<UserInfo> message = new ArrayList<UserInfo>();
				UserInfo recentUser = new UserInfo();
				recentUser.id = newUser.id;
				recentUser.username = newUser.username;
				recentUser.emailAddress = newUser.emailAddress;
				message.add(recentUser);
				response.put("user", message);
			}
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();			
		parameters.put("username", username);
		parameters.put("password",password);
		parameters.put("gender", gender);
		parameters.put("fbEmailAddress", fbEmailAddress);
		parameters.put("fbFullName", fbFullName);
		parameters.put("fbUserId", Long.toString(fbUserId));
		parameters.put("fbLocationName", fbLocationName);
		parameters.put("fbLocationId", Long.toString(fbLocationId));
		parameters.put("fbAuthToken", fbAuthToken);
		Request request = new Request(isValidRequest, "addUser", endTime - startTime, parameters);

		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
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
	
	/**
	 * Generated a deallr email address for the user being added
	 * @param username
	 * @param fbEmailAddress
	 * @return
	 */
	private static String getUniqueDeallrEmailAddress(String username, String fbEmailAddress)
	{
		if(username == null || username.trim().isEmpty())
		{
			username = fbEmailAddress.substring(0,fbEmailAddress.indexOf("@"));
		}
		
		return username + "@deallr.com";
	}
	
	/**
	 * End point to get the deals for a user
	 * @param userId: Id of the user we want to get deals for.
	 * @param page: Result page number to return. Default is 1.
	 * @param sort: The field to sort the results by. Possible values are {@code SortFields}. Default is POST_DATE
	 * @param sortOrder: Order of sort. Possible values are {@code SortOrder}. Default is DESC
	 */
	public static void getUserDeals(@Required(message="userId is required")Long userId,
																	int page, String sort, String sortOrder)
	{
		Long startTime = System.currentTimeMillis();
		Boolean isValidRequest = Boolean.TRUE;
		
		Service serviceResponse = new Service();
		Map<String, List<?>> response = new HashMap<String, List<?>>(); 
		
		// Validate input
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
			//Set defaults
			if(page <= 0)
			{
				page = 1;
			}
			if(sort == null || SortFields.valueOf(sort) == null)
			{
				sort = SortFields.postDate.toString();
			}
			if(sortOrder == null || SortOrder.valueOf(sortOrder) == null)
			{
				sortOrder = SortOrder.DESC.toString();
			}
			
			// Get deals for user
			String query = USER_DEAL_LOOKUP_HQL + sort + " " + sortOrder;
			final List<Deal> allDeals = Deal.find(query, userId).fetch();
			final int allDealsCount = allDeals.size();

			if(allDeals != null && allDealsCount != 0)
			{
				// Adjust result for page number
				int startIndex = PAGE_SIZE * (page - 1);
				int endIndex = ((startIndex + PAGE_SIZE) <= allDealsCount) ? (startIndex + PAGE_SIZE) : allDealsCount;

				if(startIndex >= endIndex)
				{
					response.put("numberOfResults", 
							new ArrayList<String>()
							{
								{
									add("0");
								}
							});
				}
				else
				{
					final List<Deal> onePageDeals = allDeals.subList(startIndex, endIndex);
					if(onePageDeals != null && onePageDeals.size() != 0)
					{
						response.put("numberOfResults", 
								new ArrayList<String>()
								{
									{
										add(Integer.toString(allDealsCount));
									}
								});
						final int pageCount = (allDealsCount/PAGE_SIZE) > 0 ? (allDealsCount/PAGE_SIZE) : 1;
						response.put("numberOfPages", 
								new ArrayList<String>()
								{
									{
										
										add(Integer.toString(pageCount));
									}
								});
						response.put("deals", onePageDeals);
					}
					else
					{
						response.put("numberOfResults", 
								new ArrayList<String>()
								{
									{
										add("0");
									}
								});
					}
				}
			}
			else
			{
				response.put("numberOfResults", 
						new ArrayList<String>()
						{
							{
								add("0");
							}
						});
			}
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();
		if(userId != null)
		{
			parameters.put("userId", Long.toString(userId));
		}
		parameters.put("page", Integer.toString(page));
		parameters.put("sort", sort);
		parameters.put("sortOrder", sortOrder);
		Request request = new Request(isValidRequest, "getUserDeals", endTime - startTime, parameters);

		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));	
	}
	
	/**
	 * End point to mark deals as read.
	 * @param userId: Id of the user we want to get deals for.
	 * @param dealIds: Ids of the deals to be marked as read.
	 */
	public static void markDealsRead(@Required(message="userId is required")Long userId,
																	@Required(message="dealIds are required")String dealIds)
	{
		Long startTime = System.currentTimeMillis();
		Boolean isValidRequest = Boolean.TRUE;
		
		Service serviceResponse = new Service();
		Map<String, List<?>> response = new HashMap<String, List<?>>(); 
		
		// Validate input
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
			String queryString = DEAL_LOOKUP_HQL + "(" + dealIds + ")";
			List<Deal> deals = Deal.find(queryString, userId).fetch();
			if(deals != null && !deals.isEmpty())
			{
				for (Deal deal : deals)
				{
					deal.dealRead = true;
					deal.save();
				}
				
				response.put("status", 
						new ArrayList<String>()
						{
							{
								add("ok");
							}
						});
			}
			else
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), "No matching deal found.");
			}
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();
		if(userId != null)
		{
			parameters.put("userId", Long.toString(userId));
		}
		if(dealIds != null)
		{
			parameters.put("dealIds", dealIds);
		}
		Request request = new Request(isValidRequest, "markDealRead", endTime - startTime, parameters);

		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));	
	}
	
	/**
	 * End point to mark a user deals as unread.
	 * @param userId: Id of the user we want to get deals for.
	 * @param dealIds: Id of the user deals to be marked as unread.
	 */
	public static void markDealsUnRead(@Required(message="userId is required")Long userId,
																		 @Required(message="dealIds are required")String dealIds)
	{
		Long startTime = System.currentTimeMillis();
		Boolean isValidRequest = Boolean.TRUE;
		
		Service serviceResponse = new Service();
		Map<String, List<?>> response = new HashMap<String, List<?>>(); 
		
		// Validate input
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
			String queryString = DEAL_LOOKUP_HQL + "(" + dealIds + ")";
			List<Deal> deals = Deal.find(queryString, userId).fetch();
			if(deals != null && !deals.isEmpty())
			{
				for (Deal deal : deals)
				{
					deal.dealRead = false;
					deal.save();
				}
				
				response.put("status", 
						new ArrayList<String>()
						{
							{
								add("ok");
							}
						});
			}
			else
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), "No matching deal found.");
			}
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();
		if(userId != null)
		{
			parameters.put("userId", Long.toString(userId));
		}
		if(dealIds != null)
		{
			parameters.put("dealIds", dealIds);
		}
		Request request = new Request(isValidRequest, "markDealsUnRead", endTime - startTime, parameters);

		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));	
	}
	
	/**
	 * End point to mark a user deals as unread.
	 * @param userId: Id of the user we want to get deals for.
	 * @param dealIds: Id of the user deals to be marked as unread.
	 */
	public static void getUnreadDealCount(@Required(message="userId is required")Long userId)
	{
		Long startTime = System.currentTimeMillis();
		Boolean isValidRequest = Boolean.TRUE;
		
		Service serviceResponse = new Service();
		Map<String, List<?>> response = new HashMap<String, List<?>>(); 
		
		// Validate input
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
			List<Deal> deals = Deal.find(UNREAD_DEAL_LOOKUP_HQL, userId).fetch();
			final int count;
			if(deals != null && !deals.isEmpty())
			{
				count = deals.size();
			}
			else
			{
				count = 0;
			}
			response.put("count", 
					new ArrayList<String>()
					{
						{
							add(Integer.toString(count));
						}
					});
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();
		if(userId != null)
		{
			parameters.put("userId", Long.toString(userId));
		}
		Request request = new Request(isValidRequest, "getUnreadDealCount", endTime - startTime, parameters);

		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));	
	}
	
	/**
	 * Returns the email address the deal was received on.
	 * @param dealId: Id of the deal we want info for.
	 */
	public static void getDealUserEmail(@Required(message="dealId is required")Long dealId)
	{
		Long startTime = System.currentTimeMillis();
		Boolean isValidRequest = Boolean.TRUE;
		
		Service serviceResponse = new Service();
		Map<String, List<?>> response = new HashMap<String, List<?>>(); 
		
		// Validate input
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
			Deal deal = Deal.find("id", dealId).first();
			if(deal != null && deal.subscription != null)
			{
				Account account = Account.find("id", deal.subscription.accountId).first();
				if(account != null)
				{
					List<DealEmailResponse> message = new ArrayList<DealEmailResponse>();
					message.add(new DealEmailResponse(dealId, account.email, deal.postDate));
					response.put("dealInfo", message);
				}
				else
				{
					serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), "No matching Account found");
				}
			}
			else
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), "No matching deal found");
			}
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();
		if(dealId != null)
		{
			parameters.put("dealId", Long.toString(dealId));
		}
		Request request = new Request(isValidRequest, "getDealUserEmail", endTime - startTime, parameters);

		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));	
	}
}