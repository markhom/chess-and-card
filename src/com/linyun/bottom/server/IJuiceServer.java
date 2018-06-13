/**
 * Juice
 * com.juice.orange.game.server
 * IJuiceServer.java
 */
package com.linyun.bottom.server;

import com.linyun.bottom.handler.IJuiceHandler;

/**
 * @author shaojieque
 * 2013-3-19
 */
public interface IJuiceServer extends Endpoint<IJuiceServer>{
	/**
	 * add a default handler
	 */
	IJuiceServer add(IJuiceHandler handler);
	
	/**
	 * add a pathMatchHandler that handler the request by path
	 */
	IJuiceServer add(String path, IJuiceHandler handler);
	
	/**
	 * return the server port
	 */
    int getPort();
    
   /**
    * Connection exception
    */
    IJuiceServer connectionExceptionHandler(Thread.UncaughtExceptionHandler ioExceptionHandler);
    
    void setTransport(String transport);
    
    /**
     * init thread pool number 
     */
    void setThreadPool(int num);
}
