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
  private String oauthToken;
  private String tokenSecret;
  private String serviceProviderName;
  private String consumerKey;
  private String consumerSecret;
  
  public NewAccountMessage(Long userid, String email, String oauthToken,
  												 String tokenSecret, String serviceProviderName, 
  												 String consumerKey,	String consumerSecret)
	{
		this.userId = userid;
		this.email = email;
		this.oauthToken = oauthToken;
		this.tokenSecret = tokenSecret;
		this.serviceProviderName = serviceProviderName;
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
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

	public String getOauthToken()
	{
		return oauthToken;
	}

	public void setOauthToken(String oauthToken)
	{
		this.oauthToken = oauthToken;
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

	public String getConsumerKey()
	{
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey)
	{
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret()
	{
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret)
	{
		this.consumerSecret = consumerSecret;
	}
}