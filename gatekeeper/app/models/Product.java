package models;

import java.sql.Date;

import javax.persistence.Entity;

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
	public String	category;
	public Date		createdAt;
	public Date		updatedAt;

	public Product(String item, String category, Date createdAt, Date updatedAt)
	{
		this.item = item;
		this.category = category;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}
