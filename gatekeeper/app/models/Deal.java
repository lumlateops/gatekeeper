package models;


import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import play.db.jpa.Model;

/**
 * 
 */
@Entity
public class Deal extends Model
{
	@ManyToOne
	public UserInfo				userInfo;
	@OneToOne
	public Subscription		subscription;
	public int						locationId;
	@ManyToMany
	public List<Product>	products;
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
	public boolean				dealInWallet;
	@ManyToOne
	public DealEmail			dealEmail;
	public String					tags;
	@ManyToMany
	public List<DealCategory>	category;
	public String					shareUrl;
	
	public Deal(UserInfo userInfo, Subscription subscription, int locationId,
			List<Product> products, Date createdAt, Date updatedAt,
			int discountPercentage, float originalValue, float dealValue,
			Date expiryDate, Date postDate, String validTo, Boolean freeShipping,
			String title, String url, boolean dealRead, boolean dealInWallet, DealEmail dealEmail, 
			String tags, List<DealCategory>	category, String shareUrl)
	{
		this.userInfo = userInfo;
		this.subscription = subscription;
		this.locationId = locationId;
		this.products = products;
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
		this.dealInWallet = dealInWallet;
		this.dealEmail = dealEmail;
		this.tags = tags;
		this.category = category;
		this.shareUrl = shareUrl;
	}
}