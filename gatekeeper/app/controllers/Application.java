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
import jsonModels.Service;
import jsonModels.Request;
import jsonModels.Error;

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
import play.mvc.Controller;

import models.Account;
import models.EmailProviders;
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
		renderJSON(new Message(new Service(request, response)));
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
		renderJSON(new Message(new Service(request, response)));
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
		Request request = new Request(isSuccess, "addEmail", endTime-startTime, parameters);

//		renderJSON(new Message(new Service(request, returnMessage)));
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
//		renderJSON(new Message(new Service(request, returnMessage)));
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
//		renderJSON(new Message(new Service(request, returnMessage)));
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
//		renderJSON(new Message(new Service(request, isValid)));
	}

	/**
	 * Login!
	 * @param userId
	 * @param provider
	 * @param email
	 */
	public static void login(@Required(message="username is required") String userName,
			@Required(message="Password is required") @Password @MinSize(5) String password)
	{
		Long startTime = System.currentTimeMillis();
		boolean isValid = false;
		Map<String, String>	parameters = new HashMap<String, String>();
		parameters.put("userName", userName);
		parameters.put("password", password);
		if(!userName.isEmpty() || !userName.trim().isEmpty())
		{
			isValid=true;
			List<UserInfo> userinfo=null;
			userinfo=UserInfo.find("userName",userName).fetch(1);
			if(userinfo.size()>0){
				if(UserInfo.find("SELECT u FROM UserInfo u where u.password is ?",password).fetch(1).size()>0){
					Long endTime = System.currentTimeMillis();
					Request request = new Request(isValid, "login", endTime-startTime, parameters);
//					renderJSON(new Message(new Service(request, Boolean.TRUE)));		
				}else{
					Long endTime = System.currentTimeMillis();
					Request request = new Request(isValid, "login", endTime-startTime, parameters);
//					renderJSON(new Message(new Service(request, Boolean.FALSE)));
				}
			}else{
				userinfo=UserInfo.find("fbEmailAddress",userName).fetch(1);
				if(userinfo.size()>0){
					if(UserInfo.find("select u from UserInfo u where u.password is ?",password).fetch().size()>0){
						Long endTime = System.currentTimeMillis();
						Request request = new Request(isValid, "login", endTime-startTime, parameters);
//						renderJSON(new Message(new Service(request, Boolean.TRUE)));	
					}else{
						Long endTime = System.currentTimeMillis();
						Request request = new Request(isValid, "login", endTime-startTime, parameters);
//						renderJSON(new Message(new Service(request, Boolean.FALSE)));
					}
				}else{
					isValid = false;
					Long endTime = System.currentTimeMillis();
					Request request = new Request(isValid, "login", endTime-startTime, parameters);
					Error err = new Error("400","User not registered");
//					renderJSON(new Message(new Service(request, err)));	
				}
			}			
		}else{
			isValid = false;
			Long endTime = System.currentTimeMillis();
			Request request = new Request(isValid, "login", endTime-startTime, parameters);
			Error err = new Error("400","Invalid Request");
//			renderJSON(new Message(new Service(request, err)));
		}
	}

	public static void checkUserNameAvailable(@Required(message="UserName is required") @MinSize(4) @MaxSize(100) String userName){
		Long startTime = System.currentTimeMillis();
		boolean isValid = false;
		Map<String, String>	parameters = new HashMap<String, String>();
		parameters.put("userName", userName);

		if(!userName.isEmpty() || !userName.trim().isEmpty())
		{
			isValid = true;
			List<UserInfo> userInfo = UserInfo.find("userName", userName).fetch(1);
			Long endTime = System.currentTimeMillis();
			if(userInfo.size()>0){
				Request request = new Request(isValid, "checkUserNameAvailable", endTime-startTime, parameters);
//				renderJSON(new Message(new Service(request, Boolean.FALSE)));
			}else{
				Request request = new Request(isValid, "checkUserNameAvailable", endTime-startTime, parameters);
//				renderJSON(new Message(new Service(request, Boolean.TRUE)));
			}
		}else{
			isValid = false;
			Long endTime = System.currentTimeMillis();
			Request request = new Request(isValid, "checkUserNameAvailable", endTime-startTime, parameters);
			Error err = new Error("400","Invalid Request");
//			renderJSON(new Message(new Service(request, err)));
		}
	}

	public static void addUser(@Required(message="UserName is required") @MinSize(4) @MaxSize(100) String userName, 
										@Required(message="Password is required") @MinSize(5) @Password String password,
										@Required(message="Zipcode is required") @MinSize(5) int zipCode,
										@Required(message="First Name is required") @MinSize(3) @MaxSize(100) String firstName,
										@Required(message="Last Name is required") @MinSize(3) @MaxSize(100) String lastName,
										@Required(message="Gender is required") @Min(0) @Max(1) int gender,
										@Required(message="Facebook Email is required") @Email @MinSize(7) @MaxSize(100) String fbEmailAddress,
										@Required(message="Facebook Id is required") @MinSize(5) long fbUserId) throws ParseException{
				String returnMessage = "";
				Long startTime = System.currentTimeMillis();
				boolean isValid = false;
				boolean isadmin=false;
				
				String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
				SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
				Date date = new Date(System.currentTimeMillis());
				Date createdAt=formatter.parse(formatter.format(date));
				Date updatedAt=formatter.parse(formatter.format(date));
				
				Map<String, String> parameters = new HashMap<String, String>();			
				parameters.put("username", userName);
				parameters.put("password",password);
				parameters.put("zipcode",Integer.toString(zipCode));
				parameters.put("gender",Integer.toString(gender));
				parameters.put("firstname",firstName);
				parameters.put("lastname",lastName);
				parameters.put("createdAt", createdAt.toString());
				parameters.put("updatedAt", updatedAt.toString());
				parameters.put("fbEmailAddress",fbEmailAddress);
				parameters.put("fb_user_id",Long.toString(fbUserId));
				
				if(!userName.trim().isEmpty() && !password.trim().isEmpty() && !firstName.trim().isEmpty() && !lastName.trim().isEmpty()){
					List<UserInfo> userInfo = UserInfo.find("userName", userName).fetch();
					if(userInfo.size()>0){
						isValid = false;
						Long endTime = System.currentTimeMillis();
						Request request = new Request(isValid, "addUser", endTime-startTime, parameters);
						Error err = new Error("406","This username is already registered");
//						renderJSON(new Message(new Service(request, err)));
					}else{
						isValid = true;
						UserInfo newuser=new UserInfo(userName,firstName,lastName,password,1,zipCode,fbEmailAddress,fbUserId,gender,createdAt,updatedAt);
						newuser.save();
						Long endTime = System.currentTimeMillis();
						Request request = new Request(isValid, "addUser", endTime-startTime, parameters);
//						renderJSON(new Message(new Service(request, Boolean.TRUE)));						
					}
				}else{
					isValid = false;
					Long endTime = System.currentTimeMillis();
					Request request = new Request(isValid, "addUser", endTime-startTime, parameters);
					Error err = new Error("400","Invalid Request");
//					renderJSON(new Message(new Service(request, err)));
				}
			}
}