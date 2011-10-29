package jsonModels;

import java.util.Date;
import java.util.List;

/**
 * Wrapper class to represent the user deal response
 * @author prachi
 *
 */
public class DealShareResponse
{
	private Long id;
	private String url;
	private String tags;
	private String title;
	private Date postDate;
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
}
