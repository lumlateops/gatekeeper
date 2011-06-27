package models;
 
import java.util.*;

import javax.persistence.*;
 
import play.data.validation.Email;
import play.db.jpa.*;
 
@Entity
@Table(name = "UserInfo")
public class UserInfo extends Model 
{
	public String username;
  public String password;
  public Boolean isActive;
  public Boolean isAdmin;
  @Email
  public String fbEmailAddress;
  public Long fbUserId;
  public String fbFullName;
  public String fbLocationName;
  public Long fbLocationId;
  public String gender;
	public Date	createdAt;
	public Date	updatedAt;
	@Email
	public String emailAddress;
	
	public UserInfo(String username, String password, Boolean isActive,
			Boolean isAdmin, String fbEmailAddress, Long fbUserId, String fbFullName,
			String fbLocationName, Long fbLocationId, String gender, Date createdAt,
			Date updatedAt, String emailAddress)
	{
		this.username = username;
		this.password = password;
		this.isActive = isActive;
		this.isAdmin = isAdmin;
		this.fbEmailAddress = fbEmailAddress;
		this.fbUserId = fbUserId;
		this.fbFullName = fbFullName;
		this.fbLocationName = fbLocationName;
		this.fbLocationId = fbLocationId;
		this.gender = gender;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.emailAddress = emailAddress;
	}

	public UserInfo()
	{
	}
}