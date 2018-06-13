/**
 * 
 */
package com.linyun.bottom.rmi.thread;

import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.linyun.bottom.exception.JuiceException;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.rmi.RPCUnit;
import com.linyun.bottom.rmi.RemoteChannelFactory;
import com.linyun.bottom.rmi.RemoteConfig;
import com.linyun.bottom.util.JavaSerializeUtils;


/**
 * @author queshaojie
 * 
 *         lewan
 */
public class RPCThreadPool {
	private static Logger logger = LoggerFactory.getLogger(RPCThreadPool.class);
	// 队列最大容量
	public static final int Queue_SIZE = 1024000;
	private final ArrayBlockingQueue<RPCUnit> requestQueue;

	public RPCThreadPool() {
		this.requestQueue = new ArrayBlockingQueue<RPCUnit>(Queue_SIZE);
		initWorkThread();
	}

	public void addRequestQueue(RPCUnit unit) {
		requestQueue.add(unit);
	}

	private void initWorkThread() {
		for (int i = 0; i < RPCPoolManager.WORK_THREAD_SIZE; i++) {
			RPCWorkThread workThread = new RPCWorkThread();
			workThread.start();
		}
	}

	public class RPCWorkThread extends Thread {
		public final static int REMOTE_TIME_OUT = 10000;

		@Override
		public void run() {
			while (true) {
				try {
					RPCUnit unit = requestQueue.take();
					if (unit == null)
						return;
					sendObject(unit.getUnit(), unit.getRemoteConfig());
				} catch (Exception e) {
					logger.error("SEND", e);
				}
			}
		}

		private void sendObject(Object obj, RemoteConfig rc) throws Exception {
			RemoteChannelFactory rcf = new RemoteChannelFactory();
			Channel channel = rcf.get(rc, REMOTE_TIME_OUT);
			if (!channel.isConnected()) {
				rcf.removeClient(rcf.getKey(rc));
				channel = rcf.get(rc, REMOTE_TIME_OUT);
			}
			if (!channel.isConnected()) {
				throw new JuiceException(
						"Remote channle is not connected, check channel:"
								+ channel.getRemoteAddress());
			}
			channel.write(JavaSerializeUtils.getInstance()
					.getChannelBuffer(obj));
		}
	}

}
