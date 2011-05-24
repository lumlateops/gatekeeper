package main.java.com.deallr.gatekeeper.broker.api;

import main.java.com.deallr.gatekeeper.model.Email;

import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

/**
 * Represents an email address within the system and provides the CRUD interface
 * for the same.
 * 
 * @author prachi
 * 
 */
public interface EmailResource {
	@Get
	public Email retrieve();

	@Put
	public void store(Email email);

	@Delete
	public void remove();
}
