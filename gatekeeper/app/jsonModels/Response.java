package jsonModels;

import java.util.List;
import java.util.Map;

public class Response implements ServiceResponse
{
	private Map<String, List<?>> success;

	public Response(Map<String, List<?>> response)
	{
		this.success = response;
	}
	
	@Override
	public ServiceResponse getServiceResponse()
	{
		return this;
	}
}
