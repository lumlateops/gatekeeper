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
		renderJSON(new Message(new Service(request, new Response(response))));
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
		renderJSON(new Message(new Service(request, new Response(response))));
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
		String returnMessage = "";
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
			renderJSON(new Message(new Service(request, new Response(response))));
		}
		else
		{
			renderJSON(new Message(new Service(request,new Errors(error))));
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
			renderJSON(new Message(new Service(request, response.getServiceResponse())));
		}
		else
		{
			renderJSON(new Message(new Service(request, new Errors(error))));
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
			renderJSON(new Message(new Service(request, response.getServiceResponse())));
		}
		else
		{
			renderJSON(new Message(new Service(request, new Errors(error))));
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
			renderJSON(new Message(new Service(request, response.getServiceResponse())));
		}
		else
		{
			renderJSON(new Message(new Service(request, new Errors(error))));
		}
	}

	/**
	 * Login!
	 * @param userId
	 * @param provider
	 * @param email
	 */
	public static void login(@Required(message = "username is required") String userName,
			@Required(message = "Password is required") @Password @MinSize(5) String password)
	{
		Long startTime = System.currentTimeMillis();

		String returnMessage = "";
		Boolean isValidRequest = Boolean.TRUE;
		Map<String, List<?>> response = new HashMap<String, List<?>>(); 
		List<Error> error = new ArrayList<Error>();
		
		if (!userName.isEmpty() || !userName.trim().isEmpty())
		{
			// Look up by username or FB email
			List<UserInfo> userList = UserInfo.find("userName", userName).fetch();
			if (userList == null || userList.size() == 0)
			{
				userList = UserInfo.find("fbEmailAddress", userName).fetch();
			}
				
			if (userList != null && userList.size() == 1)
			{
				final UserInfo userInfo = userList.get(0);
				if(userInfo.password != null && userInfo.password.equals(password))
				{
					List<Map<String, String>> message = new ArrayList<Map<String, String>>();
					message.add(
						new HashMap<String, String>()
						{
							{
								put("id", userInfo.id.toString());
								put("name", userInfo.userName);
							}
						});
					response.put("User", message);
				}
				else
				{
					returnMessage = "User name and password do not match.";
					error.add(new Error(ErrorCodes.AUTHENTICATION_FAILED.toString(), returnMessage));
				}
			}
			else
			{
				returnMessage = "User not registered.";
				error.add(new Error(ErrorCodes.NO_SUCH_USER.toString(), returnMessage));
			}
		}
		else
		{
			isValidRequest = Boolean.FALSE;
			error.add(new Error(ErrorCodes.INVALID_REQUEST.toString(), "Invalid Request"));
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("userName", userName);
		parameters.put("password", password);
		Request request = new Request(isValidRequest, "login", endTime - startTime, parameters);
		
		if(!response.isEmpty())
		{
			renderJSON(new Message(new Service(request, new Response(response))));
		}
		else
		{
			renderJSON(new Message(new Service(request,new Errors(error))));
		}
	}

	/**
	 * 
	 * @param userName
	 */
	public static void checkUserNameAvailable(
			@Required(message = "UserName is required") @MinSize(4) @MaxSize(100) String userName)
	{
		Long startTime = System.currentTimeMillis();
		Boolean isValidRequest = Boolean.TRUE;
		
		List<Error> error = new ArrayList<Error>();
		Map<String, List<?>> response = new HashMap<String, List<?>>(); 

		if (!userName.isEmpty() || !userName.trim().isEmpty())
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
		else
		{
			isValidRequest = Boolean.FALSE;
			error.add(new Error(ErrorCodes.INVALID_REQUEST.toString(), "UserName is required"));
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("userName", userName);
		Request request = new Request(isValidRequest, "checkUserNameAvailable", endTime-startTime, parameters);
		
		if(!response.isEmpty())
		{
			renderJSON(new Message(new Service(request, new Response(response))));
		}
		else
		{
			renderJSON(new Message(new Service(request, new Errors(error))));
		}
	}

	/**
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
	public static void addUser(@Required(message="UserName is required") @MinSize(4) @MaxSize(100) String userName, 
										@Required(message="Password is required") @MinSize(5) @Password String password,
										@Required(message="Zipcode is required") @MinSize(5) int zipCode,
										@Required(message="First Name is required") @MinSize(3) @MaxSize(100) String firstName,
										@Required(message="Last Name is required") @MinSize(3) @MaxSize(100) String lastName,
										@Required(message="Gender is required") @Min(0) @Max(1) int gender,
										@Required(message="Facebook Email is required") @Email @MinSize(7) @MaxSize(100) String fbEmailAddress,
										@Required(message="Facebook Id is required") @MinSize(5) long fbUserId) throws ParseException
	{
		Long startTime = System.currentTimeMillis();
		Boolean isValidRequest = Boolean.TRUE;
		boolean isadmin = false;
		
		List<Error> error = new ArrayList<Error>();
		Map<String, List<?>> response = new HashMap<String, List<?>>(); 
		
		String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		Date date = new Date(System.currentTimeMillis());
		Date currentDate = formatter.parse(formatter.format(date));
		
		if(!userName.trim().isEmpty() && !password.trim().isEmpty() && 
				!firstName.trim().isEmpty() && !lastName.trim().isEmpty())
		{
			List<UserInfo> userInfo = UserInfo.find("userName", userName).fetch();
			if(userInfo.size() > 0)
			{
				error.add(new Error(ErrorCodes.DUPLICATE_USER.toString(), "This username is already registered."));
			}
			else
			{
				final UserInfo newUser = new UserInfo(userName, firstName, lastName,
																				password, Boolean.TRUE, Boolean.FALSE, 
																				zipCode, fbEmailAddress, fbUserId, 
																				gender, currentDate, currentDate);
				newUser.save();
				List<Map<String, String>> message = new ArrayList<Map<String, String>>();
				message.add(
					new HashMap<String, String>()
					{
						{
							put("id", newUser.id.toString());
							put("name", newUser.userName);
						}
					});
				response.put("User", message);
			}
		}
		else
		{
			isValidRequest = false;
			error.add(new Error(ErrorCodes.INVALID_REQUEST.toString(), "Required parameter missing."));
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();			
		parameters.put("userName", userName);
		parameters.put("password",password);
		parameters.put("zipCode",Integer.toString(zipCode));
		parameters.put("gender",Integer.toString(gender));
		parameters.put("firstName",firstName);
		parameters.put("lastName",lastName);
		parameters.put("createdAt", currentDate.toString());
		parameters.put("updatedAt", currentDate.toString());
		parameters.put("fbEmailAddress",fbEmailAddress);
		parameters.put("fbUserId",Long.toString(fbUserId));
		Request request = new Request(isValidRequest, "login", endTime - startTime, parameters);
		
		if(!response.isEmpty())
		{
			renderJSON(new Message(new Service(request, new Response(response))));
		}
		else
		{
			renderJSON(new Message(new Service(request,new Errors(error))));
		}
	}
}