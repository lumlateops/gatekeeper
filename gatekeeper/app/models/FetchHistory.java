package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class FetchHistory extends Model
{
	@ManyToOne
	public UserInfo	userInfo;
	public Date			fetchStartTime;
	public Date			fetchEndTime;
	public String		fetchStatus;
	public String		fetchErrorMessage;
	public String		sessionid;
  
	public FetchHistory(UserInfo userInfo, Date fetchStartTime, Date fetchEndTime,
			String fetchStatus, String fetchErrorMessage, String sessionid)
	{
		this.userInfo = userInfo;
		this.fetchStartTime = fetchStartTime;
		this.fetchEndTime = fetchEndTime;
		this.fetchStatus = fetchStatus;
		this.fetchErrorMessage = fetchErrorMessage;
		this.sessionid = sessionid;
	}
}
