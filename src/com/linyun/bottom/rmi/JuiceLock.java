/**
 * Juice
 * com.juice.orange.game.rmi
 * JuiceLock.java
 */
package com.linyun.bottom.rmi;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author shaojieque 
 * 2013-4-17
 */
public class JuiceLock {
	private Lock lock;
	private Condition condition;

	public JuiceLock() {
		this.lock = new ReentrantLock();
		this.condition = lock.newCondition();
	}

	public void lock() {
		lock.lock();
	}

	public void unlock() {
		lock.unlock();
	}

	public void await() throws InterruptedException {
		condition.await();
	}

	public boolean await(long time, TimeUnit unit) throws InterruptedException {
		return condition.await(time, unit);
	}

	public void signal() {
		condition.signal();
	}
}
