/**
 * Juice
 * com.juice.orange.game.handler
 * JuiceRequest.java
 */
package com.linyun.bottom.handler;

import java.net.SocketAddress;

import com.linyun.bottom.container.GameSession;
import com.linyun.bottom.util.InputMessage;


/**
 * @author shaojieque 
 * 2013-3-22
 */
public class DefaultJuiceRequest implements SocketRequest {
	private GameSession session;
	private String path;
	private InputMessage paramMsg;
	private Object id;
	private long timestamp;
	private String uri;
	private SocketAddress socketAddress;

	public DefaultJuiceRequest(SocketAddress socketAddress, GameSession session, String path,
			InputMessage paramMsg, Object id, long timestamp) {
		this.socketAddress = socketAddress;
		this.session = session;
		this.path = path;
		this.paramMsg = paramMsg;
		this.id = id;
		this.timestamp = timestamp;
	}

	@Override
	public String uri() {
		return uri;
	}

	@Override
	public SocketAddress remoteAddress() {
		return socketAddress;
	}

	@Override
	public Object id() {
		return id;
	}

	@Override
	public long timestamp() {
		return timestamp;
	}

	@Override
	public String getPath() {
		return path;
	}

	public GameSession getSession() {
		return session;
	}

	@Override
	public InputMessage getInputMessage() {
		return paramMsg;
	}
}
