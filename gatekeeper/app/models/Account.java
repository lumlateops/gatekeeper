package models;


import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import play.Logger;
import play.data.validation.Email;
import play.db.jpa.Model;


/**
 * 
 * @author prachi
 *
 */
@Entity
public class Account extends Model
{
	@ManyToOne
	public UserInfo	userInfo;
	@Email
	public String		email;
	public String		password;
	public String		dllrAccessToken;
	public String		dllrTokenSecret;
	public String		fbToken;
	public Boolean	registeredEmail;
	public Boolean	active;
	public String		lastError;
	public Date			lastConfirmedAt;
	public Date			lastErrorAt;
	public Date			createdAt;
	public Date			updatedAt;

	@OneToOne
	public ServiceProvider	provider;

	public Account(UserInfo userInfo, String email, String password,
			String dllrAccessToken, String dllrTokenSecret, String fbToken,
			Boolean registeredEmail, Boolean active, String lastError,
			Date lastConfirmedAt, Date lastErrorAt, Date createdAt, Date updatedAt,
			ServiceProvider provider)
	{
		this.userInfo = userInfo;
		this.email = email;
		this.password = password;
		this.dllrAccessToken = dllrAccessToken;
		this.dllrTokenSecret = dllrTokenSecret;
		this.fbToken = fbToken;
		this.registeredEmail = registeredEmail;
		this.active = active;
		this.lastError = lastError;
		this.lastConfirmedAt = lastConfirmedAt;
		this.lastErrorAt = lastErrorAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.provider = provider;
	}
}