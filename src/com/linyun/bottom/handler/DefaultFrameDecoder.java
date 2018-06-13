/**
 * Juice
 * com.juice.orange.game.handler
 * DefaultStringHandler.java
 */
package com.linyun.bottom.handler;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import com.linyun.bottom.exception.JuiceException;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.util.InputMessage;
import com.linyun.bottom.util.JavaSerializeUtils;


/**
 * @author shaojieque 
 * 2013-3-22
 */
public class DefaultFrameDecoder extends FrameDecoder {
	private static Logger logger = LoggerFactory
			.getLogger(DefaultFrameDecoder.class);
	//
	public static final short PROTOCOL_DEFAULT_HEAD = 1000;
	public static final short PROTOCOL_DEFAULT_TAIL = 2000;
	public static final short PROTOCOL_OBJECT_HEAD = 1111;
	public static final short PROTOCOL_OBJECT_TAIL = 2222;
	//
	private static final int DEFAULT_HEAD_LENGTH = 2;
	private int headLength;

	public DefaultFrameDecoder() {
		this.headLength = DEFAULT_HEAD_LENGTH;
	}

	public DefaultFrameDecoder(int length) {
		this.headLength = length;
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buffer) throws Exception {
		try {
			if (!buffer.readable()) {
				return null;
			}
			//int length = buffer.readableBytes();
			int index = buffer.readerIndex();

			short head = buffer.getShort(index);
			if (head == PROTOCOL_DEFAULT_HEAD) {
				return decodeClientRequest(buffer, index);
			} else if (head == PROTOCOL_OBJECT_HEAD) {
				return decodeObjectRequest(buffer, index);
			} else {
				throw new JuiceException("message head value is error!" + channel.getRemoteAddress());
			}
		} catch (Exception e) {
			logger.error("Exception message : ",e);
		} 
		return null;
	}
	
	/**
	 * 解析客户端发送过来的字节流
	 * 协议格式：消息头(short=1000) + 消息长度(short) + 协议号(short) + 消息内容(byte[]) + 消息尾(short=2000)
	 */
	private Object decodeClientRequest(ChannelBuffer buffer, int index) {
		int len = buffer.getInt(index + 2);
		if (buffer.readableBytes() < len) {
			return null;
		}
		
		short protocolId = buffer.getShort(index + 6);
		//
		byte[] content = new byte[len - 6 - headLength - 2];
		buffer.getBytes(index + 6 + headLength, content);
		InputMessage msg = new InputMessage(content);
		
		short last = buffer.getShort(index + len - 2);
		if (last != 2000) {
			throw new JuiceException("decodeClientRequest message last value is error!");
		}
		//
		buffer.skipBytes(len);
		//ChannelBuffer frame = extractFrame(buffer, index + len, buffer.readableBytes());
		//buffer.
		//buffer.discardReadBytes();
		
		DefaultJuiceMessage message = new DefaultJuiceMessage(protocolId);
		message.setMsg(msg);
		//Channels.fireMessageReceived(channel, message);
		return message;
	}
	
	/**
	 * 解析客户端发送过来的字节流
	 * 协议格式：消息头(short=1111) + 消息长度(int) + 对象字节流(byte[])  + 消息尾(short=2222)
	 */
	private Object decodeObjectRequest(ChannelBuffer buffer, int index) {
		int len = buffer.getInt(index + 2);
		if (buffer.readableBytes() < len) {
			return null;
		}
		//
		ByteBuffer byteButter = ByteBuffer.allocate(len - 8);
		buffer.getBytes(index + 6, byteButter);
		
		short last = buffer.getShort(index + len - 2);
		if (last != 2222) {
			throw new JuiceException("decodeServerRequest message last value is error!");
		}

		buffer.skipBytes(len);
		//
		Object obj = JavaSerializeUtils.getInstance().deserialize(byteButter);
		return obj;
	}
	
	protected ChannelBuffer extractFrame(ChannelBuffer buffer, int index, int length) {
        ChannelBuffer frame = buffer.factory().getBuffer(length);
        frame.writeBytes(buffer, index, length);
        return frame;
    }
}
