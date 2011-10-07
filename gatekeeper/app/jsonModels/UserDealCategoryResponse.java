package jsonModels;

import models.DealCategory;

/**
 * Represents a Deal Category in user deal response
 * @author prachi
 *
 */
public class UserDealCategoryResponse
{
	public Long 	id;
	public String category;
	public String description;
	
	public UserDealCategoryResponse(DealCategory category)
	{
		this(category.id, category.category, category.description);
	}

	public UserDealCategoryResponse(Long id, String category, String description)
	{
		this.id = id;
		this.category = category;
		this.description = description;
	}
}
