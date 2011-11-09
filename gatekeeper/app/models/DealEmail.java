package models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import play.db.jpa.Model;

/**
 * Stored the deal emails we read in from user's inboxes
 * @author prachi
 *
 */
@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(name="uniqueDealEmail", columnNames = {"dateReceived", "fromEmail", "subject", "toName"})})
public class DealEmail extends Model
{
    public String        fromName;
    public String        fromEmail;
    public String        toName;
    public String        subject;
    public String        senderIP;
    public String        spfResult;
    public String        domainKey;
    public Date          sentDate;
    public Date          dateReceived;
    @OneToOne
    public EmailCategory emailCategory;
    @Column(columnDefinition = "mediumtext", length = 5000)
    public String        parsedContent;
    @Column(columnDefinition = "mediumtext", length = 5000)
    public String        content;
    @Column(columnDefinition = "mediumtext", length = 5000)
    public String       unsubscribeLinks;
    
		public DealEmail(String fromName, String fromEmail, String toName,
				String subject, String senderIP, String spfResult, String domainKey,
				Date sentDate, Date dateReceived, EmailCategory emailCategory,
				String parsedContent, String content, String unsubscribeLinks)
		{
			this.fromName = fromName;
			this.fromEmail = fromEmail;
			this.toName = toName;
			this.subject = subject;
			this.senderIP = senderIP;
			this.spfResult = spfResult;
			this.domainKey = domainKey;
			this.sentDate = sentDate;
			this.dateReceived = dateReceived;
			this.emailCategory = emailCategory;
			this.parsedContent = parsedContent;
			this.content = content;
			this.unsubscribeLinks = unsubscribeLinks;
		}
}
