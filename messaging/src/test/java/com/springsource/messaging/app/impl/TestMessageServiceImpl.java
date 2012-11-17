package com.springsource.messaging.app.impl;


import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.springsource.messaging.app.MessageService;

public class TestMessageServiceImpl {

	MessageService messageService;
	
	@Before
	public void setUp() throws Exception {
		messageService = new MessageServiceImpl();
	}

	@Test
	public void testMessageServiceSend() {
		String request = "Test message";
		String response = messageService.processMessage(request);
		Assert.assertEquals("Server response: " + request, response);
	}
	
	@After
	public void tearDown() throws Exception {
	}

}
