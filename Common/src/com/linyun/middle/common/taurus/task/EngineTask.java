package com.linyun.middle.common.taurus.task;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.linyun.bottom.common.task.BaseTask;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.middle.common.taurus.engine.GameEngine;


/**
 * 主线程
 * 
 * @author queshaojie
 * 
 *         2014
 */
public class EngineTask extends BaseTask
{
	private static Logger logger = LoggerFactory.getLogger(EngineTask.class);

	private ConcurrentHashMap<String, GameEngine> engineMap; // 标准房 

	public EngineTask()
	{
		engineMap = new ConcurrentHashMap<String, GameEngine>(); 
	}

	public void addEngine(GameEngine engine)
	{
		engineMap.put(engine.getEngineId(), engine);
//		if (engine instanceof TaurusRotateEngine)
//		{
//		
//		}  else
//		{
//			logger.error("游戏方式异常,找不到对应的游戏类型！");
//		}
	}

	public void removeEngine(GameEngine engine)
	{
		engineMap.remove(engine.getEngineId());
//		if (engine instanceof TaurusRotateEngine)
//		{
//			engineMap.remove(engine.getEngineId());
//		}  else
//		{
//			logger.error("游戏方式异常,找不到对应的游戏类型！");
//		}
	}

	@Override
	public void run()
	{
		for (Entry<String, GameEngine> entry : engineMap.entrySet())
		{
			try
			{
				entry.getValue().running(GameTimer.DEFAULT_TIME);
			} 
			catch (Throwable e)
			{
				e.printStackTrace();
				logger.error(e.getStackTrace());
			}
		}
	}
}
