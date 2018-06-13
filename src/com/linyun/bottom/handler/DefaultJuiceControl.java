/**
 * Juice
 * com.juice.orange.game.handler
 * DefaultJuiceControl.java
 */
package com.linyun.bottom.handler;

import java.util.Iterator;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

import com.linyun.bottom.log.LoggerFactory;


/**
 * @author shaojieque
 * 2013-3-22
 */
public class DefaultJuiceControl implements IJuiceControl{
	private static Logger logger = LoggerFactory.getLogger(DefaultJuiceControl.class);
	
	private final Iterator<IJuiceHandler> handlerIterator;
    private final Executor executor;
    
    private IJuiceRequest defaultRequest;
    private IJuiceResponse defauResponse;
    private DefaultJuiceControl defaultControl;
    private DefaultJuiceMessage message;
    private final int channelId;
	
	public DefaultJuiceControl (final Iterator<IJuiceHandler> handlerIterator,
			final Executor executor,
			IJuiceRequest request, 
			IJuiceResponse response,
			DefaultJuiceMessage message,
			int channelId) {
		this.handlerIterator = handlerIterator;
		this.defaultRequest = request;
		this.defauResponse = response;
		this.executor = executor;
		this.message = message;
		this.defaultControl = this;
		this.channelId = channelId;
	}
	
	@Override
	public void execute(Runnable command) {
		handlerExecutor().execute(command);
	}

	@Override
	public void nextHandler() {
		nextHandler(defaultRequest, defauResponse, defaultControl);
	}
	
	private void nextHandler(IJuiceRequest request, IJuiceResponse response, IJuiceControl control) {
		if (handlerIterator.hasNext()) {
			IJuiceHandler handler = handlerIterator.next();
			try {
				handler.handlerRequest(request, response, control);
			}catch (Throwable e) {
				logger.error(e);
				//
			}
		} else {
			//
		}
	}

	@Override
	public Executor handlerExecutor() {
		return executor;
	}

	public DefaultJuiceMessage getMessage() {
		return message;
	}

	public void setMessage(DefaultJuiceMessage message) {
		this.message = message;
	}

	@Override
	public int getChannelId()
	{
		return this.channelId;
	}
}
