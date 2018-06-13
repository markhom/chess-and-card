/**
 * Juice
 * com.juice.orange.game.rmi
 * RPCServerControl.java
 */
package com.linyun.bottom.rmi.control;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;

import com.linyun.bottom.handler.IJuiceControl;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.rmi.Result;
import com.linyun.bottom.rmi.Transport;
import com.linyun.bottom.util.ClassUtils;
import com.linyun.bottom.util.JavaSerializeUtils;


/**
 * @author shaojieque
 * 2013-7-29
 */
public class RPCServerControl implements IJuiceControl {
	private static Logger logger = LoggerFactory.getLogger(RPCServerControl.class);
	//
	private final Executor executor;
	private Transport transport;
	private ChannelHandlerContext ctx;
	
	public RPCServerControl(final Executor executor, 
			ChannelHandlerContext ctx, Transport transport){
		this.executor = executor;
		this.transport = transport;
		this.ctx = ctx;
	}
	
	
	@Override
	public void execute(Runnable command) {
		handlerExecutor().execute(command);
	}

	@Override
	public void nextHandler() {
		try {
			String clazz = transport.getClazz();
			Object obj = ClassUtils.getObject(clazz);
			//
			Class<?>[] parameterTypes = transport.getParamTypes();
			Object[] params = transport.getArgs();
			Method method = obj.getClass().getMethod(transport.getMethod(), parameterTypes);
			Object result = null;
			try {
				result = method.invoke(obj, params);
			}catch (InvocationTargetException ie) {
				result = ie.getTargetException();
			}
			Result rs = new Result();
			rs.setId(transport.getId());
			rs.setResult(result);
			ctx.getChannel().write(JavaSerializeUtils.getInstance().getChannelBuffer(rs));
		}catch(Exception e) {
			logger.error(e);
		}
	}

	@Override
	public Executor handlerExecutor() {
		return executor;
	}


	public Transport getTransport() {
		return transport;
	}


	@Override
	public int getChannelId()
	{
		return this.ctx.getChannel().getId();
	}
}
