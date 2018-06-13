/**
 * Juice
 * com.juice.orange.game.handler
 * SocketRequest.java
 */
package com.linyun.bottom.handler;

import com.linyun.bottom.util.InputMessage;

/**
 * @author shaojieque
 * 2013-4-10
 */
public interface SocketRequest extends IJuiceRequest{
	/**
	 * get request path that help to find handler 
	 */
	String getPath();
	
	/**
	 * get param message
	 */
	InputMessage getInputMessage();
}
