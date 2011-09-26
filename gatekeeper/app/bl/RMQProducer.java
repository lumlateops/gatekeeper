package bl;

import java.io.IOException;

import play.Logger;
import play.Play;
import play.modules.rabbitmq.producer.RabbitMQPublisher;

import models.NewAccountMessage;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class RMQProducer
{
	private static final String	RMQ_SERVER							= (String) Play.configuration.get("rabbitmq.host");
	private static final String	RMQ_USER								= (String) Play.configuration.get("rabbitmq.user");
	private static final String	RMQ_PASSWORD						= (String) Play.configuration.get("rabbitmq.password");
	private static final String	NEW_EMAIL_ACCOUNT_QUEUE	= (String) Play.configuration.get("rabbitmq.new.account.queue");
	
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
			factory.setUsername(RMQ_USER);
			factory.setPassword(RMQ_PASSWORD);
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
			Logger.error("Error posting to RMQ: " + e.toString() + e.getCause());
		}
	}
}
