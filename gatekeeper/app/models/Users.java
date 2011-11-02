package models;
 
import javax.persistence.Entity;
import javax.persistence.Table;

import play.data.validation.Email;
import play.data.validation.Password;
import play.db.jpa.Model;

/**
+----------+-------------+------+-----+---------+-------+
| Field    | Type        | Null | Key | Default | Extra |
+----------+-------------+------+-----+---------+-------+
| email    | varchar(80) | NO   | PRI | NULL    |       |
| password | varchar(20) | NO   |     | NULL    |       |
+----------+-------------+------+-----+---------+-------+
 * @author prachi
 *
 */
@Entity
@Table(name = "users")
public class Users extends Model 
{
	@Email
	public String		email;
	@Password
	public String		password;
	
	public Users(String email, String password)
	{
		this.email = email;
		this.password = password;
	}
}