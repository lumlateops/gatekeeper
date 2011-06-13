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
	private Object response;
	
	public Service(Request request, Object response)
	{
		this.request = request;
		this.response = response;
	}
}
