package jsonModels;

import models.Department;

public class UserDealRetailerResponse
{
	public Long 	id;
	public String	domain;
	public String	name;
	public String	image;
	public String email;
	
	public UserDealRetailerResponse(Department department)
	{
		this(department.retailer.id, department.retailer.domain, 
				department.retailer.name, department.retailer.image, department.email);
	}
	
	public UserDealRetailerResponse(Long id, String domain, String name, String image, String email)
	{
		this.id = id;
		this.domain = domain;
		this.name = name;
		this.image = image;
		this.email = email;
	}
}
