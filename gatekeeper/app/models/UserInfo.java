package models;
 
import java.util.*;

import javax.persistence.*;
 
import play.data.validation.Email;
import play.db.jpa.*;

/**
+----------------+--------------+------+-----+---------+----------------+
| Field          | Type         | Null | Key | Default | Extra          |
+----------------+--------------+------+-----+---------+----------------+
| id             | bigint(20)   | NO   | PRI | NULL    | auto_increment |
| createdAt      | datetime     | YES  |     | NULL    |                |
| emailAddress   | varchar(255) | YES  |     | NULL    |                |
| fbEmailAddress | varchar(255) | YES  |     | NULL    |                |
| fbFullName     | varchar(255) | YES  |     | NULL    |                |
| fbLocationId   | bigint(20)   | YES  |     | NULL    |                |
| fbLocationName | varchar(255) | YES  |     | NULL    |                |
| fbUserId       | bigint(20)   | YES  |     | NULL    |                |
| gender         | varchar(255) | YES  |     | NULL    |                |
| isActive       | bit(1)       | YES  |     | NULL    |                |
| isAdmin        | bit(1)       | YES  |     | NULL    |                |
| password       | varchar(255) | YES  |     | NULL    |                |
| updatedAt      | datetime     | YES  |     | NULL    |                |
| username       | varchar(255) | YES  |     | NULL    |                |
+----------------+--------------+------+-----+---------+----------------+
 * @author prachi
 *
 */
@Entity
@Table(name = "UserInfo")
public class UserInfo extends Model 
{
	public String		username;
	public String		password;
	public Boolean	isActive;
	public Boolean	isAdmin;
	@Email
	public String		fbEmailAddress;
	public Long			fbUserId;
	public String		fbFullName;
	public String		fbLocationName;
	public Long			fbLocationId;
	public String		gender;
	public Date			createdAt;
	public Date			updatedAt;
	@Email
	public String		emailAddress;
	
	public UserInfo(String username, String password, Boolean isActive, Boolean isAdmin, 
									String fbEmailAddress, Long fbUserId, String fbFullName,
									String fbLocationName, Long fbLocationId, String gender, 
									Date createdAt, Date updatedAt, String emailAddress)
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