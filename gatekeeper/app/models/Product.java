package models;

import java.sql.Date;

import javax.persistence.Entity;

import org.hibernate.annotations.Index;

/**
 * Table to keep track of each product in the deals
 * @author prachi
 *
 */
@Entity
public class Product
{
	@Index(name = "index_product")
	public String	name;
	public String	vertical;
	public Date		createdAt;
	public Date		updatedAt;

	public Product(String name, String vertical, Date createdAt, Date updatedAt)
	{
		this.name = name;
		this.vertical = vertical;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/**
	 * CREATE TABLE `Product` (
			`id` int(11) NOT NULL AUTO_INCREMENT,
			`name` varchar(255) COLLATE utf8_unicode_ci NOT NULL, 
			`vertical` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
			`createdAT` datetime DEFAULT NULL,
			`updatedAT` datetime DEFAULT NULL,
			PRIMARY KEY (`id`),
			UNIQUE KEY `index_product` (`name`)
			) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
	 */
}
