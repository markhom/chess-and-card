/**
 * Juice
 * com.juice.orange.game.container
 * GameSessionImpl.java
 */
package com.linyun.bottom.container;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import com.linyun.bottom.exception.JuiceException;
import com.linyun.bottom.handler.DefaultFrameDecoder;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.util.DateUtils;
import com.linyun.bottom.util.OutputMessage;


/**
 * @author shaojieque 2013-3-22
 */
public class GameSessionImpl implements GameSession {
	private static Logger logger = LoggerFactory.getLogger(GameSessionImpl.class);
	private Map<String, Object> objectMap;
	private final ChannelHandlerContext ctx;

	private String sessionId;
	private int status;

	public GameSessionImpl(ChannelHandlerContext ctx) {
		autoGenetateId();
		this.ctx = ctx;
		this.objectMap = new HashMap<String, Object>();
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}
	
	@Override
	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public int getStatus() {
		return status;
	}
	
	@Override
	public void sendMessage(short protocolId, OutputMessage msg) {
		try {
			int length = 10 + msg.length();
			ByteBuffer byteBuffer = ByteBuffer.allocate(length);
			byteBuffer.putShort(DefaultFrameDecoder.PROTOCOL_DEFAULT_HEAD);
			byteBuffer.putInt(length);
			byteBuffer.putShort(protocolId);
			byteBuffer.put(msg.getBytes());
			byteBuffer.putShort(DefaultFrameDecoder.PROTOCOL_DEFAULT_TAIL);
			ChannelBuffer buffer = ChannelBuffers.copiedBuffer(byteBuffer.array());
			sendMessage(buffer);
			byteBuffer.clear();
		} catch (Exception e) {
			status = GameSession.STATUS_UNCONN;
			e.printStackTrace();
		} 
	}
	
	private void sendMessage(ChannelBuffer buffer) {
		try {
			checkChannel();
			//
			Object att=ctx.getAttachment();
			if(att instanceof String)
			{ String atstr=(String)att;
			if(atstr.equals("WebSocket"))
			{
				BinaryWebSocketFrame b=new BinaryWebSocketFrame();
				b.setBinaryData(buffer);
				ctx.getChannel().write(b);
				return;
				
			}
				
			}
			ctx.getChannel().write(buffer);
		} catch (Exception e) {
			logger.error(e.getMessage(), e.getCause());
		}
	}
	
	private void checkChannel() throws JuiceException {
		if (ctx.getChannel() == null) {
			throw new JuiceException("channel is null, can't send message!");
		}
		
		if (!ctx.getChannel().isConnected()) {
			 ChannelFuture future = ctx.getChannel().close();
			 logger.error("channel is disconnected, so close channel future=" + future.isSuccess());
			 throw new JuiceException("can't connect by channel!");
		}
		
		if (!ctx.getChannel().isWritable()) {
			throw new JuiceException("can't write message by channel!");
		}
	}

	private void autoGenetateId() {
		sessionId = DateUtils.generateId();
	}

	@Override
	public void put(String key, Object value) {
		objectMap.put(key, value);
	}

	@Override
	public Object getObject(String key) {
		return objectMap.get(key);
	}

	@Override
	public void remove(String key) {
		objectMap.remove(key);
	}
}
