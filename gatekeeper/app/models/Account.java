package models;


import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import play.Logger;
import play.data.validation.Email;
import play.db.jpa.Model;

@Entity
public class Account extends Model
{
	public String		userId;
	@Email
	public String		email;
	public String		dllrAuthToken;
	public String		dllrTokenSecret;
	public boolean	active;
	public String		lastError;
	public Date			lastConfirmedAt;
	public Date			lastErrorAt;
	public Date			createdAt;
	public Date			updatedAt;

	@OneToOne
	ServiceProvider	provider;

	public Account(String userId, String userEmail, ServiceProvider provider,
			String dllrAuthToken, String dllrTokenSecret, boolean active, String lastError,
			Date lastConfirmedAt, Date lastErrorAt, Date createdAt, Date updatedAt)
	{
		Logger.info("Account constructor called");
		
		this.userId = userId;
		this.email = userEmail;
		this.provider = provider;
		this.dllrAuthToken = dllrAuthToken;
		this.dllrTokenSecret = dllrTokenSecret;
		this.active = active;
		this.lastError = lastError;
		this.lastConfirmedAt = lastConfirmedAt;
		this.lastErrorAt = lastErrorAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}