/**
 * Juice
 * com.juice.orange.game.util
 * OutputMessage.java
 */
package com.linyun.bottom.util;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shaojieque 
 * 2013-5-8
 */
public class OutputMessage implements Serializable 
{
	
	private static final long serialVersionUID = 6466028171419915327L;
	
	private List<Byte> byteList;
	private ByteBuffer buffer;

	public OutputMessage() {
		byteList = new ArrayList<Byte>();
	}
	
	public OutputMessage(boolean isOK) {
		byteList = new ArrayList<Byte>();
		if (isOK)
		{
			putByte((byte)0);
		}
		else
		{
			putByte((byte)1);
		}
	}
	
//	@Deprecated
//	public OutputMessage(int size) {
//		this.buffer = ByteBuffer.allocate(size);
//	}

	public void putShort(short value) {
		if (buffer!=null) {
			buffer.putShort(value);
		} else if (byteList!=null){
			byte _short1 = (byte)(value >> 8);
			byte _short0 = (byte)(value >> 0);
			byteList.add(_short1);
			byteList.add(_short0);
		}
	}

	public void putString(String value) {
		try {
			if (value==null) {
				short length_null = 0;
				if (buffer!=null) {
					buffer.putShort(length_null);
				} else if (byteList!=null){
					putShort(length_null);
				}
				return;
			}
			byte[] content = value.getBytes("utf-8");
			short length = (short) content.length;
			//
			if (buffer!=null) {
				buffer.putShort(length);
				buffer.put(content);
			} else if (byteList!=null){
				putShort(length);
				for(byte _content : content) {
					byteList.add(_content);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void putBoolean(boolean value) {
		byte _value = (byte) (value ? 1 : 0);
		if (buffer!=null) {
			buffer.put(_value);
		} else if(byteList!=null){
			byteList.add(_value);
		}
	}
	
	public void putByte(byte... value) {
		if (buffer!=null) {
			buffer.put(value);
		} else if(byteList!=null){
			for(Byte _value : value) {
				byteList.add(_value);
			}
		}
	}

	public void putInt(int value) {
		if (buffer!=null) {
			buffer.putInt(value);
		} else if(byteList!=null){
			byte _int3= (byte)(value >> 24); 
			byte _int2= (byte)(value >> 16); 
			byte _int1= (byte)(value >>  8); 
			byte _int0= (byte)(value >> 0); 
			byteList.add(_int3);
			byteList.add(_int2);
			byteList.add(_int1);
			byteList.add(_int0);
		}
	}
	
	public byte[] getBytes(){
		if(byteList!=null){
			int size = byteList.size();
			byte[] _bytes = new byte[size];
			for (int i=0;i<size;i++){
				_bytes[i] = byteList.get(i);
			}
			return _bytes;
		}
		return buffer.array();
	}
	
	public int length(){
		if(buffer!=null){
			return buffer.array().length;
		} else if(byteList!=null){
			return byteList.size();
		}
		return 0;
	}
}
