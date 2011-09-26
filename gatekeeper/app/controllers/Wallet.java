package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jsonModels.Message;
import jsonModels.Request;
import jsonModels.Service;
import models.Deal;
import models.ErrorCodes;
import models.ServiceProvider;
import models.SortFields;
import models.SortOrder;
import play.Logger;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;

public class Wallet extends Controller
{
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
		renderJSON("Loaded");
	}
}