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
//  private String oauthToken;
//  private String tokenSecret;
  private String serviceProviderName;
//  private String consumerKey;
//  private String consumerSecret;
  
	public NewAccountMessage(Long userId, String email, String password,
			String serviceProviderName)
	{
		this.userId = userId;
		this.email = email;
		this.password = password;
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

	public String getServiceProviderName()
	{
		return serviceProviderName;
	}

	public void setServiceProviderName(String serviceProviderName)
	{
		this.serviceProviderName = serviceProviderName;
	}
}