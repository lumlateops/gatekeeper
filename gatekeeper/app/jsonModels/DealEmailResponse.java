package jsonModels;

import java.util.Date;

public class DealEmailResponse
{
	private Long dealId;
	private String dealEmail;
	private Date dealEmailReceivedDate;
	
	public DealEmailResponse(Long dealId, String dealEmail,
			Date dealEmailReceivedDate)
	{
		super();
		this.dealId = dealId;
		this.dealEmail = dealEmail;
		this.dealEmailReceivedDate = dealEmailReceivedDate;
	}
}
