package jsonModels;

import java.util.List;
import java.util.Map;


public class Request
{
	private Boolean isValid;
	private String operation;
	private Long processingTime;
	private Map<String, String>	parameters;
	
	public Request(Boolean isValid, String operation, Long processingTime,
			Map<String, String>	parameters)
	{
		this.isValid = isValid;
		this.operation = operation;
		this.processingTime = processingTime;
		this.parameters = parameters;
	}
}