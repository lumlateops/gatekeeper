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

@Entity
public class Deal extends Model
{
	public Long			userId;
	public String		title;
	public String		description;
	public String		discountPercentage;
	public Date			expiryDate;
	public Date			postDate;
	public String		url;
	public Date			createdAt;
	public Date			updatedAt;

	@ManyToMany
	public List<DealCategory>	dealCategory;
	
	@OneToOne
	public Retailers retailers;

	public Deal(Long userId, String title, String description,
			String discountPercentage, Date expiryDate, Date postDate, String url,
			Date createdAt, Date updatedAt, List<DealCategory> dealCategory,
			Retailers retailers)
	{
		this.userId = userId;
		this.title = title;
		this.description = description;
		this.discountPercentage = discountPercentage;
		this.expiryDate = expiryDate;
		this.postDate = postDate;
		this.url = url;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.dealCategory = dealCategory;
		this.retailers = retailers;
	}
	
	/**
	 * {
   "numberOfResults":"Total number of Deals",
   "numberOfPages":"Total number of pages with deals for this user",
   "deals":[
      {
         "id":"123456",
         "title":"Title of the deal",
         "description":"Description of the deal",
         "discountPercentage":"50%",
         "expiryDate":"Any time or date based string. Preferable format: MM/DD/YYYY",
         "postDate":"Day the deal was posted or received in the email by the user. MM/DD/YYYY",
         "url":"Optional deal url into poster's or publisher's url",
         "dealCategories":[
            {
               "id":"123",
               "name":"Apparel" 
            },
            {
               "id":"11244",
               "name":"Kids Clothing" 
            }
         ],
         "publisherId":"23663",
         "publisherName":"Macys",
         "publisherUrl":"http://www.macys.com",
         "publisherLogoUrl":"URL for the logo" 
      }
   ]
}
	 */
}