/**
 * Juice
 * com.juice.orange.game.rmi
 * JuiceInvocationHandler.java
 */
package com.linyun.bottom.rmi;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.linyun.bottom.container.Container;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.rmi.thread.RPCPoolManager;


/**
 * @author shaojieque 
 * 2013-4-11
 */
public class JuiceInvocationHandler implements InvocationHandler, Serializable {
	private static Logger logger = LoggerFactory.getLogger(JuiceInvocationHandler.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//
	public static final int REMOTE_TIME_OUT = 3000;
	//
	//private Channel channel;
	private String remoteName;
	private String clazzName;

	public JuiceInvocationHandler(String remoteName, String clazzName) {
		//this.channel = channel;
		this.remoteName = remoteName;
		this.clazzName = clazzName;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Transport t = new Transport();
		//t.setClazz(proxy.getClass().getName());
		t.setClazz(clazzName);
		t.setMethod(method.getName());
		t.setParamTypes(method.getParameterTypes());
		t.setArgs(args);
		CountDownLatch latch = new CountDownLatch(1);
		t.setLatch(latch);
		// send a request to server
		JuiceRemoteManager.addTransport(t);
		//
		RemoteConfig rc = Container.getRemoteConfigs().get(remoteName);
		RPCUnit unit = new RPCUnit(t, rc);
		RPCPoolManager.getInstance().addRequest(unit);
		try {
			boolean timeOut = t.getLatch().await(REMOTE_TIME_OUT, TimeUnit.MILLISECONDS);
			if(!timeOut){
				logger.error("Latch request time out..");
			}
		}finally {
			JuiceRemoteManager.removeTransport(t.getId());
		}
		//
		if (t.getResult().getResult() instanceof Throwable) {
			throw (Throwable) t.getResult().getResult();
		}
		return t.getResult().getResult();
	}
}
