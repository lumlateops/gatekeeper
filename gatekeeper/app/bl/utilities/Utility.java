package bl.utilities;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import play.Logger;
import play.Play;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Utility
{
	private static final String	CIPHER		= (String)Play.configuration.get("application.cipher");
	private static final char[]	PASSWORD	= ((String)Play.configuration.get("application.secret")).toCharArray();
	private static final byte[]	SALT			= ((String)Play.configuration.get("application.key")).getBytes();

//	public static void main(String[] args) throws Exception
//	{
//		String originalPassword = "secret";
//		System.out.println("Original password: " + originalPassword);
//		String encryptedPassword = encrypt(originalPassword);
//		System.out.println("Encrypted password: " + encryptedPassword);
//		String decryptedPassword = decrypt(encryptedPassword);
//		System.out.println("Decrypted password: " + decryptedPassword);
//	}

	public static String encrypt(String property) throws GeneralSecurityException
	{
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(CIPHER);
		SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
		Cipher pbeCipher = Cipher.getInstance(CIPHER);
		pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
		return base64Encode(pbeCipher.doFinal(property.getBytes()));
	}

	public static String decrypt(String property) throws GeneralSecurityException, IOException
	{
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(CIPHER);
		SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
		Cipher pbeCipher = Cipher.getInstance(CIPHER);
		pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
		return new String(pbeCipher.doFinal(base64Decode(property)));
	}
	
	public static void logRequest()
	{
		Logger.info("-----------------BEGIN REQUEST INFO-----------------");
		play.mvc.Http.Request currentRequest = play.mvc.Http.Request.current();
		Logger.info("Request end point: " + currentRequest.action);
		Map<String, String[]> requestParams = currentRequest.params.all();
		for (String key : requestParams.keySet())
		{
			if(!key.equalsIgnoreCase("body"))
			{
				String value = requestParams.get(key)[0];
				if(key.equalsIgnoreCase("password"))
				{
					value = "******";
				}
				Logger.info(key + ": '"+ value + "'");
			}
		}
	}
	
	public static void logResponse()
	{
		play.mvc.Http.Response currentResponse = play.mvc.Http.Response.current();
		Logger.info("Response status: " + currentResponse.status);
	}
	
	private static String base64Encode(byte[] bytes)
	{
		// NB: This class is internal, and you probably should use another impl
		return new BASE64Encoder().encode(bytes);
	}

	private static byte[] base64Decode(String property) throws IOException
	{
		// NB: This class is internal, and you probably should use another impl
		return new BASE64Decoder().decodeBuffer(property);
	}
}
