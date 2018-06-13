/**
 * Juice
 * com.juice.orange.game.handler
 * JuiceChannelHandler.java
 */
package com.linyun.bottom.handler;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import com.linyun.bottom.container.Container;
import com.linyun.bottom.container.GameSession;
import com.linyun.bottom.container.GameSessionImpl;
import com.linyun.bottom.exception.JuiceException;
import com.linyun.bottom.notify.NotifyServerControl;
import com.linyun.bottom.rmi.Transport;
import com.linyun.bottom.rmi.control.RPCServerControl;
import com.linyun.bottom.util.ConnectionHelper;
import com.linyun.bottom.util.InputMessage;
import com.linyun.bottom.util.threadpool.JuicePoolManager;


/**
 * @author shaojieque 2013-3-20
 */
public class JuiceChannelHandler extends SimpleChannelHandler
{
	// private static Logger logger =
	// LoggerFactory.getLogger(JuiceChannelHandler.class);
	private static final Object IGNORE_REQUEST = new Object();

	private final Executor executor;
	private final List<IJuiceHandler> handlers;
	private final Object id;
	private final long timestamp;
	private final Thread.UncaughtExceptionHandler exceptionHandler;
	private final Thread.UncaughtExceptionHandler ioExceptionHandler;
	private final ConnectionHelper connectionHelper;

	public JuiceChannelHandler(Executor executor, List<IJuiceHandler> handlers,
			Object id, long timestamp,
			Thread.UncaughtExceptionHandler exceptionHandler,
			Thread.UncaughtExceptionHandler ioExceptionHandler)
	{
		this.executor = executor;
		this.handlers = handlers;
		this.id = id;
		this.timestamp = timestamp;
		this.exceptionHandler = exceptionHandler;
		this.ioExceptionHandler = ioExceptionHandler;
		//
		connectionHelper = new ConnectionHelper(executor, exceptionHandler,
				ioExceptionHandler)
		{
			@Override
			protected void fireOnClose() throws Exception
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception
	{
		GameSession session = null;
		String sessionId = Container.getChannelSession(ctx.getChannel());
		if (sessionId == null)
		{
			session = new GameSessionImpl(ctx);
			Container.addSession(session);
			Container.addChannelSession(ctx.getChannel(), session.getSessionId());
			Container.addSessionChannel(session.getSessionId(), ctx.getChannel());
		} else
		{
			session = Container.getSessionById(sessionId);
		}
		//
		if (session != null)
		{
			session.setStatus(GameSession.STATUS_CONN);
		}
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception
	{
		clearGameSession(ctx);
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception
	{
		clearGameSession(ctx);
	}

	private void clearGameSession(ChannelHandlerContext ctx)
	{
		GameSession session = null;
		String sessionId = Container.getChannelSession(ctx.getChannel());
		if (sessionId != null)
		{
			session = Container.getSessionById(sessionId);
			if (session != null)
			{
				session.setStatus(GameSession.STATUS_UNCONN);
				Container.notifyListener(session);
				Container.removeSession(session.getSessionId());
			}
			Container.removeSessionChannel(sessionId);
		}
		//
		Container.removeChannelSession(ctx.getChannel());
		StaleConnectionTrackingHandler.stopTracking(ctx.getChannel());
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception
	{
		GameSession session = null;
		String sessionId = Container.getChannelSession(ctx.getChannel());
		if (sessionId != null)
		{
			session = Container.getSessionById(sessionId);
		}
		//
		if (e.getMessage() instanceof DefaultJuiceMessage)
		{
			DefaultJuiceMessage messgae = (DefaultJuiceMessage) e.getMessage();
			handleDefaultRequest(ctx, e, session, messgae);
		} else if (e.getMessage() instanceof HttpRequest
				&& ctx.getAttachment() != IGNORE_REQUEST)
		{
			handleHttpRequest(ctx, e, session, (HttpRequest) e.getMessage());
		} else if (e.getMessage() instanceof Transport)
		{
			Transport t = (Transport) e.getMessage();
			handleRPCRequest(ctx, t);
		} else if (e.getMessage() instanceof WebSocketFrame)
		{
			ctx.setAttachment("WebSocket");
			WebSocketFrame messgae = (WebSocketFrame) e.getMessage();
			handleWebsocketFrame(ctx, e, session, messgae);
		} else if (e.getMessage() instanceof Object)
		{
			Object obj = (Object) e.getMessage();
			handleNotifyRequest(ctx, obj);
		}
	}

	/**
	 * 解析客户端发送过来的字节流 协议格式：消息头(short=1000) + 消息长度(int) + 协议号(short) +
	 * 消息内容(byte[]) + 消息尾(short=2000)
	 */
	private Object decodeClientWebSocketRequest(ChannelBuffer buffer, int index)
	{
		int len = buffer.getInt(index + 2);
		if (buffer.readableBytes() < len)
		{
			return null;
		}
		int headLength = 2;
		short protocolId = buffer.getShort(index + 6);
		//
		byte[] content = new byte[len - 6 - headLength - 2];
		buffer.getBytes(index + 6 + headLength, content);
		InputMessage msg = new InputMessage(content);

		short last = buffer.getShort(index + len - 2);
		if (last != 2000)
		{
			throw new JuiceException(
					"decodeClientRequest message last value is error!");
		}
		//
		buffer.skipBytes(len);
		// ChannelBuffer frame = extractFrame(buffer, index + len,
		// buffer.readableBytes());
		// buffer.
		// buffer.discardReadBytes();

		DefaultJuiceMessage message = new DefaultJuiceMessage(protocolId);
		message.setMsg(msg);
		// Channels.fireMessageReceived(channel, message);
		return message;
	}

	private void handleWebsocketFrame(final ChannelHandlerContext ctx,
			MessageEvent messageEvent, GameSession session,
			WebSocketFrame request)
	{
		if (request instanceof CloseWebSocketFrame)
		{
			ctx.getChannel().close();
		} else if (request instanceof PingWebSocketFrame)
		{
			ctx.getChannel().write(new PongWebSocketFrame(request.getBinaryData()));		
		} else if (request instanceof TextWebSocketFrame)
		{
			ctx.getChannel()
					.write(new TextWebSocketFrame("unsupported Text mssage!"));
		} else if (request instanceof BinaryWebSocketFrame)
		{
			BinaryWebSocketFrame bframe = (BinaryWebSocketFrame) request;
			ChannelBuffer buf = bframe.getBinaryData();
			DefaultJuiceMessage ms = (DefaultJuiceMessage) decodeClientWebSocketRequest(
					buf, buf.readerIndex());
			handleDefaultRequest(ctx, messageEvent, session, ms);
		} else
		{
			ctx.getChannel()
					.write(new TextWebSocketFrame("unknown frame format!"));
		}

	}

	private void handleDefaultRequest(final ChannelHandlerContext ctx,
			MessageEvent messageEvent, GameSession session,
			DefaultJuiceMessage defaultRequest)
	{
		final DefaultJuiceRequest request = new DefaultJuiceRequest(
				ctx.getChannel().getRemoteAddress(), session,
				String.valueOf(defaultRequest.getProtocolId()),
				defaultRequest.getMsg(), id, timestamp);
		final DefaultJuiceResponse response = new DefaultJuiceResponse(session);
		Iterator<IJuiceHandler> handlerIterator = this.handlers.iterator();
		final DefaultJuiceControl control = new DefaultJuiceControl(
				handlerIterator, executor, request, response, defaultRequest, ctx.getChannel().getId());

		JuicePoolManager.getInstance().addRequest(control);
	}

	private boolean iswebsocketReq(ChannelHandlerContext channel,
			HttpRequest request)
	{
		boolean b = HttpMethod.GET.equals(request.getMethod()) && "websocket"
				.equalsIgnoreCase(HttpHeaders.getHeader(request, "Upgrade"));

		return b;
	}

	private void handleHttpRequest(final ChannelHandlerContext ctx,
			MessageEvent messageEvent, GameSession session,
			HttpRequest httpRequest)
	{
		if (iswebsocketReq(ctx, httpRequest))
		{
			WebSocketServerHandshakerFactory wsShakerFactory = new WebSocketServerHandshakerFactory(
					"ws://" + HttpHeaders.getHeader(httpRequest,
							HttpHeaders.Names.HOST),
					"default-protocol", false);
			WebSocketServerHandshaker wsShakerHandler = wsShakerFactory
					.newHandshaker(httpRequest);
			if (null == wsShakerHandler)
			{
				// 无法处理的websocket版本
				wsShakerFactory.sendUnsupportedWebSocketVersionResponse(
						ctx.getChannel());
			} else
			{
				// 向客户端发送websocket握手,完成握手
				// 客户端收到的状态是101 sitching protocol
				wsShakerHandler.handshake(ctx.getChannel(), httpRequest);
			}
		} else
		{
			final JuiceHttpRequest request = new JuiceHttpRequest(messageEvent,
					httpRequest, session, id, timestamp);
			DefaultHttpResponse ok_200 = new DefaultHttpResponse(
					HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
			final JuiceHttpResponse response = new JuiceHttpResponse(ctx,
					ok_200, HttpHeaders.isKeepAlive(httpRequest),
					exceptionHandler);
			Iterator<IJuiceHandler> handlerIterator = this.handlers.iterator();
			final HttpControl control = new JuiceHttpControl(handlerIterator,
					executor, ctx, request, response, httpRequest, ok_200,
					exceptionHandler, ioExceptionHandler);
			JuicePoolManager.getInstance().addRequest(control);
		}
	}

	private void handleRPCRequest(final ChannelHandlerContext ctx, Transport t)
	{
		RPCServerControl control = new RPCServerControl(executor, ctx, t);
		JuicePoolManager.getInstance().addRequest(control);
	}

	private void handleNotifyRequest(final ChannelHandlerContext ctx,
			Object obj)
	{
		NotifyServerControl control = new NotifyServerControl(executor, ctx,
				obj);
		JuicePoolManager.getInstance().addRequest(control);
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx,
			final ExceptionEvent e) throws Exception
	{
		connectionHelper.fireConnectionException(e);
		exceptionHandler.uncaughtException(Thread.currentThread(),
				JuiceException.fromException(e.getCause(), ctx.getChannel()));
	}
}
