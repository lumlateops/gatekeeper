package models;
 
import java.util.*;

import javax.persistence.*;
 
import play.data.validation.Email;
import play.db.jpa.*;
 
@Entity
@Table(name = "UserInfo")
public class UserInfo extends Model {
	
	public String userName;
  public String firstName;
  public String lastName;
  public String password;
  public int isActive;
  public int zipCode;
  @Email
  public String fbEmailAddress;
  public long fbUserId;
  public int gender; 
	public Date	createdAt;
	public Date	updatedAt;
	@Email
	public String emailAddress;
	
	public UserInfo(String userName, String firstName, String lastName,
									String password, int isActive,int zipCode, 
									String fbEmailAddress, long fbUserId, int gender,
									Date createdAt, Date updatedAt) {
		this.userName = userName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.password = password;
		this.isActive = isActive;
		this.zipCode = zipCode;
		this.fbEmailAddress = fbEmailAddress;
		this.fbUserId = fbUserId;
		this.gender = gender;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.emailAddress = userName+"@deallr.com";
	}
	
}