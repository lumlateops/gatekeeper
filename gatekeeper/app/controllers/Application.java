package controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gdata.client.authn.oauth.OAuthException;

import jsonModels.Errors;
import jsonModels.LoginResponse;
import jsonModels.Message;
import jsonModels.Parameter;
import jsonModels.Response;
import jsonModels.Service;
import jsonModels.Request;
import jsonModels.Error;
import jsonModels.ServiceResponse;

import bl.googleAuth.AuthPayLoad;
import bl.googleAuth.GmailProvider;

import play.Logger;
import play.data.validation.Email;
import play.data.validation.IsTrue;
import play.data.validation.Max;
import play.data.validation.MaxSize;
import play.data.validation.Min;
import play.data.validation.MinSize;
import play.data.validation.Password;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Scope.Params;

import models.Account;
import models.EmailProviders;
import models.ErrorCodes;
import models.ServiceProvider;
import models.UserInfo;

/**
 * The main controller for the application. This is the only controller in the
 * app for now but it will be split up later on as it gets more complicated.
 * 
 * @author prachi
 */
public class Application extends Controller
{
	private static final String	EMAIL_LOOKUP_HQL	= "SELECT u FROM Account u WHERE u.userId IS ? AND active IS ?";

	@Before
	public static void logRequest()
	{
		Logger.debug("##############BEGIN REQUEST INFO##############");
		play.mvc.Http.Request currentRequest = play.mvc.Http.Request.current();
		Logger.debug("Request end point: " + currentRequest.action);
		Map<String, String[]> requestParams = currentRequest.params.all();
		for (String key : requestParams.keySet())
		{
			Logger.debug(key + ": '"+ requestParams.get(key)[0] + "'");
		}
		Logger.debug("##############END REQUEST INFO##############");
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
		response.put("Providers", providers);

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
		response.put("Providers", activeProviders);

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
	public static void addEmail(@Required(message="UserId is required") String userId,
			@Required(message="Email provider is required") String provider,
			@Required(message="Email is required") String email)
	{
		Long startTime = System.currentTimeMillis();

		Boolean isValidRequest = Boolean.TRUE;
		String returnMessage = "";/**
		 * 
		 * @param userName
		 * @param password
		 * @param zipCode
		 * @param firstName
		 * @param lastName
		 * @param gender
		 * @param fbEmailAddress
		 * @param fbUserId
		 * @throws ParseException
		 */
		Logger.debug("Authorize Email Called:" + userId + "/" + provider + "/" + email);

		//TODO: Validate input

		List<Error> error = new ArrayList<Error>();
		Map<String, List<?>> response = new HashMap<String, List<?>>(); 
		
		// Go to correct provider
		if(provider != null && EmailProviders.GMAIL.toString().equalsIgnoreCase(provider.trim()))
		{
			Logger.debug("AE: Provider is Gmail");

			//Check if account exists and is still valid
			boolean isDuplicate = GmailProvider.isDuplicateAccount(userId, email);

			Logger.debug("AE: isDuplicate: " + isDuplicate);

			//Account not present or invalid, then get a new one and store it
			if(!isDuplicate)
			{
				try
				{
					String authUrl = GmailProvider.authorizeAccount(userId, email);
					Logger.debug("Auth url: "+authUrl);
					List<String> authMessage = new ArrayList<String>();
					authMessage.add(authUrl);
					response.put("AuthUrl", authMessage);
				}
				catch (OAuthException e)
				{
					returnMessage = e.getMessage() + e.getCause();
					error.add(new Error(ErrorCodes.OAUTH_EXCEPTION.toString(), returnMessage));
				}
			}
			else
			{
				returnMessage = "Account registered and authorized already";
				error.add(new Error(ErrorCodes.DUPLICATE_ACCOUNT.toString(), returnMessage));
			}
		}
		else
		{
			error.add(new Error(ErrorCodes.UNSUPPORTED_PROVIDER.toString(), provider + " not supported."));
		}
		Long endTime = System.currentTimeMillis();

		Map<String, String>	parameters = new HashMap<String, String>();
		parameters.put("userId", userId);
		parameters.put("provider", provider);
		parameters.put("email", email);
		Request request = new Request(isValidRequest, "addEmail", endTime-startTime, parameters);
		
		if(!response.isEmpty())
		{
//			renderJSON(new Message(new Service(request, new Response(response))));
		}
		else
		{
//			renderJSON(new Message(new Service(request,new Errors(error))));
		}
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
		List<Error> error = new ArrayList<Error>();
		ServiceResponse response = null;

		// Go to correct provider
		if(provider != null && EmailProviders.GMAIL.toString().equalsIgnoreCase(provider.trim()))
		{
			//Upgrade to access token and store the account
			response = GmailProvider.upgradeToken(userId, email, queryString);
		}
		else
		{
			error.add(new Error(ErrorCodes.UNSUPPORTED_PROVIDER.toString(), provider + " not supported."));
		}
		Long endTime = System.currentTimeMillis();

		Map<String, String>	parameters = new HashMap<String, String>();
		parameters.put("userId", userId);
		parameters.put("provider", provider);
		parameters.put("email", email);
		parameters.put("queryString", queryString);
		Request request = new Request(Boolean.TRUE, "upgradeToken", endTime-startTime, parameters);
		
		if(response != null)
		{
//			renderJSON(new Message(new Service(request, response.getServiceResponse())));
		}
		else
		{
//			renderJSON(new Message(new Service(request, new Errors(error))));
		}
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

		List<Error> error = new ArrayList<Error>();
		ServiceResponse response = null;

		// Go to correct provider
		if(provider != null && EmailProviders.GMAIL.toString().equalsIgnoreCase(provider.trim()))
		{
			//Upgrade to access token and store the account
			response = GmailProvider.revokeAccess(userId, password, email);
		}
		else
		{
			error.add(new Error(ErrorCodes.UNSUPPORTED_PROVIDER.toString(), provider + " not supported."));
		}
		Long endTime = System.currentTimeMillis();

		Map<String, String>	parameters = new HashMap<String, String>();
		parameters.put("userId", userId);
		parameters.put("provider", provider);
		parameters.put("email", email);
		Request request = new Request(Boolean.TRUE, "revokeAccess", endTime-startTime, parameters);
		
		if(response != null)
		{
//			renderJSON(new Message(new Service(request, response.getServiceResponse())));
		}
		else
		{
//			renderJSON(new Message(new Service(request, new Errors(error))));
		}
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
		
		List<Error> error = new ArrayList<Error>();
		ServiceResponse response = null;

		// Go to correct provider
		if(provider != null && EmailProviders.GMAIL.toString().equalsIgnoreCase(provider.trim()))
		{
			//Upgrade to access token and store the account
			response = GmailProvider.isAccountAuthorized(userId, email);
		}
		else
		{
			error.add(new Error(ErrorCodes.UNSUPPORTED_PROVIDER.toString(), provider + " not supported."));
		}
		Long endTime = System.currentTimeMillis();

		Map<String, String>	parameters = new HashMap<String, String>();
		parameters.put("userId", userId);
		parameters.put("provider", provider);
		parameters.put("email", email);
		Request request = new Request(Boolean.TRUE, "revokeAccess", endTime-startTime, parameters);
		if(response != null)
		{
//			renderJSON(new Message(new Service(request, response.getServiceResponse())));
		}
		else
		{
//			renderJSON(new Message(new Service(request, new Errors(error))));
		}
	}

	/**
	 * Login!
	 * @param userId
	 * @param provider
	 * @param email
	 */
	public static void login(@Required(message = "username is required") String username,
			@Required(message = "Password is required") @Password @MinSize(5) String password)
	{
//		Long startTime = System.currentTimeMillis();
//
//		Boolean isValidRequest = Boolean.TRUE;
//		Service serviceResponse = new Service();
//		Map<String, List<?>> response = new HashMap<String, List<?>>();
//		
//		if(Validation.hasErrors())
//		{
//			isValidRequest = false;
//			for (play.data.validation.Error validationError : Validation.errors())
//			{
//				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), validationError.getKey() + ":" + validationError.message());
//			}
//		}
//		else
//		{
//			// Look up by username or FB email
//			List<UserInfo> userList = UserInfo.find("username", username).fetch();
//			if (userList == null || userList.size() == 0)
//			{
//				userList = UserInfo.find("fbEmailAddress", username).fetch();
//			}
//				
//			if (userList != null && userList.size() == 1)
//			{
//				final UserInfo userInfo = userList.get(0);
//				if(userInfo.password != null && userInfo.password.equals(password))
//				{
//					List<Map<String, String>> message = new ArrayList<Map<String,String>>();
//					message.add(
//						new HashMap<String, String>()
//						{
//							{
//								put("id", userInfo.id.toString());
//								put("name", userInfo.username);
//							}
//						});
//					response.put("User", message);
//				}
//				else
//				{
//					returnMessage = "User name and password do not match.";
//					error.add(new Error(ErrorCodes.AUTHENTICATION_FAILED.toString(), returnMessage));
//				}
//			}
//			else
//			{
//				returnMessage = "User not registered.";
//				error.add(new Error(ErrorCodes.NO_SUCH_USER.toString(), returnMessage));
//			}
//		}
//		
//		else
//		{
//			isValidRequest = Boolean.FALSE;
//			error.add(new Error(ErrorCodes.INVALID_REQUEST.toString(), "Invalid Request"));
//		}
//		Long endTime = System.currentTimeMillis();
//		
//		Map<String, String> parameters = new HashMap<String, String>();
//		parameters.put("username", username);
//		parameters.put("password", password);
//		Request request = new Request(isValidRequest, "login", endTime - startTime, parameters);
//		
//		if(!response.isEmpty())
//		{
////			renderJSON(new Message(new Service(request, new Response(response))));
//		}
//		else
//		{
////			renderJSON(new Message(new Service(request,new Errors(error))));
//		}
	}
	
	/**
	 * Login using the FBUserId
	 * @param fbUserId
	 */
	public static void fbIdlogin(@Required(message = "Facebook Id is required") Long fbUserId)
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
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), validationError.getKey() + ":" + validationError.message());
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
				List<Account> accounts = Account.find(EMAIL_LOOKUP_HQL, userInfo.id, Boolean.TRUE).fetch();
				if(accounts != null && accounts.size() > 0)
				{
					hasEmail = Boolean.TRUE;
				}
				List<LoginResponse> message = new ArrayList<LoginResponse>();
				message.add(new LoginResponse(Long.toString(userInfo.id),
																			userInfo.username, hasEmail));
				response.put("user", message);
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
	public static void checkUserNameAvailable(
			@Required(message = "UserName is required") @MinSize(4) @MaxSize(100) String userName)
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
																 validationError.getKey() + ":" + validationError.message());
			}
		}
		else
		{
			List<UserInfo> userInfo = UserInfo.find("userName", userName).fetch(1);
			
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
		parameters.put("userName", userName);

		serviceResponse.setRequest(new Request(isValidRequest, "checkUserNameAvailable", 
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
	public static void addUser(@Required(message="UserName is required") @MinSize(4) @MaxSize(100) String username, 
										@Required(message="Password is required") @MinSize(5) @Password String password,
										@Required(message="Gender is required") String gender,
										@Required(message="Facebook Email is required") @Email String fbEmailAddress,
										@Required(message="Facebook name is required") String fbFullName,
										@Required(message="Facebook Id is required") @MinSize(5) Long fbUserId,
										@Required(message="Facebook location name is required") String fbLocationName,
										@Required(message="Facebook location Id is required")Long fbLocationId)
	{
		Long startTime = System.currentTimeMillis();
		Boolean isValidRequest = Boolean.TRUE;
		
		Service serviceResponse = new Service();
		Map<String, List<?>> response = new HashMap<String, List<?>>(); 
		
		String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		Date date = new Date(System.currentTimeMillis());
		Date currentDate = null;
		try
		{
			currentDate = formatter.parse(formatter.format(date));
		}
		catch (ParseException e)
		{
			//Should not happen, go ahead without a date
		}
		
		if(Validation.hasErrors())
		{
			isValidRequest = false;
			for (play.data.validation.Error validationError : Validation.errors())
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), validationError.getKey() + ":" + validationError.message());
			}
		}
		else
		{
			List<UserInfo> userInfo = UserInfo.find("userName", username).fetch();
			if(userInfo.size() > 0)
			{
				serviceResponse.addError(ErrorCodes.DUPLICATE_USER.toString(), "This username is already registered.");
			}
			else
			{
				UserInfo newUser = new UserInfo(username, password, Boolean.TRUE, Boolean.FALSE, 
						fbEmailAddress, fbUserId, fbFullName, fbLocationName, fbLocationId,
					  gender, currentDate, currentDate, username+"@deallr.com");
				newUser.save();
				
				List<UserInfo> message = new ArrayList<UserInfo>();
				UserInfo recentUser = new UserInfo();
				recentUser.id = newUser.id;
				recentUser.username = newUser.username;
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
		Request request = new Request(isValidRequest, "addUser", endTime - startTime, parameters);

		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));
	}
}