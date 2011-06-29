package models;


import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import play.Logger;
import play.data.validation.Email;
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