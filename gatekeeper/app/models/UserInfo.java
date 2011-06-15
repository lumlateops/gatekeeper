package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name = "UserInfo")
public class UserInfo extends Model
{
	public String firstName;
	public String lastName;
	public String userId;
	public String password;
	public boolean active;
	public int zipcode;
	public String email;
	public String gender;
	public Date	created_at;
	public Date	updated_at;
	
	public UserInfo(String firstName, String lastName, String userId,
			String password, boolean active, int zipcode, String email,
			Date created_at, Date updated_at) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.userId = userId;
		this.password = password;
		this.active = active;
		this.zipcode = zipcode;
		this.email = email;
		this.created_at = created_at;
		this.updated_at = updated_at;
	}
	
	
	
	
}
