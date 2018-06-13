package com.linyun.middle.common.taurus.manager;

import java.util.LinkedList;

import com.linyun.common.taurus.eum.BankerMode;
import com.linyun.middle.common.taurus.engine.GameEngine;
import com.linyun.middle.common.taurus.engine.TaurusAllEngine;
import com.linyun.middle.common.taurus.engine.TaurusBrightEngine;
import com.linyun.middle.common.taurus.engine.TaurusFixedEngine;
import com.linyun.middle.common.taurus.engine.TaurusFreeEngine;
import com.linyun.middle.common.taurus.engine.TaurusRotateEngine;
import com.linyun.middle.common.taurus.engine.TaurusTaurusEngine;

public class EngineManager 
{     
	private static LinkedList<GameEngine>  taurusAllEngineList = new LinkedList<GameEngine>();
	private static LinkedList<GameEngine>  taurusBrightEngineList = new LinkedList<GameEngine>();
	private static LinkedList<GameEngine>  taurusFixedEngineList = new LinkedList<GameEngine>();
	private static LinkedList<GameEngine>  taurusFreeEngineList = new LinkedList<GameEngine>();
	private static LinkedList<GameEngine>  taurusRotateEngineList = new LinkedList<GameEngine>();
	private static LinkedList<GameEngine>  taurusTaurusEngineList = new LinkedList<GameEngine>();
	private static int count = 0;
	
	public static void  addGameEngine(GameEngine engine)
	{   
		if(engine instanceof TaurusAllEngine)
		{
			synchronized (taurusAllEngineList)
			{
				taurusAllEngineList.add(engine);
				++count;
			}
		}
		else if (engine instanceof TaurusBrightEngine)
		{
			synchronized (taurusBrightEngineList)
			{
				taurusBrightEngineList.add(engine);
				++count;
			}
		}
		else if (engine instanceof TaurusFixedEngine)
		{
			synchronized (taurusFixedEngineList)
			{
				taurusFixedEngineList.add(engine);
				++count;
			}
		}
		else if (engine instanceof TaurusFreeEngine)
		{
			synchronized (taurusFreeEngineList)
			{
				taurusFreeEngineList.add(engine);
				++count;
			}
		}
		else if (engine instanceof TaurusRotateEngine)
		{
			synchronized (taurusRotateEngineList)
			{
				taurusRotateEngineList.add(engine);
				++count;
			}
		}
		else if (engine instanceof TaurusTaurusEngine)
		{
			synchronized (taurusTaurusEngineList)
			{
				taurusTaurusEngineList.add(engine);
				++count;
			}
		}
	}
	
	//根据创建房间的类型，先在资源池中寻找是否有对应类型的引擎，有则返回此引擎 没有返回null
	public static GameEngine getGameEngine(BankerMode gameMode)
	{   
		LinkedList<GameEngine> list = null;
		switch (gameMode)
		{
		 	case BANKER_MODE_ALL_COMPARE:
		 		list = taurusAllEngineList;
		 		break;
		    case BANKER_MODE_BRIGHT_ROB:
		    	list = taurusBrightEngineList;
		        break;
		    case BANKER_MODE_FIXED:
		    	list = taurusFixedEngineList;
		    	break;
		    case BANKER_MODE_FREE:
		    	list = taurusFreeEngineList;
		    	break;
		    case BANKER_MODE_ROTATE:
		    	list = taurusRotateEngineList;
		    	break;
		    case BANKER_MODE_TAURUS:
		    	list = taurusTaurusEngineList;
		    	break;
		}
		
		if (list == null)
		{
			return null;
		}
		
		synchronized (list)
 		{
 			if (list.isEmpty())
	 		{
 				return null;
			}
 			--count;
 			return list.removeFirst();
 		}
	}
	
	public static int getCount()
	{
		return count;
	}
}
