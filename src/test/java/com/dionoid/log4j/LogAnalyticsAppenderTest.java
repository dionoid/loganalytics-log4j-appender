package com.dionoid.log4j;

import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;

public class LogAnalyticsAppenderTest {

	final static Logger LOG = Logger.getLogger(LogAnalyticsAppenderTest.class);
	
	@Test
	public void BasicTest() throws InterruptedException {
		LOG.info("Hello world!");
		
		CloseAllAppenders();
	}
	
	private void CloseAllAppenders() {
		for (Enumeration<?> loggers = LogManager.getCurrentLoggers(); loggers.hasMoreElements();)  {
			for (Enumeration<?> appenders = ((Logger)loggers.nextElement()).getAllAppenders(); appenders.hasMoreElements();)  {
		        ((Appender)appenders.nextElement()).close();
			}
		}
	}
}
