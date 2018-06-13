/**
 * Juice
 * com.juice.orange.game.handler
 * IJuiceControl.java
 */
package com.linyun.bottom.handler;

import java.util.concurrent.Executor;

/**
 * @author shaojieque
 * 2013-3-21
 */
public interface IJuiceControl extends Executor{
	void nextHandler();
	
	int getChannelId();

	Executor handlerExecutor();
}
