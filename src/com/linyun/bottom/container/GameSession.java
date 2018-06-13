/**
 * Juice
 * com.juice.orange.game.container
 * GameSession.java
 */
package com.linyun.bottom.container;

import com.linyun.bottom.util.OutputMessage;

/**
 * @author shaojieque 2013-3-22
 */
public interface GameSession {
	public static final int OPEN = 1;
	public static final int CLOSE = 2;
	public static final int STATUS_CONN = 1;
	public static final int STATUS_UNCONN = -1;

	/**
	 * get sessionId
	 */
	String getSessionId();
	
	/**
	 * send a message
	 */
	void sendMessage(short protocolId, OutputMessage msg);

	/**
	 * restore a object
	 */
	void put(String key, Object value);

	/**
	 * get object
	 */
	Object getObject(String key);
	
	/**
	 * remove object
	 */
	void remove(String key);
	
	/**
	 * setting status value
	 */
	void setStatus(int status);
	
	/**
	 * get status value
	 */
	int getStatus();
}
