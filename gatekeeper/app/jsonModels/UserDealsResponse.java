package jsonModels;

import java.util.Date;
import java.util.List;

import models.Deal;

/**
 * Wrapper class to represent the user deal response
 * @author prachi
 *
 */
public class UserDealsResponse
{
	private Long id;
	private String url;
	private String tags;
	private String title;
	private Date postDate ;
	private String validTo;
	private String shareUrl;
	private Date expiryDate;
	private float dealValue;
	private boolean isExpired;
	public boolean isInWallet;
	private String description; // get from email subject
	private float originalValue;
	private boolean freeShipping;
	private int discountPercentage ;
	private List<UserDealProductResponse> products;
	private UserDealRetailerResponse retailer;
	private List<UserDealCategoryResponse> categories;
	
	public UserDealsResponse(Deal deal, boolean isExpired, 
													 List<UserDealProductResponse> products, 
													 List<UserDealCategoryResponse> categories)
	{
		this(deal.title, deal.dealEmail.parsedContent,
				deal.discountPercentage, deal.expiryDate, deal.postDate, deal.url,
				deal.id, new UserDealRetailerResponse(deal.subscription.department), deal.freeShipping, deal.originalValue,
				deal.dealValue, deal.validTo, isExpired, deal.tags, products, categories, deal.dealInWallet, deal.shareUrl);
	}
	
	public UserDealsResponse(String title, String description,
			int discountPercentage, Date expiryDate, Date postDate, String url,
			Long id, UserDealRetailerResponse retailer, boolean freeShipping, float originalValue,
			float dealValue, String validTo, boolean isExpired, String tags, 
			List<UserDealProductResponse> products, List<UserDealCategoryResponse> categories, 
			boolean isInWallet, String shareUrl)
	{
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
		this.isExpired = isExpired;
		this.tags = tags;
		this.products = products;
		this.categories = categories;
		this.isInWallet = isInWallet;
		this.shareUrl = shareUrl;
	}
}
