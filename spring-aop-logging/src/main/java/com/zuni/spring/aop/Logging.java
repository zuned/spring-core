package com.zuni.spring.aop;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * 
 * @author Zuned
 *
 */
@Retention(RUNTIME)
@Target(METHOD)
@Component
public @ interface Logging {

	public static enum Level {  ERROR, WARN, INFO, DEBUG,TRACE } 
	Logging.Level level() default Logging.Level.INFO;
}
