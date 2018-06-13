/**
 * Juice
 * com.juice.orange.game.schedule
 * SimpleTaskManagerService.java
 */
package com.linyun.bottom.schedule;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author shaojieque 2013-4-7
 */
public class SimpleTaskManagerService extends ScheduledThreadPoolExecutor
		implements TaskManagerService {
	/**
	 * Used to create a unique identifier for each task
	 */
	private AtomicInteger taskNum;

	public SimpleTaskManagerService(int corePoolSize)
	{
		super(corePoolSize);
		taskNum = new AtomicInteger(0);
	}

	@Override
	public void execute(Task task)
	{
		super.execute(task);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public ScheduledFuture schedule(final Task task, long delay, TimeUnit unit)
	{
		task.setId(taskNum.incrementAndGet());
		return super.schedule(task, delay, unit);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public ScheduledFuture scheduleAtFixedRate(Task task, long initialDelay,
			long period, TimeUnit unit)
	{
		task.setId(taskNum.incrementAndGet());
		return super.scheduleAtFixedRate(task, initialDelay, period, unit);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public ScheduledFuture scheduleWithFixedDelay(Task task,
			long initialDelay, long delay, TimeUnit unit)
	{
		task.setId(taskNum.incrementAndGet());
		return super.scheduleWithFixedDelay(task, initialDelay, delay, unit);
	}
}
