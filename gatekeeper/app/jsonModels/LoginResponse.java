package jsonModels;

public class LoginResponse
{
	private String id;
	private String username;
	private String name;
	private String fbAuthToken;
	private Boolean hasSetupEmailAccounts;
	
	public LoginResponse(String id, String username, String name,
			String fbAuthToken, Boolean hasSetupEmailAccounts)
	{
		super();
		this.id = id;
		this.username = username;
		this.name = name;
		this.fbAuthToken = fbAuthToken;
		this.hasSetupEmailAccounts = hasSetupEmailAccounts;
	}
}
