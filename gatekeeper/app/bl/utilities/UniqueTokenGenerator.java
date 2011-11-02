package bl.utilities;

import java.math.BigInteger;
import java.security.SecureRandom;

public class UniqueTokenGenerator
{
	private SecureRandom random = new SecureRandom();

  public String nextToken()
  {
    return new BigInteger(30, random).toString(32);
  }
  
  public static void main(String[] args)
	{
  	UniqueTokenGenerator tgen = new UniqueTokenGenerator();
  	for(int ctr=0; ctr<150; ctr++)
  	{
  		System.out.println(tgen.nextToken());
  	}
	}
}
