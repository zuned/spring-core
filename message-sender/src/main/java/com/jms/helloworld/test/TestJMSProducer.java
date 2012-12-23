package com.jms.helloworld.test;

import javax.jms.JMSException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jms.helloworld.producer.MessageProducer;

public class TestJMSProducer {
	public static void main(String[] args) throws JMSException {

		ApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "/spring/JMSConfig.xml" });
		MessageProducer myBean = (MessageProducer) context.getBean("simpleMessageProducer");
		myBean.sendMessages();
	}
}
