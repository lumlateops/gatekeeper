package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import jsonModels.DealEmailResponse;
import jsonModels.Message;
import jsonModels.Request;
import jsonModels.Service;
import jsonModels.UserDealCategoryResponse;
import jsonModels.UserDealProductResponse;
import jsonModels.UserDealsResponse;
import models.Account;
import models.Deal;
import models.DealCategory;
import models.FetchHistory;
import models.LoginHistory;
import models.Product;
import models.enums.ErrorCodes;
import models.enums.SortFields;
import models.enums.SortOrder;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.JPA;

public class DealController extends BaseContoller
{
	private static final int		PAGE_SIZE									= Integer.parseInt((String)Play.configuration.get("deal.page.size"));
	private static final String	USER_DEAL_LOOKUP_HQL			= "SELECT d AS d FROM Deal d WHERE d.userInfo.id IS ? AND d.dealEmail.emailCategory.id IS 1 ORDER BY ";
	private static final String	USER_DEAL_COUNT_HQL				= "SELECT count(d) FROM Deal d WHERE d.userInfo.id IS ? AND d.dealEmail.emailCategory.id IS 1";
	private static final String	UNREAD_DEAL_COUNT_HQL			= "SELECT count(d) FROM Deal d WHERE d.userInfo.id IS ? AND d.dealEmail.emailCategory.id IS 1 AND d.createdAt > ?";
	private static final String BULK_MARK_DEAL_READ				= "UPDATE Deal d SET d.dealRead = true WHERE d IN (:deals)"; 
	private static final String	DEAL_LOOKUP_HQL						= "SELECT d AS d FROM Deal d WHERE d.userInfo.id IS ? AND d.id IN ";
	private static final String	UNREAD_DEAL_LOOKUP_HQL		= "SELECT d AS d FROM Deal d WHERE d.userInfo.id IS ? AND d.dealRead='false' ";
	private static final String	FETCH_HISTORY_LOOKUP_HQL	= "SELECT f AS f FROM FetchHistory f WHERE f.userInfo.id IS ? ORDER BY fetchStartTime";
	
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
				final int onePageDealsCount = onePageDeals.size();
				final long allDealsCount = Deal.count(USER_DEAL_COUNT_HQL, userId);
				
				if(onePageDeals != null && onePageDealsCount != 0)
				{
					final long pageCount = (allDealsCount/PAGE_SIZE) > 0 ? (allDealsCount/PAGE_SIZE) : 1;
					List<UserDealsResponse> dealsResponse = new ArrayList<UserDealsResponse>();
					constructDealResponse(onePageDeals, dealsResponse);
					
					//Create the response object
					response.put("numberOfResults", 
							new ArrayList<String>()
							{
								{
									add(Integer.toString(onePageDealsCount));
								}
							});
					response.put("numberOfPages", 
							new ArrayList<String>()
							{
								{
									
									add(Long.toString(pageCount));
								}
							});
					response.put("totalDealsCount", 
							new ArrayList<String>()
							{
								{
									
									add(Long.toString(allDealsCount));
								}
							});
					response.put("deals", dealsResponse);
					
					//Mark the deals as read
					EntityManager em = JPA.em();
					Query updateQuery = em.createQuery(BULK_MARK_DEAL_READ);
					updateQuery.setParameter("deals", onePageDeals);
					updateQuery.executeUpdate();
				}
				else
				{
					//Get fetch history
					final FetchHistory fh = FetchHistory.find(FETCH_HISTORY_LOOKUP_HQL, userId).first();
					if(fh != null)
					{
						response.put("numberOfResults", 
								new ArrayList<String>()
								{
									{
										add("0");
									}
								});
						response.put("fetchStatus", 
								new ArrayList<String>()
								{
									{
										add(fh.fetchStatus);
									}
								});
					}
					else
					{
						serviceResponse.addError(ErrorCodes.SERVER_EXCEPTION.toString(), "Could not get email account reading status.");
					}
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
			final long unreadDealsCount;
			LoginHistory loginHistory = LoginHistory.find("userInfo.id", userId).first();
			if(loginHistory != null)
			{
				unreadDealsCount = Deal.count(UNREAD_DEAL_COUNT_HQL, userId, loginHistory.lastLoginTime);
			}
			else
			{
				unreadDealsCount = 0;
			}
			response.put("count", 
					new ArrayList<String>()
					{
						{
							add(Long.toString(unreadDealsCount));
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
	 * TODO: Take flag to know if details are for user or shareurl. Strip email id from email content if for details
	 * End point to get all the details for a deal
	 * @param dealIds: Id of the user deal.
	 */
	public static void getDealDetails(@Required(message="dealId is required")Long dealId,
																		Boolean isShareDetail)
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
				//Strip user email from content if share detail
				if(isShareDetail != null && isShareDetail)
				{
					
				}
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
	
	/**
	 * End point to get all the details for a deal by shareurl
	 * @param shareUrl
	 */
	public static void getDealDetailsByShareUrl(@Required(message="Share url is required")String shareUrl)
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
			final Deal deal = Deal.find("shareUrl", shareUrl).first();
			if(deal != null)
			{
				ArrayList<UserDealsResponse> dealResponse = new ArrayList<UserDealsResponse>();
				List<Deal> deals = new ArrayList<Deal>();
				deals.add(deal);
				constructDealResponse(deals, dealResponse);
				response.put("deal", dealResponse);
			}
			else
			{
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), "No matching deal found.");
			}
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("shareUrl", shareUrl);
		Request request = new Request(isValidRequest, "getDealDetailsByShareurl", endTime - startTime, parameters);

		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));	
	}
	
	/**
	 * Constructs and populates the deals response for incoming deals.
	 * @param dealsToParse
	 * @param dealsResponse
	 */
	private static void constructDealResponse(List<Deal> dealsToParse, 
																						List<UserDealsResponse> dealsResponse)
	{
		if(dealsToParse != null)
		{
			for (Deal deal : dealsToParse)
			{
				boolean isExpired = false;
				if(deal.expiryDate != null)
				{
					try
					{
						DateTime expiry = new DateTime(deal.expiryDate, ISOChronology.getInstanceUTC());
						isExpired = expiry.isBeforeNow();
					}catch(Exception ex)
					{
						Logger.error("Error trying to determine expiry time for expiry date: " + deal.expiryDate + ". Assuming not expired.");
					}
				}
				//Get products
				List<UserDealProductResponse> products = new ArrayList<UserDealProductResponse>();
				if(deal.products != null)
				{
					for (Product product : deal.products)
					{
						products.add(new UserDealProductResponse(product));
					}
				}
				//Get categories
				List<UserDealCategoryResponse> categories = new ArrayList<UserDealCategoryResponse>();
				if(deal.category != null)
				{
					for (DealCategory category : deal.category)
					{
						categories.add(new UserDealCategoryResponse(category));
					}
				}
				dealsResponse.add(new UserDealsResponse(deal, isExpired, products, categories)); 
			}
		}
	}
}