package com.springsource.messaging.app;

/**
 * Service for processing messages
 */

public interface MessageService {

	/**
	 * 
	 * @param message the message being sent
	 * @return the reply message from the service
	 */
	public String processMessage(String message);	

}
