package jsonModels;

import java.util.Date;

public class EmailAccountResponse
{
	private String email;
	private String providerName;
	
	public EmailAccountResponse(String email, String providerName)
	{
		this.email = email;
		this.providerName = providerName;
	}
}
