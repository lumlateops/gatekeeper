package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name = "ServiceProvider")
public class ServiceProvider extends Model
{
	public String		name;
	public String		consumerKey;
	public String		consumerSecret;
	public String		logoUrl;
	public boolean	active;
	public String		website;
	public String		protocol;
	public Date			createdAt;
	public Date			updatedAt;

	public ServiceProvider(String name, String consumerKey, String consumerSecret, 
												 String logoUrl, boolean active, String website,
												 String protocol, Date createdAt, Date updatedAt)
	{
		this.name = name;
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.logoUrl = logoUrl;
		this.active = active;
		this.website = website;
		this.protocol = protocol;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}
