package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jsonModels.Message;
import jsonModels.Request;
import jsonModels.Service;
import jsonModels.UserDealCategoryResponse;
import jsonModels.UserDealProductResponse;
import jsonModels.UserWalletResponse;
import models.Deal;
import models.DealCategory;
import models.Product;
import models.UserInfo;
import models.Wallet;
import models.enums.ErrorCodes;
import models.enums.SortFields;
import models.enums.SortOrder;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import bl.utilities.Utility;

public class WalletController extends Controller
{
	private static final int PAGE_SIZE	= Integer.parseInt((String)Play.configuration.get("deal.page.size"));
	private static final String	USER_WALLET_LOOKUP_HQL = "SELECT w AS w FROM Wallet w WHERE w.userInfo.id IS ? ORDER BY w.deal.";
	private static final String	WALLET_ENTRY_LOOKUP_HQL = "SELECT w AS w FROM Wallet w WHERE w.userInfo.id IS ? AND w.deal.id IS ?";
	
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
	 * End point to get the deals for a user's wallet
	 * @param userId: Id of the user we want to get deals for.
	 * @param page: Result page number to return. Default is 1.
	 * @param sort: The field to sort the results by. Possible values are {@code SortFields}. Default is POST_DATE
	 * @param sortOrder: Order of sort. Possible values are {@code SortOrder}. Default is DESC
	 */
	public static void getWalletDeals(@Required(message="userId is required")Long userId,
																		@Required(message="page is required")int page, 
																		@Required(message="sort is required")String sort, 
																		@Required(message="sort order is required")String sortOrder)
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
				String query = USER_WALLET_LOOKUP_HQL + sort + " " + sortOrder;
				final List<Wallet> onePageDeals = Wallet.find(query, userId).from(startIndex).fetch(PAGE_SIZE);
				final int onePageDealsCount = onePageDeals.size();
				final long allDealsCount = onePageDeals.size();
				
				if(onePageDeals != null && onePageDealsCount != 0)
				{
					//Create the response object
					final long pageCount = (allDealsCount/PAGE_SIZE) > 0 ? (allDealsCount/PAGE_SIZE) : 1;
					List<UserWalletResponse> walletResponse = new ArrayList<UserWalletResponse>();
					for (Wallet wallet : onePageDeals)
					{
						boolean isExpired = true;
						if(wallet.deal.expiryDate != null)
						{
							try
							{
								DateTime expiry = new DateTime(wallet.deal.expiryDate, ISOChronology.getInstanceUTC());
								isExpired = expiry.isBeforeNow();
							}catch(Exception ex)
							{
								Logger.error("Error trying to determine expiry time for expiry date: " + wallet.deal.expiryDate);
							}
						}
						//Get products
						List<UserDealProductResponse> products = new ArrayList<UserDealProductResponse>();
						if(wallet.deal.products != null)
						{
							for (Product product : wallet.deal.products)
							{
								products.add(new UserDealProductResponse(product));
							}
						}
						//Get categories
						List<UserDealCategoryResponse> categories = new ArrayList<UserDealCategoryResponse>();
						if(wallet.deal.category != null)
						{
							for (DealCategory category : wallet.deal.category)
							{
								categories.add(new UserDealCategoryResponse(category));
							}
						}
						UserWalletResponse userWalletResponse = new UserWalletResponse(wallet, isExpired, products, categories);
						walletResponse.add(userWalletResponse);
					}
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
					response.put("wallet", walletResponse);
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
		Request request = new Request(isValidRequest, "getWalletDeals", endTime - startTime, parameters);

		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));	
	}
	
	/**
	 * Adds a deal to a user's wallet.
	 * @param dealId
	 * @param userId
	 */
	public static void addWalletDeals(@Required(message="dealId is required")Long dealId,
																		@Required(message="userId is required")Long userId,
																		Date alertTime)
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
			//Get user
			UserInfo userInfo = UserInfo.find("id", userId).first();
			
			//Get deal
			Deal deal = Deal.find("id", dealId).first();
			
			//Create wallet entry
			if(deal != null && userInfo != null)
			{
				Wallet wallet = new Wallet(deal, userInfo, isValidRequest, alertTime, new Date(System.currentTimeMillis()));
				wallet.save();
				response.put("status", 
						new ArrayList<String>()
						{
							{
								add("ok");
							}
						});
				//Mark deal is in wallet
				deal.dealInWallet = true;
				deal.save();
			}
			else
			{
				if(deal == null)
				{
					serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), "No matching deal found.");
				}
				else if(userInfo == null)
				{
					serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), "No matching user found.");
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
		if(dealId != null)
		{
			parameters.put("dealId", Long.toString(dealId));
		}else
		{
			parameters.put("dealId", "null");
		}
		Request request = new Request(isValidRequest, "addWalletDeals", endTime - startTime, parameters);

		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));	
	}
	
	/**
	 * Removes a deal to a user's wallet.
	 * @param dealId
	 * @param userId
	 */
	public static void removeWalletDeals(@Required(message="dealId is required")Long dealId,
																			 @Required(message="userId is required")Long userId)
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
			//Delete wallet entry
			Wallet wallet = Wallet.find(WALLET_ENTRY_LOOKUP_HQL, userId, dealId).first();
			if(wallet != null)
			{
				//Mark deal is in wallet
				wallet.deal.dealInWallet = false;
				wallet.deal.save();
				//Remove wallet entry
				wallet.delete();
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
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), "No matching wallet entry found.");
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
		if(dealId != null)
		{
			parameters.put("dealId", Long.toString(dealId));
		}else
		{
			parameters.put("dealId", "null");
		}
		Request request = new Request(isValidRequest, "removeWalletDeals", endTime - startTime, parameters);

		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));	
	}
	
	/**
	 * Add alert time to a Wallet entry.
	 * @param walletId
	 * @param alertTime
	 */
	public static void updateAlertTime(@Required(message="walletId is required")Long walletId,
																		 @Required(message="alertTime is required")Date alertTime)
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
			//Delete wallet entry
			Wallet wallet = Wallet.find("id", walletId).first();
			if(wallet != null)
			{
				wallet.alertTime = alertTime;
				wallet.save();
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
				serviceResponse.addError(ErrorCodes.INVALID_REQUEST.toString(), "No matching wallet entry found.");
			}
		}
		Long endTime = System.currentTimeMillis();
		
		Map<String, String> parameters = new HashMap<String, String>();
		if(walletId != null)
		{
			parameters.put("walletId", Long.toString(walletId));
		}else
		{
			parameters.put("walletId", "null");
		}
		if(alertTime != null)
		{
			parameters.put("alertTime", alertTime.toString());
		}else
		{
			parameters.put("alertTime", "null");
		}
		Request request = new Request(isValidRequest, "updateAlertTime", endTime - startTime, parameters);

		serviceResponse.setRequest(request);
		if(isValidRequest && !response.isEmpty())
		{
			serviceResponse.setResponse(response);
		}
		renderJSON(new Message(serviceResponse));	
	}
}