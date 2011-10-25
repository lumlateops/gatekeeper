package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jsonModels.Message;
import jsonModels.Request;
import jsonModels.Service;
import models.BetaToken;
import models.enums.ErrorCodes;
import play.data.validation.Required;
import play.data.validation.Validation;


public class BetaAccessController extends BaseContoller
{
	/**
	 * Verifies if incoming token exists, is active and hasn't been used already
	 */
	public static void verifyTokenValidity(@Required(message = "Token is required") String token)
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
			serviceResponse = verifyToken(token);
			if(!serviceResponse.hasErrors())
			{
				//Token is valid
				response.put("tokenValid", 
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
		parameters.put("token", token);
		Request request = new Request(isValidRequest, "verifyTokenValidity", endTime - startTime, parameters);
		
		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));
	}
	
	/**
	 * Checks token validity
	 * @param token
	 * @return
	 */
	public static Service verifyToken(String token)
	{
		Service serviceResponse = new Service();
		
		// Look up by token
		BetaToken matchingToken = BetaToken.find("token", token).first();
		if(matchingToken == null)
		{
			//No matching token
			serviceResponse.addError(ErrorCodes.INVALID_BETA_TOKEN.toString(), "Invalid sign up code. Please make sure there are no typing errors.");
		}
		else
		{
			if(!matchingToken.isActive)
			{
				//In active token
				serviceResponse.addError(ErrorCodes.INACTIVE_BETA_TOKEN.toString(), "Invalid sign up code. Please make sure there are no typing errrs.");
			}
			else if(matchingToken.isUsed)
			{
				//Already used token
				serviceResponse.addError(ErrorCodes.USED_BETA_TOKEN.toString(), "Code already used for signing up.");
			}
		}
		return serviceResponse;
	}
}
