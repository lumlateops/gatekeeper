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
	public Long		userId;
	@Email
	public String		email;
	public String		dllrAccessToken;
	public String		dllrTokenSecret;
	public Boolean	active;
	public String		lastError;
	public Date			lastConfirmedAt;
	public Date			lastErrorAt;
	public Date			createdAt;
	public Date			updatedAt;

	@OneToOne
	ServiceProvider	provider;

	public Account(Long userId, String userEmail, ServiceProvider provider,
			String dllrAccessToken, String dllrTokenSecret, Boolean active, String lastError,
			Date lastConfirmedAt, Date lastErrorAt, Date createdAt, Date updatedAt)
	{
		Logger.info("Account constructor called");
		
		this.userId = userId;
		this.email = userEmail;
		this.provider = provider;
		this.dllrAccessToken = dllrAccessToken;
		this.dllrTokenSecret = dllrTokenSecret;
		this.active = active;
		this.lastError = lastError;
		this.lastConfirmedAt = lastConfirmedAt;
		this.lastErrorAt = lastErrorAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}