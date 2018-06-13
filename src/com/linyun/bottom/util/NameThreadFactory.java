/**
 * Juice
 * com.juice.orange.game.util
 * NameThreadFactory.java
 */
package com.linyun.bottom.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author shaojieque
 * 2013-3-20
 */
public class NameThreadFactory implements ThreadFactory {
	private static final AtomicInteger factoryCount = new AtomicInteger();
	private final AtomicInteger threadCount = new AtomicInteger();
	
	private final ThreadFactory factory;
	private String prefix;
	
	public NameThreadFactory(ThreadFactory factory, String prefix) {
		this.factory = factory;
		this.prefix = prefix;
		factoryCount.incrementAndGet();
	}
	
	public NameThreadFactory(String prefix){
		this(Executors.defaultThreadFactory(), prefix);
	}
	
	@Override
	public Thread newThread(Runnable r) {
		threadCount.incrementAndGet();
		Thread thread = factory.newThread(r);
		thread.setName(threadName());
		return thread;
	}

	protected String threadName() {
        return String.format("%s-%d-%d-thread", prefix, factoryCount.intValue(), threadCount.intValue());
    }
}
