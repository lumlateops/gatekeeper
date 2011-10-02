package models;

import java.sql.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Index;

import play.db.jpa.Model;

/**
 * Table to keep track of each product in the deals
 * @author prachi
 *
 */
@Entity
public class Product extends Model
{
	@Index(name = "index_product_item")
	public String	item;
	@Index(name = "index_product_category")
	@ManyToOne
	public DealCategory	category;
	public String	categoryName;
	public Date		createdAt;
	public Date		updatedAt;

	public Product(String item, DealCategory category, Date createdAt, Date updatedAt, String categoryName)
	{
		this.item = item;
		this.category = category;
		this.categoryName = categoryName;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}
