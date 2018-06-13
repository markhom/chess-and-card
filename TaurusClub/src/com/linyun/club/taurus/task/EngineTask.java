package com.linyun.club.taurus.task;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import com.linyun.bottom.common.task.BaseTask;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.club.taurus.engine.GameEngine;
import com.linyun.club.taurus.engine.HundredsTaurusEngine;



/**
*  @Author walker
*  @Since 2018年5月23日
**/

public class EngineTask extends BaseTask
{
	
	private static Logger logger = LoggerFactory.getLogger(EngineTask.class);

	private ConcurrentHashMap<Integer, GameEngine> HundredsEngineMap; // 

	public EngineTask()
	{
		HundredsEngineMap = new ConcurrentHashMap<Integer, GameEngine>(); 
	}

	public void addEngine(GameEngine engine)
	{
		if (engine instanceof HundredsTaurusEngine)
		{
			HundredsEngineMap.put(engine.getEngineId(), engine);
		}  else
		{
			logger.error("游戏方式异常,找不到对应的游戏类型！");
		}
	}

	public void removeEngine(GameEngine engine)
	{
		if (engine instanceof HundredsTaurusEngine)
		{
			HundredsEngineMap.remove(engine.getEngineId());
		}  else
		{
			logger.error("游戏方式异常,找不到对应的游戏类型！");
		}
	}

	@Override
	public void run()
	{
		for (Entry<Integer, GameEngine> entry : HundredsEngineMap.entrySet())
		{
			try
			{
				entry.getValue().running(GameTimer.DEFAULT_TIME);
			} 
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		}
		 
	}
}
