package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import play.db.jpa.Model;

/**
 * 
 * @author prachi
 *
 */
@Entity
public class Subscription extends Model
{
	@OneToOne
	public Department	department;
	public Long				accountId;
	public Boolean		active;
	public Date				createdAt;
	public Date				updatedAt;
	
	public Subscription(Department department, Long accountId, Boolean active, Date createdAt, Date updatedAt)
	{
		this.department = department;
		this.accountId = accountId;
		this.active = active;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}
