package models;


import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import play.data.validation.Email;
import play.db.jpa.Model;

@Entity
public class Account extends Model
{
	public String		userId;
	@Email
	public String		email;
	public String		dllr_auth_token;
	public String		dllr_token_secret;
	public boolean	active;
	public String		last_error;
	public Date			last_confirmed_at;
	public Date			last_error_at;
	public Date			created_at;
	public Date			updated_at;

	@OneToOne
	ServiceProvider	provider;

	public Account(String userId, String userEmail, ServiceProvider provider,
			String dllrAuthToken, String dllrTokenSecret, boolean active, String lastError,
			Date lastConfirmedAt, Date lastErrorAt, Date createdAt, Date updatedAt)
	{
		this.userId = userId;
		this.email = userEmail;
		this.provider = provider;
		this.dllr_auth_token = dllrAuthToken;
		this.dllr_token_secret = dllrTokenSecret;
		this.active = active;
		this.last_error = lastError;
		this.last_confirmed_at = lastConfirmedAt;
		this.last_error_at = lastErrorAt;
		this.created_at = createdAt;
		this.updated_at = updatedAt;
	}
}