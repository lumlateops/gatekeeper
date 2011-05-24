package main.java.com.deallr.gatekeeper.model;

import org.apache.commons.validator.EmailValidator;

/**
 *The model for email information
 * 
 * @author prachi
 * 
 */
public class Email {

	private String emailAddress;
	private EmailProvider provider;

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public EmailProvider getProvider() {
		return provider;
	}

	public void setProvider(EmailProvider provider) {
		this.provider = provider;
	}
	
	public boolean isValidEmail(){
		return EmailValidator.getInstance().isValid(emailAddress);
	}
}
