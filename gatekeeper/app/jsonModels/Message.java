package jsonModels;

public class Message
{
	boolean isSuccess;
	String message;
	
	public Message(boolean isSuccess, String message)
	{
		this.isSuccess = isSuccess;
		this.message = message;
	}
}
