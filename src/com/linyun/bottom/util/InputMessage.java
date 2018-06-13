/**
 * Juice
 * com.juice.orange.game.util
 * InputMessage.java
 */
package com.linyun.bottom.util;

import java.io.UnsupportedEncodingException;

/**
 * @author shaojieque 
 * 2013-5-8
 */
public class InputMessage {

	private byte[] msgData;
	private int inc = 0;

	/**
	 * 封包、解析和协议
	 * 
	 * @param data
	 */
	public InputMessage(byte[] bytes) {
		this.msgData = bytes;
		setInc(0);
	}

	public void setMsgData(byte[] msgData) {
		this.msgData = msgData;
		setInc(0);
	}

	protected int getInc() {
		return inc;
	}

	public void setInc(int inc) {
		this.inc = inc;
	}

	public int getInt() {
		return ((msgData[inc++] & 0xFF) << 24)
				+ ((msgData[inc++] & 0xFF) << 16)
				+ ((msgData[inc++] & 0xFF) << 8) + (msgData[inc++] & 0xFF);
	}

	public short getShort() {
		return (short) (((msgData[inc++] & 0xFF) << 8) + (msgData[inc++] & 0xFF));
	}

	public byte getByte() {
		return msgData[inc++];
	}
	
	public boolean getBoolean() {
		return msgData[inc++] != 0;
	}

	public String getUTF() {
		short len = getShort();
		String str = null;
		try {
			str = new String(msgData, inc, len, "utf-8"); //
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		inc += len;
		return str;
	}
}
