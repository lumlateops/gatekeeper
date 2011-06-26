package jsonModels;

import java.util.HashMap;
import java.util.Map;

/**
 * "Errors": {
 *             "Error": {
 *                        "code": "invalid_request",
 *                         "message": "Invalid email address" 
 *                      }
 *           }
 * 
 * @author prachi
 *
 */
public class Error
{
	private static final String	CODE_KEY	= "code";
	private static final String	MESSAGE_KEY	= "message";
	private Map<String, String> error = new HashMap<String, String>();

	public Error(String code, String message)
	{
		this.error.put(CODE_KEY, code);
		this.error.put(MESSAGE_KEY, message);
	}
}
