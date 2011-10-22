package bl.utilities;

import java.math.BigInteger;
import java.security.SecureRandom;

public class UniqueTokenGenerator
{
	private SecureRandom random = new SecureRandom();

  public String nextSessionId()
  {
    return new BigInteger(130, random).toString(32);
  }
}
