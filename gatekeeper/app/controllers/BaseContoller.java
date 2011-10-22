package controllers;

import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import bl.utilities.Utility;

public class BaseContoller extends Controller
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