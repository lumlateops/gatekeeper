package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * Stored the deal emails we read in from user's inboxes
 * @author prachi
 *
 */
@Entity
public class EmailCategory extends Model
{
	// Can be deal, subscription, spam, other, confirmation
	public String	name;

	public EmailCategory(String name)
	{
		this.name = name;
	}
}
