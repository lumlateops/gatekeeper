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
| createdAT | datetime     | YES  |     | NULL    |                |
| updatedAT | datetime     | YES  |     | NULL    |                |
+-----------+--------------+------+-----+---------+----------------+
*/
@Entity
public class Retailers extends Model
{
	public String	domain;
	public String	name;
	public String	image;
	public Date		createdAT;
	public Date		updatedAT;
	
	public Retailers(String domain, String name, String image, Date createdAT,
									Date updatedAT)
	{
		this.domain = domain;
		this.name = name;
		this.image = image;
		this.createdAT = createdAT;
		this.updatedAT = updatedAT;
	}
}