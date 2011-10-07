package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import play.db.jpa.Model;

/**
 * Stores the user login history
 * @author prachi
 *
 */
@Entity
public class LoginHistory extends Model
{
	@OneToOne
	public UserInfo userinfo;
	public Date lastLoginTime;
	
	public LoginHistory(UserInfo userinfo, Date lastLoginTime)
	{
		this.userinfo = userinfo;
		this.lastLoginTime = lastLoginTime;
	}
}
