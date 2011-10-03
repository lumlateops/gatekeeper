package models;


import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class DealCategory extends Model
{
	public String category;
	public String description;
	
	public DealCategory(String category, String description)
	{
		this.category = category;
		this.description = description;
	}
}