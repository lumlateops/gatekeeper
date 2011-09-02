package models;


import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import play.Logger;
import play.data.validation.Email;
import play.db.jpa.Model;

/**
 * 
 */
@Entity
public class Deal extends Model
{
	@OneToOne
	public Subscription		subscription;
	public int						locationId;
	@ManyToMany
	public List<Product>	products;
	public String					email;
	public Date						createdAt;
	public Date						updatedAt;
	public int						discountPercentage;
	public float					originalValue;
	public float					dealValue;
	public Date						expiryDate;
	public Date						postDate;
	public String					validTo;
	public Boolean				freeShipping;
	public String					title;
	public String					url;
	
	public Deal(Subscription subscription, int locationId,
			List<Product> products, String email, Date createdAt, Date updatedAt,
			int discountPercentage, float originalValue, float dealValue,
			Date expiryDate, Date postDate, String validTo, Boolean freeShipping,
			String title, String url)
	{
		this.subscription = subscription;
		this.locationId = locationId;
		this.products = products;
		this.email = email;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.discountPercentage = discountPercentage;
		this.originalValue = originalValue;
		this.dealValue = dealValue;
		this.expiryDate = expiryDate;
		this.postDate = postDate;
		this.validTo = validTo;
		this.freeShipping = freeShipping;
		this.title = title;
		this.url = url;
	}
	
	/**
	 *  CREATE TABLE `Deal` (r
			`id` bigint(20) NOT NULL AUTO_INCREMENT,
			`subscriptionId` int(11) NOT NULL, 
			`locationId` int(11) NOT NULL DEFAULT 0,
			`productId` int(11) DEFAULT 0,
			`emailId` int(11) DEFAULT 0,
			`createdAt` datetime DEFAULT NULL,
			`discountPercentage` int(3) DEFAULT NULL,
			`dealValue` int(11) NOT NULL DEFAULT 0,
			`expiryDate` datetime DEFAULT NULL,
			`postDate` datetime DEFAULT NULL,
			`validTo` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
			`freeShipping` bit(1) NOT NULL, 
			`title` varchar(255) DEFAULT NULL,
			`updatedAt` datetime DEFAULT NULL,
			`url` varchar(255) DEFAULT NULL,
			PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 
	 */
}