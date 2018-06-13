/**
 * Juice
 * com.juice.orange.game.server
 * Endpoint.java
 */
package com.linyun.bottom.server;

import java.net.URI;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * @author shaojieque
 * 2013-3-20
 */
public interface Endpoint<T> {
	Future<? extends T> start();
	
	Future<? extends T> stop();
	
	//What to do when an exception gets thrown in a handler.
	T uncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler);
	
	 /**
     * What to do when an exception occurs when attempting to read/write data
     * from/to the underlying connection. e.g. If an HTTP request disconnects
     * before it was expected.
     * <p/>
     * Defaults to using {@link org.webbitserver.handler.exceptions.SilentExceptionHandler}
     * as this is a common thing to happen on a network, and most systems should not care.
     */
	T connectionExceptionHandler(Thread.UncaughtExceptionHandler handler);
	
	/**
     * Get main work executor that all handlers will execute on.
     */
	Executor getExecutor();
	
	URI getURI();
}
