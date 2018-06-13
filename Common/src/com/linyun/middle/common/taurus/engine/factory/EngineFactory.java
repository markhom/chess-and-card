package com.linyun.middle.common.taurus.engine.factory;

import org.apache.log4j.Logger;

import com.linyun.bottom.common.exception.GameException;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.middle.common.taurus.engine.GameEngine;
import com.linyun.middle.common.taurus.engine.TaurusAllEngine;
import com.linyun.middle.common.taurus.engine.TaurusBrightEngine;
import com.linyun.middle.common.taurus.engine.TaurusFixedEngine;
import com.linyun.middle.common.taurus.engine.TaurusFreeEngine;
import com.linyun.middle.common.taurus.engine.TaurusRotateEngine;
import com.linyun.middle.common.taurus.engine.TaurusTaurusEngine;
import com.linyun.middle.common.taurus.manager.EngineManager;
import com.linyun.middle.common.taurus.room.TaurusRoom;
import com.linyun.middle.common.taurus.table.TaurusTable;

public class EngineFactory 
{   
	private static Logger logger = LoggerFactory.getLogger(EngineFactory.class);
	
	public static GameEngine newEngine(TaurusTable table, TaurusRoom room) throws GameException
	{   
		GameEngine gameEngine = EngineManager.getGameEngine(room.getConfig().getBankerMode());
		switch (room.getConfig().getBankerMode()) 
		{
		case BANKER_MODE_TAURUS:
			if(gameEngine == null)
			{
				gameEngine = new TaurusTaurusEngine(table, room);
				logger.info("游戏开始，初始化引擎，资源池中没有空闲引擎，new一个新的！模式--牛牛上庄");
			}
			else
			{
				 gameEngine.InitEngine(table, room);
				logger.info("游戏开始，初始化引擎时,从资源池中获取引擎资源，模式--牛牛上庄");
			}
			return gameEngine ;
		case BANKER_MODE_ROTATE:
			if(gameEngine == null)
			{
				gameEngine = new TaurusRotateEngine(table, room);
				logger.info("游戏开始，初始化引擎时，资源池中没有空闲引擎，new一个新的！模式--轮庄");
			}
			else
			{
				gameEngine.InitEngine(table, room);
				logger.info("游戏开始，初始化引擎时,从资源池中获取引擎资源，模式--轮庄");
			}
			return gameEngine ;
		case BANKER_MODE_ALL_COMPARE:
			if(gameEngine == null)
			{
				gameEngine = new TaurusAllEngine(table, room);
			}
			else
			{
				gameEngine.InitEngine(table, room);
			}
			return gameEngine ;
		case BANKER_MODE_BRIGHT_ROB:
			if(gameEngine == null)
			{
				gameEngine = new TaurusBrightEngine(table, room);
			}
			else
			{
				gameEngine.InitEngine(table, room);
			}
			return gameEngine ;
		case BANKER_MODE_FIXED:
			if(gameEngine == null)
			{
				gameEngine = new TaurusFixedEngine(table, room);
			}
			else
			{
				gameEngine.InitEngine(table, room);
			}
			return gameEngine ;
		case BANKER_MODE_FREE:
			if(gameEngine == null)
			{
				gameEngine = new TaurusFreeEngine(table, room);
			}
			else
			{
				gameEngine.InitEngine(table, room);
			}
			return gameEngine ;
		default:
			throw new GameException(GameException.ERROR_UP_BANKER_MODE, "错误的上庄模式");
		}
	}
	
}
