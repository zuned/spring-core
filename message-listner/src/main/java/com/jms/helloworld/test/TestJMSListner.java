package com.jms.helloworld.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestJMSListner {
 public static void main(String[] args) {
	 System.out.println("LISTNER STARTED");
	  @SuppressWarnings("unused")
	ApplicationContext context = new ClassPathXmlApplicationContext( new String[] { "classpath*:META-INF/spring/JMSConfig.xml" });
 }
}
