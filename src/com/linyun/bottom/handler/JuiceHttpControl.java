/**
 * Juice
 * com.juice.orange.game.handler
 * JuiceHttpControl.java
 */
package com.linyun.bottom.handler;

import java.util.Iterator;
import java.util.concurrent.Executor;

import org.jboss.netty.channel.ChannelHandlerContext;

/**
 * @author shaojieque
 * 2013-3-20
 */
@SuppressWarnings("unused")
public class JuiceHttpControl implements HttpControl {
	private final Iterator<IJuiceHandler> handlerIterator;
    private final Executor executor;
    private final ChannelHandlerContext ctx;
	private final JuiceHttpRequest juiceHttpRequest;
    private final org.jboss.netty.handler.codec.http.HttpRequest nettyHttpRequest;
    private final org.jboss.netty.handler.codec.http.HttpResponse nettyHttpResponse;
    private final Thread.UncaughtExceptionHandler exceptionHandler;
    private final Thread.UncaughtExceptionHandler ioExceptionHandler;
	
    private HttpRequest defaultRequest;
    private HttpResponse defaultResponse;
    private HttpControl defaultControl;
    //private NettyWebSocketConnection webSocketConnection;
    //private NettyEventSourceConnection eventSourceConnection;
    
	public JuiceHttpControl(Iterator<IJuiceHandler> handlerIterator,
                            Executor executor,
                            ChannelHandlerContext ctx,
                            JuiceHttpRequest request,
                            JuiceHttpResponse response,
                            org.jboss.netty.handler.codec.http.HttpRequest nettyHttpRequest,
                            org.jboss.netty.handler.codec.http.HttpResponse nettyHttpResponse,
                            Thread.UncaughtExceptionHandler exceptionHandler,
                            Thread.UncaughtExceptionHandler ioExceptionHandler){
		this.handlerIterator = handlerIterator;
		this.executor = executor;
		this.ctx = ctx;
		this.juiceHttpRequest = request;
		this.defaultResponse = response;
		this.nettyHttpRequest = nettyHttpRequest;
		this.nettyHttpResponse = nettyHttpResponse;
		this.ioExceptionHandler = ioExceptionHandler;
		this.exceptionHandler = exceptionHandler;
		
		defaultRequest = request;
		defaultControl = this;
	}
	
	public HttpRequest getDefaultRequest() {
		return defaultRequest;
	}

	@Override
	public void execute(Runnable command) {
		handlerExecutor().execute(command);
	}

	@Override
	public void nextHandler() {
		nextHandler(defaultRequest, defaultResponse, defaultControl);
	}

	@Override
	public void nextHandler(HttpRequest request, HttpResponse response) {
		nextHandler(request, response, defaultControl);
	}

	@Override
	public void nextHandler(HttpRequest request, HttpResponse response,
			HttpControl control) {
		this.defaultRequest = request;
		this.defaultResponse = response;
		this.defaultControl = control;
		if (handlerIterator.hasNext()) {
			IJuiceHandler handler = handlerIterator.next();
			try {
				handler.handlerRequest(request, response, control);
			}catch (Throwable e) {
				response.error(e);
			}
		} else {
			response.status(404).end();
		}
	}


	@Override
	public Executor handlerExecutor() {
		return executor;
	}

	@Override
	public int getChannelId()
	{
		return this.ctx.getChannel().getId();
	}
}
