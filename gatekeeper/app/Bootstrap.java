import play.*;
import play.jobs.*;
import play.test.*;

import models.*;

@OnApplicationStart
public class Bootstrap extends Job 
{
	public void doJob() 
	{
		// Check if the database is empty
		if (ServiceProvider.count() == 0 || EmailCategory.count() == 0) 
		{
			Fixtures.loadModels("initial-data.yml");
		}
	}
}