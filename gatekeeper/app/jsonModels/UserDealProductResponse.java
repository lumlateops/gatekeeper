package jsonModels;

import models.Product;

/**
 * Represents a Product in user deal response
 * @author prachi
 *
 */
public class UserDealProductResponse
{
	public Long id;
	public String	item;
	public String	categoryName;
	
	public UserDealProductResponse(Product product)
	{
		this(product.id, product.item, product.categoryName);
	}

	public UserDealProductResponse(Long id, String item, String categoryName)
	{
		this.id = id;
		this.item = item;
		this.categoryName = categoryName;
	}
}
