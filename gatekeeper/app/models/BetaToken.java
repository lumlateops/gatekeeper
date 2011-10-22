package models;
 
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import play.data.validation.Email;
import play.db.jpa.Model;

/**
 * @author prachi
 *
 */
@Entity
public class BetaToken extends Model 
{
	public String		token;
	//Pre-generated tokens that haven't been sent out yet
	public Boolean	isActive;
	//Token is marked used as soon as someone registers using it
	public Boolean	isUsed;
	//User who uses this token to register
	@OneToOne
	public UserInfo	userId;
	//The email address this token was sent to for invitation.
	@Email
	public String		emailSentTo;
	
	public BetaToken(String token, Boolean isActive, Boolean isUsed,
			UserInfo userId, String emailSentTo)
	{
		this.token = token;
		this.isActive = isActive;
		this.isUsed = isUsed;
		this.userId = userId;
		this.emailSentTo = emailSentTo;
	}
}