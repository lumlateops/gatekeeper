package models;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;

/**
 * Table to store retailer department information for deal categorization
 * @author prachi
 * 
 *
 */
@Entity
public class Department
{
	@OneToOne
	public Retailer	retailer;
	@Index(name = "index_departments_on_email")
	public String		email;
	public String		name;
	public String		logo;
	public Date			createdAt;
	public Date			updatedAt;
	
	public Department(Retailer retailer, String email, String name,
			String logo, Date createdAt, Date updatedAt)
	{
		this.retailer = retailer;
		this.email = email;
		this.name = name;
		this.logo = logo;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	
	/**
	 * CREATE TABLE `Departments` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`retailerId` int(11) NOT NULL DEFAULT 0, 
`email` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
`name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
`image` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
`createdAT` datetime DEFAULT NULL,
`updatedAT` datetime DEFAULT NULL,
PRIMARY KEY (`id`),
UNIQUE KEY `index_departments_on_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
	 */
}
