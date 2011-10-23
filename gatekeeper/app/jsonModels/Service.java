package jsonModels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents the response object that is sent back to the client.
 * It has the following structure:
 * 
 * {
 *    "Service": {
 *        "Version": "1.1",
 *        "Request": {
 *                "IsValid": "true",
 *                "Operation": "isEmailUnique",
 *                "Parameters": {
 *                    "email": "user@deallr.com",
 *                },
 *                "ProcessingTime": "0.017" 
 *          },
 *          "Response": {
 *             "unique": true
 *       }
 * }
 *
 * @author prachi
 *
 */
public class Service
{
	private String version = "1.1";
	private Request request;
	private Map<String, List<?>> response;
	private List<Error> errors;

	public Service()
	{
	}
	
	public Service(Request request, Map<String, List<?>> response)
	{
		this.request = request;
		this.response = response;
	}
	
	public void addError(final String code, final String message)
	{
		if(this.errors == null)
		{
			this.errors = new ArrayList<Error>();
		}
		this.errors.add(new Error(code, message));
	}
	
	public void setRequest(Request request)
	{
		this.request = request;
	}
	
	public void setResponse(Map<String, List<?>> response)
	{
		this.response = response;
	}
	
	public boolean hasErrors()
	{
		return this.errors == null || !this.errors.isEmpty();
	}
}
