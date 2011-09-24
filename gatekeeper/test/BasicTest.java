import org.junit.*;

import java.util.*;

import play.test.*;
import models.*;

public class BasicTest extends UnitTest 
{
	@Before
	public void setup() {
	    Fixtures.deleteAll();
	}
	 
	@Test
	public void createAndRetrieveAccount() 
	{
		Date currentDate = new Date(System.currentTimeMillis());
		ServiceProvider provider = new ServiceProvider(Providers.GMAIL
				.toString(), "deallr.com", "f_yk4d2GkQljJ38JQrcRJBPr", "logourl" ,true,
				"http://gmail.com", AuthProtocols.OAUTH.toString(), 
				currentDate, 
				currentDate).save();
		
	  // Create a new user email account and save it
		new Account(Long.parseLong("23"), "bob@gmail.com", "password", null,
							  "token", "secret", true, true, null, currentDate, currentDate,
							  currentDate, currentDate, provider).save();
	    
	  Account account = Account.find("byEmail", "bob@gmail.com").first();
	    
	    // Test 
	    assertNotNull(account);
	    assertEquals("bob", account.userId);
	}
	
	@Test
	public void createAndRetrieveProvider()
	{
		new ServiceProvider(Providers.GMAIL.toString(), "deallr.com",
				"f_yk4d2GkQljJ38JQrcRJBPr", "logourl", true, 
							"http://gmail.com", AuthProtocols.OAUTH.toString(), 
							new Date(System.currentTimeMillis()), 
							new Date(System.currentTimeMillis())).save();
		
		ServiceProvider provider = ServiceProvider.find("byName", Providers.GMAIL.toString()).first();
		
		assertNotNull(provider);
		assertEquals("http://gmail.com", provider.website);
	}
	
	@Test
	public void fullTest() 
	{
	    Fixtures.loadModels("data.yml");
	 
	    // Count things
	    assertEquals(1, Account.count());
	    assertEquals(6, ServiceProvider.count());
	    
	    List<ServiceProvider> providers = ServiceProvider.findAll();
	    assertEquals(6, providers.size());
	    for (ServiceProvider sp : providers)
			{
				assertNotNull(sp);
				assertNotNull(sp.name);
				assertNotNull(sp.consumerKey);
				assertNotNull(sp.consumerSecret);
				assertNotNull(sp.protocol);
				assertNotNull(sp.website);
				assertNotNull(sp.active);
				assertNotNull(sp.created_at);
				assertNotNull(sp.updated_at);
			}
	    
	    List<Account> accounts = Account.find("email", "lumlateops@gmail.com").fetch();
	    assertEquals(1, accounts.size());
	    
	    Fixtures.deleteAll();
	}
}
