package jsonModels;

import java.util.Date;

import models.Deal;
import models.Retailers;

/**
 * 
 * "deals":[
            {
               "userId":2,
               "title":"Discount clothes",
               "description":"40% off all apparel",
               "discountPercentage":"40%",
               "expiryDate":"Jun 13, 2012 5:00:00 PM",
               "postDate":"Jun 13, 2009 5:00:00 PM",
               "url":"http://mydeal.com",
               "retailer":{
                  "domain":"target.com",
                  "name":"Target",
                  "image":"http://th209.photobucket.com/albums/bb175/clayandcrystal/th_target_logo.jpg",
                  "id":2
               },
               "id":1
            }
         ]
 * @author prachi
 *
 */
public class UserDealsResponse
{
	private Long userId;
	private String title;
	private String description; // get from email subject
	private int discountPercentage ;
	private Date expiryDate ;
	private Date postDate ;
	private String url;
	private Long id;
	private Retailers retailer;
	private boolean freeShipping;
	private float originalValue;
	private float dealValue;
	private String validTo;
	
	public UserDealsResponse(Deal deal)
	{
		this(deal.userInfo.id, deal.title, deal.dealEmail.subject,
				deal.discountPercentage, deal.expiryDate, deal.postDate, deal.url,
				deal.id, deal.subscription.department.retailer, deal.freeShipping, deal.originalValue,
				deal.dealValue, deal.validTo);
	}
	
	public UserDealsResponse(Long userId, String title, String description,
			int discountPercentage, Date expiryDate, Date postDate, String url,
			Long id, Retailers retailer, boolean freeShipping, float originalValue,
			float dealValue, String validTo)
	{
		this.userId = userId;
		this.title = title;
		this.description = description;
		this.discountPercentage = discountPercentage;
		this.expiryDate = expiryDate;
		this.postDate = postDate;
		this.url = url;
		this.id = id;
		this.retailer = retailer;
		this.freeShipping = freeShipping;
		this.originalValue = originalValue;
		this.dealValue = dealValue;
		this.validTo = validTo;
	}
}
