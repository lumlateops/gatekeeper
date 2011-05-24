package main.java.com.deallr.gatekeeper.broker;

import main.java.com.deallr.gatekeeper.broker.impl.EmailServerResource;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

/**
 * The REST layer that receives all the incoming requests and hands them out to
 * the relevant handlers. This is essentially the REST interface for gatekeeper
 * 
 * @author prachi
 * 
 */
public class RequestBroker extends Application {

	private static final Protocol PROTOCOL = Protocol.HTTP;
	private static final int PORT = 8182;

	public static void main(String[] args) throws Exception {
		// Create the HTTP server and listen on port 8182
		new Server(PROTOCOL, PORT, RequestBroker.class).start();
	}
	
	@Get
	public String addGmailAccount() {
		return "Gmail account added";
	}
	
	@Get
	public String toString() {
		return "hello, world";
	}
	
	@Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());

        router.attachDefault(new Directory(getContext(), "war:///"));
        router.attach("/email/123", EmailServerResource.class);

        return router;
    }
}
