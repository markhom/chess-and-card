/**
 * Juice
 * com.juice.orange.game.handler
 * IJuiceRequest.java
 */
package com.linyun.bottom.handler;

import java.net.SocketAddress;

import com.linyun.bottom.container.GameSession;


/**
 * @author shaojieque
 * 2013-3-21
 */
public interface IJuiceRequest {
	/**
	 * return url
	 */
	String uri();
	
	/**
	 * Remote address of connection (i.e. the host of the client).
	 */
	SocketAddress remoteAddress();
	
	 /**
     * A unique identifier for this request. This should be treated as an opaque object,
     * that can be used to track the lifecycle of a request.
     */
    Object id();

    /**
     * Timestamp (millis since epoch) of when this request was first received by the server.
     */
    long timestamp();
    
    /**
	 * get session
	 */
	GameSession getSession();
}
