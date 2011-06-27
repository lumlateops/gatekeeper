package jsonModels;

public class LoginResponse
{
	private String id;
	private String username;
	private Boolean hasSetupEmailAccounts;
	
	public LoginResponse(String id, String username, Boolean hasSetupEmailAccounts)
	{
		this.id = id;
		this.username = username;
		this.hasSetupEmailAccounts = hasSetupEmailAccounts;
	} 
}
