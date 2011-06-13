package jsonModels;

import java.util.List;
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
public class Errors
{
	private List<Error> errors;

	public Errors(List<Error> errors)
	{
		super();
		this.errors = errors;
	}
}
