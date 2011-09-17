package models;


import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import play.Logger;
import play.data.validation.Email;
import play.db.jpa.Model;

/**
+-----------+--------------+------+-----+---------+----------------+
| Field     | Type         | Null | Key | Default | Extra          |
+-----------+--------------+------+-----+---------+----------------+
| id        | int(11)      | NO   | PRI | NULL    | auto_increment |
| domain    | varchar(255) | NO   | UNI | NULL    |                |
| name      | varchar(255) | NO   |     | NULL    |                |
| image     | varchar(255) | YES  |     | NULL    |                |
| createdAt | datetime     | YES  |     | NULL    |                |
| updatedAt | datetime     | YES  |     | NULL    |                |
+-----------+--------------+------+-----+---------+----------------+
*/
@Entity
public class Retailers extends Model
{
	public String	domain;
	public String	name;
	public String	image;
	public Date		createdAt;
	public Date		updatedAt;
	
	public Retailers(String domain, String name, String image, Date createdAt,
									Date updatedAt)
	{
		this.domain = domain;
		this.name = name;
		this.image = image;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}