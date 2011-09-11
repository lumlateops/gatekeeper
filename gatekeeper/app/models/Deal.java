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
	public Long						userId;
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
	public boolean				dealRead;
	public Long						dealEmailId;
	
	public Deal(Long userId, Subscription subscription, int locationId,
			List<Product> products, String email, Date createdAt, Date updatedAt,
			int discountPercentage, float originalValue, float dealValue,
			Date expiryDate, Date postDate, String validTo, Boolean freeShipping,
			String title, String url, boolean dealRead, Long dealEmailId)
	{
		this.userId = userId;
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
		this.dealRead = dealRead;
		this.dealEmailId = dealEmailId;
	}
}