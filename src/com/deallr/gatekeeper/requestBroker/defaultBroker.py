__author__ = 'prachi'


import web

# All the endpoints exposed by the app
urls = (
'/email/add/gmail/(.*)', 'addGmailHandler',
'/(.*)', 'defaultHandler'
)

app = web.application(urls, globals())

# This is the default handler that handles all un-registered end points
class defaultHandler:
    def GET (self, name):
        return web.websafe('Sorry, I dont understand your request')


# Gets the user email and initiates the auth flow.
# This method is provides the response for the request to the frontend
class addGmailHandler:
    def GET (self, email):
        return email
    

# The main method
if __name__ == "__main__":
    app.run()