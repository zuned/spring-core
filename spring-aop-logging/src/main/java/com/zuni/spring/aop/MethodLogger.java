package com.zuni.spring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;


/**
 * 
 * @author Zuned
 *
 */

@Aspect
@Component
public class MethodLogger {
	
	@Around("execution(* com.hcentive..*.*(..)) && @annotation(loggLevel)")
	public Object loggingAdvice(ProceedingJoinPoint joinPoint , Logging loggLevel) throws Throwable {
		Logger logger = LoggerFactory.getLogger(this.getClass());
		if( !isLoggingEnabled(logger ,loggLevel ) )
		{
			return joinPoint.proceed();
		}
		
			StopWatch stopWatch = new StopWatch();
		logMessage(logger , prepareStarMessage(joinPoint) , loggLevel);
			stopWatch.start();
		Object retVal = joinPoint.proceed();
			stopWatch.stop();
		logMessage(logger , prepareEndMessage(joinPoint ,stopWatch.getTotalTimeMillis()) , loggLevel);

		return retVal;
	}

	private boolean isLoggingEnabled(Logger logger, Logging loggLevel) {
		Logging.Level level = loggLevel.level();
		switch (level) {
			case ERROR:
				return logger.isErrorEnabled();
			case WARN:
				return logger.isWarnEnabled();
			case INFO:
				return logger.isInfoEnabled();
			case DEBUG:
				return logger.isDebugEnabled();
			case TRACE:
				return logger.isTraceEnabled();
		}
		return false;
	}

	private String prepareEndMessage(ProceedingJoinPoint joinPoint, long totalTimeMillis) {
		StringBuilder logMessageStringBuffer = new StringBuilder();
		logMessageStringBuffer.append("Leaving from execution time: ");
		logMessageStringBuffer.append(totalTimeMillis);
		logMessageStringBuffer.append(" ms");
		return logMessageStringBuffer.toString();
	}

	private String prepareStarMessage(ProceedingJoinPoint joinPoint) {
		StringBuilder logMessageStringBuffer = new StringBuilder();
		logMessageStringBuffer.append("Entering into : ");
		logMessageStringBuffer.append(joinPoint.getTarget().getClass().getName());
		logMessageStringBuffer.append(".");
		logMessageStringBuffer.append(joinPoint.getSignature().getName());
		logMessageStringBuffer.append("(");
		logMessageStringBuffer.append(joinPoint.getArgs());
		logMessageStringBuffer.append(")");
		return logMessageStringBuffer.toString();
	}

	private void logMessage(Logger logger, String messgae, Logging loggLevel) {
		Logging.Level level = loggLevel.level();
		switch (level) {
			case ERROR:
				logger.error(messgae);
				break;
			case WARN:
				logger.warn(messgae);
				break;
			case INFO:
				logger.info(messgae);
				break;
			case DEBUG:
				logger.debug(messgae);
				break;
			case TRACE:
				logger.trace(messgae);
				break;
		}
	}
}
