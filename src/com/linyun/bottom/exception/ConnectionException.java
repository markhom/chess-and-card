/**
 * Juice
 * com.juice.orange.game.exception
 * ConnectionException.java
 */
package com.linyun.bottom.exception;

import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.log4j.Logger;

import com.linyun.bottom.log.LoggerFactory;


/**
 * @author shaojieque
 * 2013-3-19
 */
public class ConnectionException implements UncaughtExceptionHandler {
	private static Logger logger = LoggerFactory.getLogger(ConnectionException.class);
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		logger.error("uncaughtException :",e);
	}
}
