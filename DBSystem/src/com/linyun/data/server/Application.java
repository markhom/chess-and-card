package com.linyun.data.server;

import org.apache.log4j.Logger;

import com.linyun.bottom.log.LoggerFactory;


public class Application
{
	private static Logger logger = LoggerFactory.getLogger(Application.class);
	
	public void init()
	{
		initServer();

		
		initSchedulerJob();
	}

	public void initServer()
	{
		logger.info("Init servers......");
	}

	public void initSchedulerJob()
	{
		logger.info("Init Scheduler Job......");

		//SchedulerUtils.getInstance().addSchedulerJob(userAO, DailyClearJob.class, BaseJob.JOB_USER_AO, "0 0 0 * * ?");
	}

}
