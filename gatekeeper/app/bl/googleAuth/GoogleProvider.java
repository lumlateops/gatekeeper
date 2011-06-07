package bl.googleAuth;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.Service;
import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthRsaSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthSigner;
import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.BaseFeed;
import com.google.gdata.data.Feed;

import java.net.URL;

class GoogleProvider
{
	private static final String	SCOPE						= "https://mail.google.com/mail/feed/atom/";
	private static final String	CONSUMER_KEY		= "deallr.com";
	private static final String	CONSUMER_SECRET	= "f_yk4d2GkQljJ38JQrcRJBPr";
	private static final String	FEED_URL				= "https://mail.google.com/mail/feed/atom/";
	private static final String	ACCESS_TOKEN		= "1/p5OBp4-ycxTuNtHue5agDUtYsHPulNEHB0MfY_t4Ozo";
	private static final String	TOKEN_SECRET		= "YZol8hF8NHLHMtUVb_qpBswM";

	public GoogleProvider()
	{
		super();
	}
	
	public static void authorizeAccount(String email)
	{
		
	}
	
//	public static void main(String[] args) throws Exception
//	{
//
//		// //////////////////////////////////////////////////////////////////////////
//		// STEP 1: Gather the user's information
//		// //////////////////////////////////////////////////////////////////////////
//
//		// This step collects information from the user, such as the consumer key
//		// and which service to query. This is just a general setup routine, and
//		// the method by which you collect user information may be different in your
//		// implementation.
//		// UserInputHelper inputController = new OAuthUserInputHelper();
//		// UserInputVariables variables = inputController.getVariables();
//
//		// //////////////////////////////////////////////////////////////////////////
//		// STEP 2: Set up the OAuth objects
//		// //////////////////////////////////////////////////////////////////////////
//
//		// You first need to initialize a few OAuth-related objects.
//		// GoogleOAuthParameters holds all the parameters related to OAuth.
//		// OAuthSigner is responsible for signing the OAuth base string.
//		GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
//
//		// Set your OAuth Consumer Key (which you can register at
//		// https://www.google.com/accounts/ManageDomains).
//		oauthParameters.setOAuthConsumerKey(CONSUMER_KEY);
//
//		// Initialize the OAuth Signer. If you are using RSA-SHA1, you must provide
//		// your private key as a Base-64 string conforming to the PKCS #8 standard.
//		// Visit http://code.google.com/apis/gdata/authsub.html#Registered to learn
//		// more about creating a key/certificate pair. If you are using HMAC-SHA1,
//		// you must set your OAuth Consumer Secret, which can be obtained at
//		// https://www.google.com/accounts/ManageDomains.
//		oauthParameters.setOAuthConsumerSecret(CONSUMER_SECRET);
//		OAuthSigner signer = new OAuthHmacSha1Signer();
//
//		// Finally create a new GoogleOAuthHelperObject. This is the object you
//		// will use for all OAuth-related interaction.
//		GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(signer);
//
//		// //////////////////////////////////////////////////////////////////////////
//		// STEP 3: Get the Authorization URL
//		// //////////////////////////////////////////////////////////////////////////
//
//		// Set the scope for this particular service.
//		oauthParameters.setScope(SCOPE);
//
//		// This method also makes a request to get the unauthorized request token,
//		// and adds it to the oauthParameters object, along with the token secret
//		// (if it is present).
//		// oauthHelper.getUnauthorizedRequestToken(oauthParameters);
//
//		// Get the authorization url. The user of your application must visit
//		// this url in order to authorize with Google. If you are building a
//		// browser-based application, you can redirect the user to the authorization
//		// url.
//		// String requestUrl =
//		// oauthHelper.createUserAuthorizationUrl(oauthParameters);
//		// System.out.println(requestUrl);
//		// System.out.println("Please visit the URL above to authorize your OAuth "
//		// + "request token.  Once that is complete, press any key to "
//		// + "continue...");
//		// System.in.read();
//
//		// //////////////////////////////////////////////////////////////////////////
//		// STEP 4: Get the Access Token
//		// //////////////////////////////////////////////////////////////////////////
//
//		// Once the user authorizes with Google, the request token can be exchanged
//		// for a long-lived access token. If you are building a browser-based
//		// application, you should parse the incoming request token from the url and
//		// set it in GoogleOAuthParameters before calling getAccessToken().
//		// String token = oauthHelper.getAccessToken(oauthParameters);
//		// System.out.println("OAuth Access Token: " + token);
//		// System.out.println();
//		// String accessTokenSecret = oauthParameters.getOAuthTokenSecret();
//		// System.out.println("OAuth Access Token's Secret: " + accessTokenSecret);
//		// System.out.println();
//
//		// //////////////////////////////////////////////////////////////////////////
//		// STEP 5: Make an OAuth authorized request to Google
//		// //////////////////////////////////////////////////////////////////////////
//		oauthParameters.setOAuthToken(ACCESS_TOKEN);
//		oauthParameters.setOAuthTokenSecret(TOKEN_SECRET);
//
//		// Initialize the variables needed to make the request
//		URL feedUrl = new URL(FEED_URL);
//		System.out.println("Sending request to " + feedUrl.toString());
//		System.out.println();
//		GoogleService googleService = new GoogleService("mail", "deallr.com");
//
//		// Set the OAuth credentials which were obtained from the step above.
//		googleService.setOAuthCredentials(oauthParameters, signer);
//
//		// Make the request to Google
//		BaseFeed resultFeed = googleService.getFeed(feedUrl, Feed.class);
//		System.out.println("Response Data:");
//		System.out.println("=====================================================");
//		System.out.println("| TITLE: " + resultFeed.getTitle().getPlainText());
//		if (resultFeed.getEntries().size() == 0)
//		{
//			System.out.println("|\tNo entries found.");
//		}
//		else
//		{
//			for (int i = 0; i < resultFeed.getEntries().size(); i++)
//			{
//				BaseEntry entry = (BaseEntry) resultFeed.getEntries().get(i);
//				System.out.println("|\t" + (i + 1) + ": "
//						+ entry.getTitle().getPlainText());
//			}
//		}
//		System.out.println("=====================================================");
//		System.out.println();
//
//		// //////////////////////////////////////////////////////////////////////////
//		// STEP 6: Revoke the OAuth token
//		// //////////////////////////////////////////////////////////////////////////
//
//		// System.out.println("Revoking OAuth Token...");
//		// oauthHelper.revokeToken(oauthParameters);
//		// System.out.println("OAuth Token revoked...");
//	}
}
