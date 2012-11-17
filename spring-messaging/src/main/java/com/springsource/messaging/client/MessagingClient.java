package com.springsource.messaging.client;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;

/**
 * The client of the Message-Driven Bean. Will send a message on the message
 * queue and will display the response received on the response queue
 */
public class MessagingClient {

	public static void main(String args[]) {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] {
				"/infrastructure-context.xml", "/com/springsource/messaging/app/impl/mdb-context.xml",
				"/com/springsource/messaging/client/client-context.xml" });
		applicationContext.start();
		JmsTemplate jmsTemplate = (JmsTemplate) applicationContext.getBean("jmsTemplate");
		jmsTemplate.convertAndSend("Hello World");
		String response = (String) jmsTemplate.receiveAndConvert("messaging.responseQueue");
		System.out.println("Received response:\n" + response);
	}
}
