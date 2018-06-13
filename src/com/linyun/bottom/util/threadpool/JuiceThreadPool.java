/**
 * 
 */
package com.linyun.bottom.util.threadpool;

import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;

import com.linyun.bottom.handler.DefaultJuiceControl;
import com.linyun.bottom.handler.IJuiceControl;
import com.linyun.bottom.handler.JuiceHttpControl;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.rmi.control.RPCServerControl;


/**
 * @author queshaojie
 * 
 *         orangegame
 */
public class JuiceThreadPool {
	//@off
	private static Logger logger = LoggerFactory.getLogger(JuiceThreadPool.class);
	//@on
	// 闃熷垪鏈�澶у閲�
	public static final int Queue_SIZE = 1024000;
	public static final int REQUEST_TIME_OUT = 0;//10000
	public static final int POOL_WARN_SIZE = 200;
	public static final int THREAD_SIZE = 10;
	public static final int REQUEST_HANDLER_TIMEOUT = 2000;
	private ArrayBlockingQueue<JuiceNotify> requestQueue;

	public JuiceThreadPool() {
		this.requestQueue = new ArrayBlockingQueue<JuiceNotify>(Queue_SIZE);
	}

	public void addJuiceNotify(IJuiceControl control) {
		JuiceNotify jn = new JuiceNotify();
		jn.setControl(control);
		jn.setRecvTime(System.currentTimeMillis());
		requestQueue.add(jn);
	}

	public void initWorkThread() {
		for (int i = 0; i < THREAD_SIZE; i++) {
			JuiceWorkThread jwt = new JuiceWorkThread();
			jwt.start();
		}
	}

	public class JuiceWorkThread extends Thread {
		@Override
		public void run() {
			IJuiceControl control = null;
			while (true) {
				try {
					JuiceNotify jn = requestQueue.take(); // .poll(100,TimeUnit.MILLISECONDS);
					long now = 0;
					long timeout = 0;
					int poolSize = 0;
					if (jn != null) {
						now = System.currentTimeMillis();
						timeout = now - jn.getRecvTime();
						if (timeout > REQUEST_HANDLER_TIMEOUT) {
							logger.warn("request queue wait too long, wait time:"
									+ timeout);
						}
						//
						control = jn.getControl();
						control.nextHandler();
						timeout = System.currentTimeMillis() - now;
//						if (timeout >= REQUEST_TIME_OUT) {
//							//printControlInfo(control, now);
//						}
						poolSize = requestQueue.size();
						if (poolSize >= POOL_WARN_SIZE) {
							logger.error("Thread pool request queue size:"
									+ poolSize + " is not ok");
						}
					}
				} catch (InterruptedException e) {
					logger.error("WorkThread exception", e);
				} catch (Exception e) {
					logger.error("control exception ", e);
				}
			}
		}
		
		@SuppressWarnings("unused")
		private void printControlInfo(IJuiceControl control, long now) {
			//@off
			if (control instanceof DefaultJuiceControl) {
				DefaultJuiceControl _control = (DefaultJuiceControl) control;
				StringBuilder memo = new StringBuilder();
				memo.append("method start time :")
						.append(System.currentTimeMillis() - now)
						.append(", protocolId:")
						.append(_control.getMessage().getProtocolId());
				logger.warn(memo.toString());
			} else if (control instanceof RPCServerControl) {
				RPCServerControl _control = (RPCServerControl) control;
				StringBuilder memo = new StringBuilder();
				memo.append("RPC method start time :")
						.append(System.currentTimeMillis() - now)
						.append(", action:")
						.append(_control.getTransport().getClazz())
						.append(", method:")
						.append(_control.getTransport().getMethod());
				logger.warn(memo.toString());
			} else if (control instanceof JuiceHttpControl) {
				JuiceHttpControl _control = (JuiceHttpControl) control;
				StringBuilder memo = new StringBuilder();
				memo.append("HTTP method start time :")
						.append(System.currentTimeMillis() - now)
						.append(", uri:")
						.append(_control.getDefaultRequest().uri());
				logger.warn(memo.toString());
			}
			//@on
		}
	}
}
