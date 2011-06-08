package bl.googleAuth;

import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;

/**
 * Bean for storing the auth information during the authorization process
 * @author prachi
 *
 */
public class AuthPayLoad
{
	String redirectUrl;
	GoogleOAuthHelper oauthHelper;
	GoogleOAuthParameters oauthParameters;
	
	public AuthPayLoad(String redirectUrl, GoogleOAuthHelper oauthHelper,
			GoogleOAuthParameters oauthParameters)
	{
		super();
		this.redirectUrl = redirectUrl;
		this.oauthHelper = oauthHelper;
		this.oauthParameters = oauthParameters;
	}

	public String getRedirectUrl()
	{
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl)
	{
		this.redirectUrl = redirectUrl;
	}

	public GoogleOAuthHelper getOauthHelper()
	{
		return oauthHelper;
	}

	public void setOauthHelper(GoogleOAuthHelper oauthHelper)
	{
		this.oauthHelper = oauthHelper;
	}

	public GoogleOAuthParameters getOauthParameters()
	{
		return oauthParameters;
	}

	public void setOauthParameters(GoogleOAuthParameters oauthParameters)
	{
		this.oauthParameters = oauthParameters;
	}
}
