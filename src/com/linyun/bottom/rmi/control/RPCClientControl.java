/**
 * Juice
 * com.juice.orange.game.rmi.control
 * RPCClientControl.java
 */
package com.linyun.bottom.rmi.control;

import java.util.concurrent.Executor;

import com.linyun.bottom.handler.IJuiceControl;
import com.linyun.bottom.rmi.JuiceRemoteManager;
import com.linyun.bottom.rmi.Result;
import com.linyun.bottom.rmi.Transport;


/**
 * @author shaojieque
 * 2013-8-6
 */
public class RPCClientControl implements IJuiceControl {
	//
	private final Executor executor;
	private Result result;
	
	public RPCClientControl(final Executor executor, Result result){
		this.executor = executor;
		this.result = result;
	}

	@Override
	public void nextHandler() {
		System.out.println("RPCClientControl:" + System.currentTimeMillis() + ";result:"+result.getId());
		Transport t = JuiceRemoteManager.getTransport(result.getId());
		if (t!=null) {
			t.setResult(result);
			t.getLatch().countDown();
		}
	}
	
	@Override
	public void execute(Runnable command) {
		this.executor.execute(command);
	}

	@Override
	public Executor handlerExecutor() {
		return executor;
	}

	@Override
	public int getChannelId()
	{
		// TODO Auto-generated method stub
		return 0;
	}
}
