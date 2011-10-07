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
	public UserInfo userInfo;
	public Date lastLoginTime;
	
	public LoginHistory(UserInfo userInfo, Date lastLoginTime)
	{
		this.userInfo = userInfo;
		this.lastLoginTime = lastLoginTime;
	}
}
