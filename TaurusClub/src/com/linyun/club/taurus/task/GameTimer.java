/**
 * 
 */
package com.linyun.club.taurus.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.schedule.SimpleTaskManagerService;
import com.linyun.club.taurus.engine.GameEngine;

 

/**
 * @author queshaojie
 * 
 *         lewan
 */
public class GameTimer {
	private static Logger logger = LoggerFactory.getLogger(GameTimer.class);
	/** 计时器间隔时间单位*/
	public static final int DEFAULT_TIME = 1;
	/** 缓存池大小*/
	public static final int DEFAULT_POOL_SIZE = 1;
	//
	private static GameTimer timer = new GameTimer();
	private static Random random = new Random();
	private SimpleTaskManagerService taskManager;
	private List<EngineTask> taskList;
	
	private GameTimer() 
	{
		init();
	}
	
	public static GameTimer getInstance() 
	{
		return timer;
	}
	
	public void init() 
	{
		this.taskManager = new SimpleTaskManagerService(DEFAULT_POOL_SIZE);
		this.taskList = new ArrayList <EngineTask>();
		for(int i=0; i < DEFAULT_POOL_SIZE * 4; i++) 
		{
			EngineTask task = new EngineTask();
			taskList.add(task);
			taskManager.scheduleAtFixedRate(task, 0, DEFAULT_TIME, TimeUnit.SECONDS);
		}
	}
	
	public void addEngine(GameEngine engine) 
	{
		if (taskList==null || taskList.size() == 0) 
		{
			logger.error("GameRunTask list is error, can't add engine!");
			return;
		}
		taskList.get(random.nextInt(taskList.size())).addEngine(engine);
	}
	
	public void removeEngine(GameEngine engine)
	{
		if (taskList==null || taskList.size() == 0)
		{
			logger.error("GameRunTask list is error, can't remove engine!");
			return;
		}
		for (EngineTask task : taskList) {
			task.removeEngine(engine);
		}
	}
}
