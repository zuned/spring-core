package com.springsource.messaging.app.impl;

import com.springsource.messaging.app.MessageService;

/**
 * An implementation for a message service, giving a default response to the sent message.
 */
public class MessageServiceImpl implements MessageService{

	public String processMessage(String message) {
		System.out.println("Received message: " + message);
		return "Server response: " + message;
	}
	
}
