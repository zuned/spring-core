package com.jms.helloworld.listner;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class MessageListenerImpl implements MessageListener {
	public void onMessage(Message message) {
		System.out.println("RECEIVER");
		TextMessage msg = (TextMessage) message;
		try {
			System.out.println("MESSAGE: " + msg.getText());
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
