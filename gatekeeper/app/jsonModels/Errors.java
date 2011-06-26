package jsonModels;

import java.util.ArrayList;
import java.util.HashMap;
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
public class Errors implements ServiceResponse
{
	private List<Error> errors = new ArrayList<Error>();

	public void addError(final String code, final String message)
	{
		this.errors.add(new Error(code, message));
	}

	@Override
	public ServiceResponse getServiceResponse()
	{
		return this;
	}
}
