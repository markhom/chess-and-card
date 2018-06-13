/**
 * Juice
 * com.juice.orange.game.handler
 * DefaultJuiceResponse.java
 */
package com.linyun.bottom.handler;

import com.linyun.bottom.container.GameSession;
import com.linyun.bottom.util.OutputMessage;

/**
 * @author shaojieque 2013-3-22
 */
public class DefaultJuiceResponse implements SocketResponse {
	private GameSession session;

	public DefaultJuiceResponse(GameSession session) {
		this.session = session;
	}

	@Override
	public void sendMessage(short protocolId, OutputMessage msg) {
		session.sendMessage(protocolId, msg);
	}
}
