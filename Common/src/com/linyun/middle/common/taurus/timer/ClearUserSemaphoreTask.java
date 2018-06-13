package com.linyun.middle.common.taurus.timer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.linyun.bottom.log.LoggerFactory;
import com.linyun.middle.common.taurus.service.BaseServer;

public class ClearUserSemaphoreTask 
{
	private static Logger logger = LoggerFactory.getLogger(ClearUserSemaphoreTask.class);
	
	public static final long CLEAR_USER_SEMAPHORE_TIME_INTERVAL = 1*24*60*60;
	
	
	private static ClearUserSemaphoreTask clearTask = new ClearUserSemaphoreTask();
		
	public static ClearUserSemaphoreTask getInstance()
	{
		return clearTask;
	}
	
	/**
	 * 每一天清理一次玩家的semaphore
	 * */
	public void start()
	{
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);  
	      
		Runnable runnable = new Runnable() 
		{
			@Override
			public void run() 
			{
				try
				{
					BaseServer.clearUserSemp();
				}
				catch (Exception e)
				{
					e.printStackTrace();
					logger.error("清理玩家信号量出错");
				}
			}
		};
	    service.scheduleAtFixedRate(runnable, 0, CLEAR_USER_SEMAPHORE_TIME_INTERVAL, TimeUnit.SECONDS);  
	}

}

