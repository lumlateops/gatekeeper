package models;

import java.io.Serializable;

/**
 * POJO for RMQ message
 * @author prachi
 *
 */
public class NewAccountMessage implements Serializable
{
  private Long userId;
  private String email;
  private String password;
  private String accessToken;
  private String tokenSecret;
  private String serviceProviderName;
  
  public NewAccountMessage(Long userId, String email, String password,
  		String accessToken, String tokenSecret, String serviceProviderName)
  {
  	this.userId = userId;
  	this.email = email;
  	this.password = password;
  	this.accessToken = accessToken;
  	this.tokenSecret = tokenSecret;
  	this.serviceProviderName = serviceProviderName;
  }

	public Long getUserId()
	{
		return userId;
	}

	public void setUserId(Long userId)
	{
		this.userId = userId;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getAccessToken()
	{
		return accessToken;
	}

	public void setAccessToken(String accessToken)
	{
		this.accessToken = accessToken;
	}

	public String getTokenSecret()
	{
		return tokenSecret;
	}

	public void setTokenSecret(String tokenSecret)
	{
		this.tokenSecret = tokenSecret;
	}

	public String getServiceProviderName()
	{
		return serviceProviderName;
	}

	public void setServiceProviderName(String serviceProviderName)
	{
		this.serviceProviderName = serviceProviderName;
	}

	@Override
	public String toString()
	{
//		return new Gson().toJson(this);
		
		return "NewAccountMessage [email=" + email + ", password=" + password 
				+ ", accessToken=" + accessToken + ", tokenSecret=" + tokenSecret
				+ ", serviceProviderName=" + serviceProviderName + ", userId=" + userId
				+ "]";
	}
}