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
	public String userName;
	public String password;
	public boolean active;
	public int zipcode;
	public String emailAddress;
	public Date	created_at;
	public Date	updated_at;
	
	
	
}
