/**
 * 
 */
package com.linyun.bottom.notify;

import java.util.concurrent.Executor;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;

import com.linyun.bottom.container.Container;
import com.linyun.bottom.handler.IJuiceControl;
import com.linyun.bottom.log.LoggerFactory;



/**
 * @author queshaojie
 *
 * lewan
 */
public class NotifyServerControl implements IJuiceControl {
	private static Logger logger = LoggerFactory.getLogger(NotifyServerControl.class);
	//
	private final Executor executor;
	private Object obj;
	private ChannelHandlerContext ctx;
	
	public NotifyServerControl(final Executor executor, 
			ChannelHandlerContext ctx, Object obj){
		this.executor = executor;
		this.obj = obj;
		this.ctx = ctx;
	}
	
	
	@Override
	public void execute(Runnable command) {
		handlerExecutor().execute(command);
	}

	@Override
	public void nextHandler() {
		try {
			Container.notifyListener(obj);
		}catch(Exception e) {
			logger.error(e);
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
