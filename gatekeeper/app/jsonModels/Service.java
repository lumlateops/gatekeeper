package jsonModels;

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
	private ServiceResponse response;
//	private Response response;
//	private Errors errors;
	
	public Service(Request request, ServiceResponse response)
	{
		this.request = request;
		this.response = response;
	}
	
}
