package models;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name = "Service_Provider")
public class ServiceProvider extends Model
{
	public String		name;
	public String		consumerKey;
	public String		consumerSecret;
	public boolean	active;
	public String		website;
	public String		protocol;
	public Date			created_at;
	public Date			updated_at;

	public ServiceProvider(String name, String consumerKey, String consumerSecret, boolean active, String website,
			String protocol, Date createdAt, Date updatedAt)
	{
		this.name = name;
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.active = active;
		this.website = website;
		this.protocol = protocol;
		created_at = createdAt;
		updated_at = updatedAt;
	}
}
