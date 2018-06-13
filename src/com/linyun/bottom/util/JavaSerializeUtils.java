/**
 * 
 */
package com.linyun.bottom.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.linyun.bottom.handler.DefaultFrameDecoder;


/**
 * @author queshaojie
 * 
 *         lewan
 */
public class JavaSerializeUtils {
	private static JavaSerializeUtils utils;

	private JavaSerializeUtils() {

	}

	public static JavaSerializeUtils getInstance() {
		if (utils == null) {
			utils = new JavaSerializeUtils();
		}
		return utils;
	}

	public ByteBuffer serialize(Object obj) {
		try {
			ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
			ObjectOutputStream stream = new ObjectOutputStream(byteArrayOS);
			stream.writeObject(obj);
			stream.close();
			return ByteBuffer.wrap(byteArrayOS.toByteArray());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}

	public Object deserialize(ByteBuffer buffer) {
		try {
			if (buffer == null || buffer.limit() == 0) {
				return null;
			}
			ByteArrayInputStream bin = new ByteArrayInputStream(buffer.array());
			ObjectInputStream inputStream = new ObjectInputStream(bin);
			Object object = inputStream.readObject();
			bin.close();
			return object;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}
	
	public ChannelBuffer getChannelBuffer(Object obj) {
		ByteBuffer objBuffer = serialize(obj);
		int length = objBuffer.array().length + 8;
		//
		ByteBuffer buffer = ByteBuffer.allocate(length);
		buffer.putShort(DefaultFrameDecoder.PROTOCOL_OBJECT_HEAD);
		buffer.putInt(length);
		buffer.put(objBuffer);
		buffer.putShort(DefaultFrameDecoder.PROTOCOL_OBJECT_TAIL);
		ChannelBuffer channelBuffer = ChannelBuffers.copiedBuffer(buffer.array());
		return channelBuffer;
	}
}
