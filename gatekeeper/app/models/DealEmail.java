package models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import play.db.jpa.Model;

/**
 * Stored the deal emails we read in from user's inboxes
 * @author prachi
 *
 */
@Entity
public class DealEmail extends Model
{
	public String					fromName;
	public String					fromEmail;
	public String					toName;
	public String					subject;
	public String					senderIP;
	public String					spfResult;
	public String					domainKey;
	@Column(columnDefinition = "mediumtext", length = 5000)
	public String					parsedContent;
	@Column(columnDefinition = "mediumtext", length = 5000)
	public String					content;
	@OneToOne
	public EmailCategory	emailCategory;
	public Date						sentDate;
	public Date						dateReceived;
	
	public DealEmail(String fromName, String fromEmail, String toName,
			Date dateReceived, Date sentDate, String subject, String senderIP,
			String spfResult, String domainKey, String parsedContent, String content,
			EmailCategory emailCategory)
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
		this.parsedContent = parsedContent;
		this.content = content;
		this.emailCategory = emailCategory;
	}
}
