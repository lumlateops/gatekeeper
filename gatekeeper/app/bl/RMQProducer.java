package bl;

import java.io.IOException;

import models.NewAccountMessage;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class RMQProducer
{
	private static final String RMQ_SERVER = "rmq01.deallr.com";
	private static final String	NEW_EMAIL_ACCOUNT_QUEUE	= "new_email_account";
	
	private static ConnectionFactory factory;
	private static Connection connection;
	private static Channel channel;
	
	public RMQProducer()
	{
		try
		{
			factory = new ConnectionFactory();
			factory.setHost(RMQ_SERVER);
			connection = factory.newConnection();
			channel = connection.createChannel();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void publishNewEmailAccountMessage(NewAccountMessage message)
	{
		publishMessage(message, NEW_EMAIL_ACCOUNT_QUEUE);
	}
	
	/**
	 * Posts messages to RMQ
	 * @param message
	 */
	private static void publishMessage(NewAccountMessage message, String queueName) 
	{
		try
		{
			channel.queueDeclare(NEW_EMAIL_ACCOUNT_QUEUE, true, false, false, null);
			channel.basicPublish("", NEW_EMAIL_ACCOUNT_QUEUE, MessageProperties.PERSISTENT_TEXT_PLAIN, message.toString().getBytes());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
