package com.linyun.middle.common.taurus.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.linyun.bottom.log.LoggerFactory;
import com.linyun.middle.common.taurus.server.ActionAware;
import com.linyun.middle.common.taurus.service.BaseServer;

public class OnlineTask extends ActionAware
{
	public static final int ONLINE_TIME_INTERVAL = 60*2;
	private static Logger logger = LoggerFactory.getLogger(OnlineTask.class);
	
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
					//计算在好友场里面的在线人数
					int count = taurusGameAction().addOnlineCount();
					//计算俱乐部的在线人数
					int count1 = BaseServer.userSessionMap.size();
					int countTotal = count + count1 ;
					logger.info("好友场在线人数:"+count+",俱乐部在线人数:"+count1+",在线总人数："+countTotal);
					if(countTotal > 0)
					{
						onlineAction().updateOnlineCount(countTotal);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
	    service.scheduleAtFixedRate(runnable, 30, ONLINE_TIME_INTERVAL, TimeUnit.SECONDS);  
	}
}
