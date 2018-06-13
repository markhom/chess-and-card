package com.linyun.pay.server;

import org.apache.log4j.Logger;
import com.linyun.bottom.container.Container;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.pay.service.PayServer;



public class Application
{
	private static Logger logger = LoggerFactory.getLogger(Application.class);

	public void init()
	{
		initServer();
	}

	public void initServer()
	{
		logger.info("Init servers start......");
		
		Container.registerServer("payServer", new PayServer());
		logger.info("Init Login servers finished......");
	}
}
