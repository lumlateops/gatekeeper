__author__ = 'prachi'

import web
import gmailHandler
import msnHandler
import aolHandler
import yahooHandler

# All the endpoints exposed by the app
urls = (
'/email/add/gmail/(.*)', 'addGmailHandler',
'/get_access_token/(.*)', 'gmailCallbackHandler',
'/email/add/msn/(.*)', 'addMSNHandler',
'/email/add/aol/(.*)', 'addAOLHandler',
'/email/add/yahoo/(.*)', 'addYahooHandler',
'/(.*)', 'defaultHandler'
)

app = web.application(urls, globals())

gmailHandler1 = None

# This is the default handler that handles all un-registered end points
class defaultHandler:
    def GET (self, name):
        return web.websafe('Sorry, I dont understand your request')


# Gets the user email and initiates the auth flow.
# This method is provides the response for the request to the frontend
class addGmailHandler:
    def GET (self, email):
        gmailHandler1 = gmailHandler.GmailHandler()
        return gmailHandler.GmailHandler.getAuthToken(gmailHandler1, email)

class gmailCallbackHandler:
    def GET (self, oauth_token):
        print 'outh_token:'
        print oauth_token
        return gmailHandler.GmailHandler.callback(gmailHandler1, oauth_token)

# Gets the user email and initiates the auth flow.
# This method is provides the response for the request to the frontend
class addMSNHandler:
    def GET (self, email):
        handler = msnHandler.MSNHandler()
        return msnHandler.MSNHandler.test(handler)

# Gets the user email and initiates the auth flow.
# This method is provides the response for the request to the frontend
class addAOLHandler:
    def GET (self, email):
        handler = aolHandler.AOLHandler()
        return aolHandler.AOLHandler.test(handler)

# Gets the user email and initiates the auth flow.
# This method is provides the response for the request to the frontend
class addYahooHandler:
    def GET (self, email):
        handler = yahooHandler.YahooHandler()
        return yahooHandler.YahooHandler.test(handler)

# The main method
if __name__ == "__main__":
    app.run()