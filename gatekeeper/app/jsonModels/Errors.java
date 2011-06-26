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
public class Errors implements ServiceResponse
{
	private List<Error> error;

	public Errors(List<Error> error)
	{
		this.error = error;
	}

	@Override
	public ServiceResponse getServiceResponse()
	{
		return this;
	}
}
