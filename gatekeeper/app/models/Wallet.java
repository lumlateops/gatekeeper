package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import play.db.jpa.Model;

/**
 * Represents the wallet table
 * @author prachi
 *
 */
@Entity
public class Wallet extends Model
{
	@OneToOne
	public Deal			deal;
	public UserInfo	userInfo;
	public Boolean	isExpired;
	public Date			alertTime;
	public Date			createdAt;
	
	public Wallet(Deal deal, UserInfo userInfo, Boolean isExpired, Date alertTime, Date createdAt)
	{
		this.deal = deal;
		this.userInfo = userInfo;
		this.isExpired = isExpired;
		this.alertTime = alertTime;
		this.createdAt = createdAt;
	}
}
