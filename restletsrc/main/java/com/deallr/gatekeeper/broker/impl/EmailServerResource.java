package main.java.com.deallr.gatekeeper.broker.impl;

import org.restlet.resource.ServerResource;

import main.java.com.deallr.gatekeeper.broker.api.EmailResource;
import main.java.com.deallr.gatekeeper.model.Email;

public class EmailServerResource extends ServerResource 
								 implements EmailResource {

	private static volatile Email email = new Email();

	public void remove() {
		email = null;
	}

	public Email retrieve() {
		return email;
	}

	public void store(Email email) {
		EmailServerResource.email = email;
	}
}
