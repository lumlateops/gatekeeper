package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class FetchHistory extends Model
{
  public Long userId;
  public Date fetchStartTime;
  public Date fetchEndTime;
  public String fetchStatus;
  public String fetchErrorMessage;
  public String sessionid;
  
	public FetchHistory(Long userId, Date fetchStartTime, Date fetchEndTime,
			String fetchStatus, String fetchErrorMessage, String sessionid)
	{
		this.userId = userId;
		this.fetchStartTime = fetchStartTime;
		this.fetchEndTime = fetchEndTime;
		this.fetchStatus = fetchStatus;
		this.fetchErrorMessage = fetchErrorMessage;
		this.sessionid = sessionid;
	}
}
