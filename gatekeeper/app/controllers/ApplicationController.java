package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jsonModels.LoginResponse;
import jsonModels.Message;
import jsonModels.Request;
import jsonModels.Service;
import models.Account;
import models.LoginHistory;
import models.ServiceProvider;
import models.UserInfo;
import models.Users;
import models.enums.ErrorCodes;
import models.enums.Providers;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

import play.Logger;
import play.data.validation.Email;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Password;
import play.data.validation.Required;
import play.data.validation.Validation;
import bl.notifiers.Mails;
import bl.utilities.Utility;

/**
 * The main controller for the application. This is the only controller in the
 * app for now but it will be split up later on as it gets more complicated.
 * 
 * @author prachi
 */
public class ApplicationController extends BaseContoller
{
	private static final String	EMAIL_LOOKUP_HQL					= "SELECT u FROM Account u WHERE u.userInfo.id IS ? AND active IS ? AND registeredEmail IS ?";

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
	 * Login!
	 * @param userId
	 * @param provider
	 * @param email
	 */
	public static void login(@Required(message = "Username or email is required") String username,
			@Required(message = "Password is required") @Password String password)
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
				//Record the login time
				DateTime loginTime = new DateTime(System.currentTimeMillis(), ISOChronology.getInstanceUTC());
				LoginHistory loginHistory = LoginHistory.find("userInfo", userInfo).first();
				if(loginHistory != null)
				{
					loginHistory.lastLoginTime = loginTime.toDate();
				}
				else
				{
					loginHistory = new LoginHistory(userInfo, loginTime.toDate());
				}
				loginHistory.save();
				List<LoginResponse> message = new ArrayList<LoginResponse>();
				message.add(new LoginResponse(Long.toString(userInfo.id),
																			userInfo.username, userInfo.fbFullName,
																			fbAuthToken, hasEmail));
				response.put("user", message);
			}
			else
			{
				serviceResponse.addError(ErrorCodes.NO_SUCH_USER.toString(), "User not registered.");
			}
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();
		if(fbUserId != null)
		{
			parameters.put("fbUserId", Long.toString(fbUserId));
		}else
		{
			parameters.put("fbUserId", "null");
		}
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
														 @Required(message="Facebook auth token is required")String fbAuthToken,
														 @Required(message="Beta invite code missing")String betaToken)
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
			//If a beta token was part of request then verify its valid
			if(betaToken != null)
			{
				serviceResponse = BetaAccessController.verifyToken(betaToken);
			}
			
			if(!serviceResponse.hasErrors())
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
					try
					{
						// Create new user
						String encryptedPassword = password;
						if(password != null)
						{
							encryptedPassword = Utility.encrypt(password);
						}
						
						Date currentDate = new Date(System.currentTimeMillis());
						UserInfo newUser = new UserInfo(username, encryptedPassword,
								Boolean.TRUE, Boolean.FALSE, 
								fbEmailAddress, fbUserId,
								fbFullName, fbLocationName, 
								fbLocationId, gender, currentDate, currentDate,
								getUniqueDeallrEmailAddress(username, fbEmailAddress));	
						newUser.save();
						
						// Save FB auth token
						ServiceProvider provider = ServiceProvider.find("name", Providers.FACEBOOK.toString()).first();
						new Account(newUser, newUser.emailAddress, encryptedPassword, null, null, fbAuthToken, 
								Boolean.FALSE, Boolean.TRUE, "", currentDate, currentDate, currentDate,
								currentDate, provider).save();
						
						//If a beta token was part of request then mark it as used
//						if(betaToken != null)
//						{
//							BetaToken matchingToken = BetaToken.find("token", betaToken).first();
//							if(matchingToken != null)
//							{
//								matchingToken.isUsed = Boolean.TRUE;
//								matchingToken.save();
//							}
//						}
						
						// construct service response
						List<UserInfo> message = new ArrayList<UserInfo>();
						UserInfo recentUser = new UserInfo();
						recentUser.id = newUser.id;
						recentUser.username = newUser.username;
						recentUser.emailAddress = newUser.emailAddress;
						message.add(recentUser);
						response.put("user", message);
					}
					catch (Exception e)
					{
						Logger.error("Error adding user: " + e.getCause() + e.getMessage(), e);
						serviceResponse.addError(ErrorCodes.SERVER_EXCEPTION.toString(), 
								e.getMessage() + e.getCause());
					}
				}
			}
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();			
		parameters.put("username", username);
		parameters.put("password", password);
		parameters.put("gender", gender);
		parameters.put("fbEmailAddress", fbEmailAddress);
		parameters.put("fbFullName", fbFullName);
		if(fbUserId != null)
		{
			parameters.put("fbUserId", Long.toString(fbUserId));
		}else
		{
			parameters.put("fbUserId", "null");
		}
		parameters.put("fbLocationName", fbLocationName);
		if(fbLocationId != null)
		{
			parameters.put("fbLocationId", Long.toString(fbLocationId));
		}else
		{
			parameters.put("fbLocationId", "null");			
		}
		parameters.put("fbAuthToken", fbAuthToken);
		parameters.put("betaToken", betaToken);
		Request request = new Request(isValidRequest, "addUser", endTime - startTime, parameters);

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

		String newEmail = username + "@deallr.com";
		Account duplicate = Account.find("email", newEmail).first();
		if(duplicate != null)
		{
			do
			{
				int cntr = (int) (Math.random() * 77);
				newEmail = username + cntr + "@deallr.com";
				duplicate = Account.find("email", newEmail).first();
			}while(duplicate != null);
		}
		
		//Insert email address into user table for the imap server
		new Users(newEmail, "").save();
		
		//Send welcome email
		Mails.welcome(username, fbEmailAddress);
		
		return newEmail;
	}
	
	public static void sendMails()
	{
		UserInfo user = UserInfo.find("id", 1L).first();
		Mails.welcome(user.username, user.fbEmailAddress);
		Mails.deals(user);
	}
}
