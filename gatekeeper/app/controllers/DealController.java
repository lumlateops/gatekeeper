package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

import bl.Utility;

import jsonModels.DealEmailResponse;
import jsonModels.Message;
import jsonModels.Request;
import jsonModels.Service;
import jsonModels.UserDealsResponse;
import models.Account;
import models.Deal;
import models.ErrorCodes;
import models.Retailers;
import models.ServiceProvider;
import models.SortFields;
import models.SortOrder;
import play.Logger;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;

public class DealController extends Controller
{
	private static final int		PAGE_SIZE									= 20;
	private static final String	USER_DEAL_LOOKUP_HQL			= "SELECT d AS d FROM Deal d WHERE d.userInfo.id IS ? AND d.dealEmail.emailCategory.id IS 1 ORDER BY ";
	private static final String BULK_MARK_DEAL_READ				= "UPDATE Deal d SET d.dealRead = true WHERE d IN (:deals)"; 
	private static final String	DEAL_LOOKUP_HQL						= "SELECT d AS d FROM Deal d WHERE d.userInfo.id IS ? AND d.id IN ";
	private static final String	UNREAD_DEAL_LOOKUP_HQL		= "SELECT d AS d FROM Deal d WHERE d.userInfo.id IS ? AND d.dealRead='false' ";
	
	@Before
	public static void logRequest()
	{
		Utility.logRequest();
	}
	
	@After
	public static void logResponse()
	{
		Utility.logResponse();
	}
	
	/**
	 * End point to get the deals for a user
	 * @param userId: Id of the user we want to get deals for.
	 * @param page: Result page number to return. Default is 1.
	 * @param sort: The field to sort the results by. Possible values are {@code SortFields}. Default is POST_DATE
	 * @param sortOrder: Order of sort. Possible values are {@code SortOrder}. Default is DESC
	 */
	public static void getUserDeals(@Required(message="userId is required")Long userId,
																	int page, String sort, String sortOrder)
	{
		Long startTime = System.currentTimeMillis();
		Boolean isValidRequest = Boolean.TRUE;
		
		Service serviceResponse = new Service();
		Map<String, List<?>> response = new HashMap<String, List<?>>(); 
		
		// Validate input
		if(Validation.hasErrors())
		{
			isValidRequest = false;
			for (play.data.validation.Error validationError : Validation.errors())
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), validationError.message());
			}
		}
		else
		{
			//Set defaults
			if(page <= 0)
			{
				page = 1;
			}
			if(sort == null || SortFields.valueOf(sort) == null)
			{
				sort = SortFields.postDate.toString();
			}
			if(sortOrder == null || SortOrder.valueOf(sortOrder) == null)
			{
				sortOrder = SortOrder.DESC.toString();
			}
			
			// Adjust result for page number
			int startIndex = PAGE_SIZE * (page - 1);
			int endIndex = startIndex + PAGE_SIZE;

			if(startIndex >= endIndex)
			{
				response.put("numberOfResults", 
						new ArrayList<String>()
						{
							{
								add("0");
							}
						});
			}
			else
			{
				// Get deals for user
				String query = USER_DEAL_LOOKUP_HQL + sort + " " + sortOrder;
				final List<Deal> onePageDeals = Deal.find(query, userId).from(startIndex).fetch(PAGE_SIZE);
				final int allDealsCount = onePageDeals.size();
				
				if(onePageDeals != null && allDealsCount != 0)
				{
					if(onePageDeals != null && onePageDeals.size() != 0)
					{
						response.put("numberOfResults", 
								new ArrayList<String>()
								{
									{
										add(Integer.toString(allDealsCount));
									}
								});
						final int pageCount = (allDealsCount/PAGE_SIZE) > 0 ? (allDealsCount/PAGE_SIZE) : 1;
						//Create the response object
						List<UserDealsResponse> dealsResponse = new ArrayList<UserDealsResponse>();
						for (Deal deal : onePageDeals)
						{
							boolean isExpired = true;
							if(deal.expiryDate != null)
							{
								try
								{
									DateTime expiry = new DateTime(deal.expiryDate, ISOChronology.getInstanceUTC());
									isExpired = expiry.isBeforeNow();
								}catch(Exception ex)
								{
									Logger.error("Error trying to determine expiry time for expiry date: " + deal.expiryDate);
								}
							}
							dealsResponse.add(new UserDealsResponse(deal, isExpired)); 
						}
						response.put("numberOfPages", 
								new ArrayList<String>()
								{
									{
										
										add(Integer.toString(pageCount));
									}
								});
						response.put("deals", dealsResponse);
					}
					else
					{
						response.put("numberOfResults", 
								new ArrayList<String>()
								{
									{
										add("0");
									}
								});
					}
					
					//Mark the deals as read
					EntityManager em = JPA.em();
					Query updateQuery = em.createQuery(BULK_MARK_DEAL_READ);
					updateQuery.setParameter("deals", onePageDeals);
					updateQuery.executeUpdate();
				}
				else
				{
					response.put("numberOfResults", 
							new ArrayList<String>()
							{
								{
									add("0");
								}
							});
				}
			}
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();
		if(userId != null)
		{
			parameters.put("userId", Long.toString(userId));
		}else
		{
			parameters.put("userId", "null");
		}
		parameters.put("page", Integer.toString(page));
		parameters.put("sort", sort);
		parameters.put("sortOrder", sortOrder);
		Request request = new Request(isValidRequest, "getUserDeals", endTime - startTime, parameters);

		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));	
	}
	
	/**
	 * End point to mark deals as read.
	 * @param userId: Id of the user we want to get deals for.
	 * @param dealIds: Ids of the deals to be marked as read.
	 */
	public static void markDealsRead(@Required(message="userId is required")Long userId,
																	@Required(message="dealIds are required")String dealIds)
	{
		Long startTime = System.currentTimeMillis();
		Boolean isValidRequest = Boolean.TRUE;
		
		Service serviceResponse = new Service();
		Map<String, List<?>> response = new HashMap<String, List<?>>(); 
		
		// Validate input
		if(Validation.hasErrors())
		{
			isValidRequest = false;
			for (play.data.validation.Error validationError : Validation.errors())
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), validationError.message());
			}
		}
		else
		{
			String queryString = DEAL_LOOKUP_HQL + "(" + dealIds + ")";
			List<Deal> deals = Deal.find(queryString, userId).fetch();
			if(deals != null && !deals.isEmpty())
			{
				for (Deal deal : deals)
				{
					deal.dealRead = true;
					deal.save();
				}
				
				response.put("status", 
						new ArrayList<String>()
						{
							{
								add("ok");
							}
						});
			}
			else
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), "No matching deal found.");
			}
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();
		if(userId != null)
		{
			parameters.put("userId", Long.toString(userId));
		}else
		{
			parameters.put("userId", "null");
		}
		if(dealIds != null)
		{
			parameters.put("dealIds", dealIds);
		}
		Request request = new Request(isValidRequest, "markDealRead", endTime - startTime, parameters);

		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));	
	}
	
	/**
	 * End point to mark a user deals as unread.
	 * @param userId: Id of the user we want to get deals for.
	 * @param dealIds: Id of the user deals to be marked as unread.
	 */
	public static void markDealsUnRead(@Required(message="userId is required")Long userId,
																		 @Required(message="dealIds are required")String dealIds)
	{
		Long startTime = System.currentTimeMillis();
		Boolean isValidRequest = Boolean.TRUE;
		
		Service serviceResponse = new Service();
		Map<String, List<?>> response = new HashMap<String, List<?>>(); 
		
		// Validate input
		if(Validation.hasErrors())
		{
			isValidRequest = false;
			for (play.data.validation.Error validationError : Validation.errors())
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), validationError.message());
			}
		}
		else
		{
			String queryString = DEAL_LOOKUP_HQL + "(" + dealIds + ")";
			List<Deal> deals = Deal.find(queryString, userId).fetch();
			if(deals != null && !deals.isEmpty())
			{
				for (Deal deal : deals)
				{
					deal.dealRead = false;
					deal.save();
				}
				
				response.put("status", 
						new ArrayList<String>()
						{
							{
								add("ok");
							}
						});
			}
			else
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), "No matching deal found.");
			}
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();
		if(userId != null)
		{
			parameters.put("userId", Long.toString(userId));
		}else
		{
			parameters.put("userId", "null");
		}
		if(dealIds != null)
		{
			parameters.put("dealIds", dealIds);
		}
		Request request = new Request(isValidRequest, "markDealsUnRead", endTime - startTime, parameters);

		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));	
	}
	
	/**
	 * End point to mark a user deals as unread.
	 * @param userId: Id of the user we want to get deals for.
	 * @param dealIds: Id of the user deals to be marked as unread.
	 */
	public static void getUnreadDealCount(@Required(message="userId is required")Long userId)
	{
		Long startTime = System.currentTimeMillis();
		Boolean isValidRequest = Boolean.TRUE;
		
		Service serviceResponse = new Service();
		Map<String, List<?>> response = new HashMap<String, List<?>>(); 
		
		// Validate input
		if(Validation.hasErrors())
		{
			isValidRequest = false;
			for (play.data.validation.Error validationError : Validation.errors())
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), validationError.message());
			}
		}
		else
		{
			List<Deal> deals = Deal.find(UNREAD_DEAL_LOOKUP_HQL, userId).fetch();
			final int count;
			if(deals != null && !deals.isEmpty())
			{
				count = deals.size();
			}
			else
			{
				count = 0;
			}
			response.put("count", 
					new ArrayList<String>()
					{
						{
							add(Integer.toString(count));
						}
					});
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();
		if(userId != null)
		{
			parameters.put("userId", Long.toString(userId));
		}else
		{
			parameters.put("userId", "null");
		}
		Request request = new Request(isValidRequest, "getUnreadDealCount", endTime - startTime, parameters);

		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));	
	}
	
	/**
	 * Returns the email address the deal was received on.
	 * @param dealId: Id of the deal we want info for.
	 */
	public static void getDealUserEmail(@Required(message="dealId is required")Long dealId)
	{
		Long startTime = System.currentTimeMillis();
		Boolean isValidRequest = Boolean.TRUE;
		
		Service serviceResponse = new Service();
		Map<String, List<?>> response = new HashMap<String, List<?>>(); 
		
		// Validate input
		if(Validation.hasErrors())
		{
			isValidRequest = false;
			for (play.data.validation.Error validationError : Validation.errors())
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), validationError.message());
			}
		}
		else
		{
			Deal deal = Deal.find("id", dealId).first();
			if(deal != null && deal.subscription != null)
			{
				Account account = Account.find("id", deal.subscription.accountId).first();
				if(account != null)
				{
					List<DealEmailResponse> message = new ArrayList<DealEmailResponse>();
					message.add(new DealEmailResponse(dealId, account.email, deal.postDate));
					response.put("dealInfo", message);
				}
				else
				{
					serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), "No matching Account found");
				}
			}
			else
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), "No matching deal found");
			}
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();
		if(dealId != null)
		{
			parameters.put("dealId", Long.toString(dealId));
		}else
		{
			parameters.put("dealId", "null");
		}
		Request request = new Request(isValidRequest, "getDealUserEmail", endTime - startTime, parameters);

		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));	
	}
	
	/**
	 * End point to get all the details for a deal
	 * @param dealIds: Id of the user deal.
	 */
	public static void getDealDetails(@Required(message="dealId is required")Long dealId)
	{
		Long startTime = System.currentTimeMillis();
		Boolean isValidRequest = Boolean.TRUE;
		
		Service serviceResponse = new Service();
		Map<String, List<?>> response = new HashMap<String, List<?>>(); 
		
		// Validate input
		if(Validation.hasErrors())
		{
			isValidRequest = false;
			for (play.data.validation.Error validationError : Validation.errors())
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), validationError.message());
			}
		}
		else
		{
			final Deal deal = Deal.find("id", dealId).first();
			if(deal != null)
			{
				response.put("deal", 
						new ArrayList<Deal>()
						{
							{
								add(deal);
							}
						});
			}
			else
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), "No matching deal found.");
			}
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();
		if(dealId != null)
		{
			parameters.put("dealId", Long.toString(dealId));
		}else
		{
			parameters.put("dealId", "null");
		}
		Request request = new Request(isValidRequest, "getDealDetails", endTime - startTime, parameters);

		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));	
	}
}