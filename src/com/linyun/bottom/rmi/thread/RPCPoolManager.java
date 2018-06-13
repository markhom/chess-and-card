/**
 * 
 */
package com.linyun.bottom.rmi.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.linyun.bottom.exception.JuiceException;
import com.linyun.bottom.rmi.RPCUnit;


/**
 * @author queshaojie
 * 
 *         lewan
 */
public class RPCPoolManager {
	public static final int MAX_POOL_SIZE = 4;
	public static final int WORK_THREAD_SIZE = MAX_POOL_SIZE * 2;
	private static Random random = new Random();
	private static List<RPCThreadPool> threadPools = new ArrayList<RPCThreadPool>();
	private static RPCPoolManager manager = new RPCPoolManager();
	private RPCPoolManager() {
		init();
	}
	
	public static RPCPoolManager getInstance() {
		return manager;
	}
	
	private void init() {
		for(int i = 0; i < MAX_POOL_SIZE; i++) {
			RPCThreadPool threadPool = new RPCThreadPool();
			threadPools.add(threadPool);
		}
	}
	
	public void addRequest(RPCUnit unit) throws JuiceException{
		if (threadPools.size() > 0) {
			RPCThreadPool threadPool = threadPools.get(random.nextInt(threadPools.size()));
			threadPool.addRequestQueue(unit);
		} else {
			throw new JuiceException("RPCThreadPool List is null, can't add RPCUnit!");
		}
	}
}
