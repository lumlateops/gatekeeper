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
	public Long				dealId;
	public Long				userId;
	public Boolean		isExpired;
	public Date				alertTime;
	public Date				createdAt;
	
	public Wallet(Long dealId, Long userId, Boolean isExpired, Date alertTime,
			Date createdAt)
	{
		super();
		this.dealId = dealId;
		this.userId = userId;
		this.isExpired = isExpired;
		this.alertTime = alertTime;
		this.createdAt = createdAt;
	}
}
