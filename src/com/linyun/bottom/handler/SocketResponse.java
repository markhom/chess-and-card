/**
 * Juice
 * com.juice.orange.game.handler
 * SocketResponse.java
 */
package com.linyun.bottom.handler;

import com.linyun.bottom.util.OutputMessage;

/**
 * @author shaojieque
 * 2013-4-10
 */
public interface SocketResponse extends IJuiceResponse{
	void sendMessage(short protocolId, OutputMessage msg);
}
