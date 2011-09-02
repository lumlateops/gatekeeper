package models;

import java.sql.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * Stored the deal emails we read in from user's inboxes
 * @author prachi
 *
 */
@Entity
public class DealEmail extends Model
{
	public String	fromName;
	public String	fromEmail;
	public String	toName;
	public String	subject;
	public String	senderIP;
	public String	spfResult;
	public String	domainKey;
	public String	rawContent;
	public String	fullText;
	public String	category;
	public Date		sentDate;
	public Date		dateReceived;
	
	public DealEmail(String fromName, String fromEmail, String toName,
			Date dateReceived, Date sentDate, String subject, String senderIP,
			String spfResult, String domainKey, String rawContent, String fullText,
			String category)
	{
		this.fromName = fromName;
		this.fromEmail = fromEmail;
		this.toName = toName;
		this.dateReceived = dateReceived;
		this.sentDate = sentDate;
		this.subject = subject;
		this.senderIP = senderIP;
		this.spfResult = spfResult;
		this.domainKey = domainKey;
		this.rawContent = rawContent;
		this.fullText = fullText;
		this.category = category;
	}
}
