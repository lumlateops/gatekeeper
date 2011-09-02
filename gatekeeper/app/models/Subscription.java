package models;

import java.sql.Date;

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
	public UserInfo		userInfo;
	@OneToOne
	public Retailers		retailer;
	@OneToOne
	public Department	department;
	public Boolean		active;
	public Date				createdAt;
	public Date				updatedAt;
	
	public Subscription(UserInfo userInfo, Retailers retailer,
			Department department, Boolean active, Date createdAt, Date updatedAt)
	{
		this.userInfo = userInfo;
		this.retailer = retailer;
		this.department = department;
		this.active = active;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/**
	 * CREATE TABLE `Subscriptions` (
		`id` bigint(20) NOT NULL AUTO_INCREMENT,
		`userAccountId` int(11) NOT NULL, 
		`retailerId` int(11) NOT NULL,
		`departmentId` int(11) NOT NULL,
		`active` bit(1) NOT NULL,
		`createdAT` datetime DEFAULT NULL,
		`updatedAT` datetime DEFAULT NULL,
		PRIMARY KEY (`id`),
		UNIQUE KEY `index_subscription` (`userAccountId`,`retailerId`,`departmentId`)
		) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
	 */
}
