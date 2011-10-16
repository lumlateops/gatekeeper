package jsonModels;

import java.util.Date;
import java.util.List;

import models.Wallet;

/**

 * @author prachi
 *
 */
public class UserWalletResponse
{
	private Long walletId;
	private Long dealId;
	private Long userId;
	private Date alertTime;
	private Date createdAt;
	private String title;
	private String description; // get from email subject
	private int discountPercentage ;
	private Date expiryDate ;
	private Date postDate ;
	private String url;
	private UserDealRetailerResponse retailer;
	private boolean freeShipping;
	private boolean isExpired;
	private float originalValue;
	private float dealValue;
	private String validTo;
	private List<UserDealProductResponse> products;
	private List<UserDealCategoryResponse> categories;
	
	public UserWalletResponse(Wallet wallet, boolean isExpired,
														List<UserDealProductResponse> products, 
														List<UserDealCategoryResponse> categories)
	{
		this(wallet.id, wallet.deal.id, wallet.userInfo.id, wallet.alertTime,
				wallet.createdAt, wallet.deal.title, wallet.deal.dealEmail.parsedContent,
				wallet.deal.discountPercentage, wallet.deal.expiryDate,
				wallet.deal.postDate, wallet.deal.url, 
				new UserDealRetailerResponse(wallet.deal.subscription.department), 
				wallet.deal.freeShipping, isExpired, wallet.deal.originalValue, 
				wallet.deal.dealValue, wallet.deal.validTo, products, categories);
	}
	
	public UserWalletResponse(Long walletId, Long dealId, Long userId,
			Date alertTime, Date createdAt, String title, String description,
			int discountPercentage, Date expiryDate, Date postDate, String url,
			UserDealRetailerResponse retailer, boolean freeShipping,
			boolean isExpired, float originalValue, float dealValue, String validTo,
			List<UserDealProductResponse> products, List<UserDealCategoryResponse> categories)
	{
		this.walletId = walletId;
		this.dealId = dealId;
		this.userId = userId;
		this.alertTime = alertTime;
		this.createdAt = createdAt;
		this.title = title;
		this.description = description;
		this.discountPercentage = discountPercentage;
		this.expiryDate = expiryDate;
		this.postDate = postDate;
		this.url = url;
		this.retailer = retailer;
		this.freeShipping = freeShipping;
		this.isExpired = isExpired;
		this.originalValue = originalValue;
		this.dealValue = dealValue;
		this.validTo = validTo;
		this.products = products;
		this.categories = categories;
	}
}
