__author__ = 'prachi'

import utils

# Gets the user email and initiates the auth flow.
# This method is provides the response for the request to the frontend
#class addGmailHandler:
#    def GET (self, email):
#        return email

# Handles the complete flow for Gmail
class GmailHandler:

    #XOAuth URL
    xoauthUrl = "https://mail.google.com/mail/b/"
    xoauthProtocol = "/imap"

    # Stores the auth token in the database
    def getAuthToken(self, email):
        # Validate the incoming email address
        isValid = utils.Utils.validateEmail(email)

        if isValid:
            fullUrl = GmailHandler.xoauthUrl + email + GmailHandler.xoauthProtocol
            return fullUrl
        else:
            return "Email address invalid"

    # Just a stupid test
    def test(self):
        return 'hello gmail'

