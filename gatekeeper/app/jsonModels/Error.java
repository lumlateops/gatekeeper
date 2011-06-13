package jsonModels;

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
	private String code;
	private String message;

	public Error(String code, String message)
	{
		this.code = code;
		this.message = message;
	}
}
