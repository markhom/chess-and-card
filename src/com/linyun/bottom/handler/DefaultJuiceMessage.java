/**
 * Juice
 * com.juice.orange.game.handler
 * DefaultJuiceMessage.java
 */
package com.linyun.bottom.handler;

import com.linyun.bottom.util.InputMessage;
/**
 * @author shaojieque 
 * 2013-3-22
 */
public class DefaultJuiceMessage {
	private short protocolId;
	private InputMessage msg;
	
	public DefaultJuiceMessage(short protocolId) {
		this.protocolId = protocolId;
	}

	public InputMessage getMsg() {
		return msg;
	}

	public void setMsg(InputMessage msg) {
		this.msg = msg;
	}

	public short getProtocolId() {
		return protocolId;
	}

	public void setProtocolId(short protocolId) {
		this.protocolId = protocolId;
	}
}
