/**
 * 
 */
package com.linyun.bottom.util.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.linyun.bottom.handler.IJuiceControl;


/**
 * @author queshaojie
 * 
 *         lewan
 */
public class JuicePoolManager {
	private static Random random = new Random();
	private static List<JuiceThreadPool> threadPools = new ArrayList<JuiceThreadPool>();
	private static JuicePoolManager manager;
	
	private JuicePoolManager() {
	}

	public static JuicePoolManager getInstance() {
		if (manager==null) {
			manager = new JuicePoolManager();
		}
		return manager;
	}
	
	public void addThreadPool(JuiceThreadPool jtp) {
		threadPools.add(jtp);
	}
	
	public void addRequest(IJuiceControl control) {
		if (threadPools.size() > 0) {
			JuiceThreadPool jtp = null;
			jtp = threadPools.get(random.nextInt(threadPools.size()));
			jtp.addJuiceNotify(control);
		}
	}
}
