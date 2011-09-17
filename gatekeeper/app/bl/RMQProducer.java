package bl;

import java.io.IOException;

import play.Logger;

import models.NewAccountMessage;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class RMQProducer
{
	private static final String RMQ_SERVER = "rmq01.deallr.com";
	private static final String	NEW_EMAIL_ACCOUNT_QUEUE	= "new_email_account";
	
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
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(RMQ_SERVER);
			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
			if(channel != null)
			{
				channel.queueDeclare(queueName, true, false, false, null);
				Logger.debug("Posting the following to queue: " + message.toString());
				channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, message.toString().getBytes());
			}
			else
			{
				Logger.info("RMQ Channel closed");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Logger.debug("Error posting to RMQ: " + e.toString() + e.getCause());
		}
	}
}
