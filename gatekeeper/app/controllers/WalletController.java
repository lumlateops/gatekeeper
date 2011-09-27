package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bl.Utility;

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

public class WalletController extends Controller
{
	@Before
	public static void logRequest()
	{
		Utility.logRequest();
	}
	
	@After
	public static void logResponse()
	{
		Utility.logResponse();
	}
}