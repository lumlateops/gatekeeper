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
	@Index(name = "index_product")
	public String	name;
	public String	vertical;
	public String tags;
	public Date		createdAt;
	public Date		updatedAt;

	public Product(String name, String vertical, String tags, Date createdAt, Date updatedAt)
	{
		this.name = name;
		this.tags = tags;
		this.vertical = vertical;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}
