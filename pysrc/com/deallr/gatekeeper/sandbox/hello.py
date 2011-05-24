'''
Created on May 14, 2011

@author: prachi
'''
import web

urls = (
  '/hello/(.*)', 'hello',
  '/bye/(.*)', 'bye')

app = web.application(urls, globals())

class hello:
    def GET(self, name):
        return 'Hello, ' + web.websafe(name) + '!'
    
class bye:
    def GET(self, name):
        return 'Bye, ' + web.websafe(name) + '!'

if __name__ == "__main__":
    app.run()