__author__ = 'prachi'

import web
import utils
import JtvClient
import gdata.gauth
import gdata.docs.client
import gdata.docs.service


# Gets the user email and initiates the auth flow.
# This method is provides the response for the request to the frontend
#class addGmailHandler:
#    def GET (self, email):
#        return email

# Handles the complete flow for Gmail
class GmailHandler:

    #XOAuth URL
    CONSUMER_KEY = 'deallr.com'
    CONSUMER_SECRET = 'f_yk4d2GkQljJ38JQrcRJBPr'
    SCOPES = ['https://mail.google.com']  # example of a multi-scoped token
    xoauthUrl = 'https://mail.google.com/mail/b/'
    xoauthProtocol = '/imap'
    request_token = None


    # Stores the auth token in the database
    def getAuthToken(self, email):
        # Validate the incoming email address
#        isValid = utils.Utils.validateEmail(email)
        isValid = True

        if isValid:

#            client = gdata.docs.service.DocsService(source='yourCompany-YourAppName-v1')
#            client.SetOAuthInputParameters(gdata.auth.OAuthSignatureMethod.HMAC_SHA1,
#                                           GmailHandler.CONSUMER_KEY,
#                                           consumer_secret=GmailHandler.CONSUMER_SECRET)
#
#            GmailHandler.request_token = client.FetchOAuthRequestToken()
#            print GmailHandler.request_token
#            client.SetOAuthToken(GmailHandler.request_token)
#
#            # req_token is from previous call to client.FetchOAuthRequestToken()
#            oauth_callback_url = 'http://deallr.com/get_access_token'
#            print oauth_callback_url
#            raise web.seeother(client.GenerateOAuthAuthorizationURL(callback_url=oauth_callback_url))


            client = gdata.docs.client.DocsClient(source='deallr-deallr-v1')
            oauth_callback_url = 'http://deallr.com/get_access_token'
            GmailHandler.request_token = client.GetOAuthToken(GmailHandler.SCOPES,
                                                 oauth_callback_url,
                                                 GmailHandler.CONSUMER_KEY,
                                                 consumer_secret=GmailHandler.CONSUMER_SECRET)
            print 'Request Token: '
            print GmailHandler.request_token

            domain = GmailHandler.CONSUMER_KEY  # If on a Google Apps domain, use your domain (e.g. 'example.com').
            redirecturl = GmailHandler.request_token.generate_authorization_url(google_apps_domain=domain)
            print 'Redirect url'
            print redirecturl
            raise web.seeother(redirecturl)
        else:
            return "Email address invalid"


    def callback(self, oauth_token):
#        oauth_token = gdata.auth.OAuthTokenFromUrl(self.request.uri)
        if oauth_token:
          oauth_token.secret = GmailHandler.request_token# TODO: recall saved request_token and set the token secret here.
          oauth_token.oauth_input_params = gdata.auth.OAuthInputParams(gdata.auth.OAuthSignatureMethod.HMAC_SHA1,
                                                                       GmailHandler.CONSUMER_KEY,
                                                                       consumer_secret=GmailHandler.CONSUMER_SECRET)
          client.SetOAuthToken(oauth_token)
        else:
          print 'No oauth_token found in the URL'


    # Just a stupid test
    def test(self):
        return 'hello gmail'

