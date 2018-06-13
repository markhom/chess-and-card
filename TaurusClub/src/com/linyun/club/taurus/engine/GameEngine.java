package com.linyun.club.taurus.engine;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.linyun.common.entity.User;
import com.linyun.middle.common.taurus.player.HundredsTaurusPlayer;
import com.linyun.middle.common.taurus.table.HundredsTaurusTable;


/**
*  @Author walker
*  @Since 2018年5月23日
**/

public abstract class GameEngine
{

	/** 游戏当前阶段
	 *  1.等待开始押注
	 *  2.开始押注，玩家可以进行押注，等待停止押注
	 *  3.停止押注,等待发牌
	 *  4，发牌，等待比牌
	 *  5.比牌完成，等待结算
	 *  6.结算
	 *  7.房间歇业。
	 * */
	public static final int GAME_STEP_BEGIN=0;
	public static final int GAME_STEP_WAIT = 1;
	public static final int GAME_STEP_START_BET = 2;
	public static final int GAME_STEP_STOP_BET = 3;
	public static final int GAME_STEP_DEAL = 4;
	public static final int GAME_STEP_CMP = 5;
	public static final int GAME_STEP_FINISH = 6;
	public static final int GAME_STEP_REST = 7;
	public static final int GAME_STEP_END=200;
	public int step = GAME_STEP_BEGIN;
	
	public static final String GAME_ENGINE = "GameEngine";
	public static final int PLAYER_BANKER_TIME = 3; 
	
	/** 房间内的玩家列表 */
	protected ConcurrentHashMap<String, HundredsTaurusPlayer> playerMap =  new ConcurrentHashMap<String, HundredsTaurusPlayer>(); 
	
	/** 房间内的玩家列表List*/
	protected Map<String,User> userMap = new LinkedHashMap<String,User>();
	
	/** 要上庄的玩家列表 */
	protected final LinkedList<HundredsTaurusPlayer> listBankers =  new LinkedList<HundredsTaurusPlayer>();
	
	//在退出房间时  的 庄家 或者 已经下注的玩家
	protected final Set<String> listExitUserId = new HashSet<String>();
	
	
	protected final int engineId;
	/** 当前玩家数 */
	public int curPlayers;
	/** 当前局数 */
	protected int m_curRound;
	/** showNum局数 唯一 */
	public int m_showNum;
	
	
	/** 桌子 */
	protected HundredsTaurusTable table = null;
	public GameEngine(HundredsTaurusTable table)
	{
		this.table = table;
		this.engineId = table.getTableId();
		
		this.m_curRound = 1;
		this.m_showNum = 1;
		this.curPlayers = 0;
	}

	public void removeUserInExitList(String userId)
	{
		synchronized (listExitUserId) 
		{
			listExitUserId.remove(userId);
		}
		
	}
	public void addUserInExitList(String userId)
	{
		synchronized (listExitUserId) 
		{
			listExitUserId.add(userId);
		}
	}
	
	public int getEngineId() 
	{
		return engineId;
	}
	
	public Map<String,User> getUserMap()
	{
		return userMap;
	}
	
	public HundredsTaurusPlayer getPlayer(String userId)
	{
		return playerMap.get(userId);
		
	}
	public Map<String, HundredsTaurusPlayer> getPlayerMap()
	{
		return this.playerMap;
	}

	public void delPlayer(String userId)
	{
		synchronized(playerMap)
		{
			if(playerMap.containsKey(userId))
			{
				playerMap.remove(userId);
				curPlayers--;
			}
		}
		
	}
	
	public void addBanker(HundredsTaurusPlayer player)
	{
		synchronized(listBankers)
		{
			listBankers.add(player);
		}
	}
	public void removeFirstBanker()
	{
		synchronized(listBankers)
		{
			listBankers.remove(0);
		}
	}
	public void removeBanker(HundredsTaurusPlayer player)
	{
		synchronized(listBankers)
		{
			int index = listBankers.indexOf(player);
			if (index != -1)
			{
				listBankers.remove(index);
			}
		}
	}
	public int indexOfBanker(HundredsTaurusPlayer player)
	{
		synchronized(listBankers)
		{
			return listBankers.indexOf(player);
		}
	}
	public int getBanekerSize()
	{
		synchronized(listBankers)
		{
			return listBankers.size();
		}
	}
	
	public List<HundredsTaurusPlayer> getBanekerList()
	{
		return listBankers;
	}
	
	public boolean isWaitBankerList(HundredsTaurusPlayer player)
	{
		synchronized(listBankers)
		{
			return listBankers.indexOf(player) != -1;
		}
	}
	
	
	public int getCurRound()
	{
		return m_curRound;
	}
	public void setCurRound(int curRound) 
	{
		this.m_curRound = curRound;
	}
	
	public abstract void addPlayer(int tableId, String userId);
	
	
	
	/**
	 * 游戏主线程
	 */
	public abstract void running(int timeFrame) throws Throwable;
	/**
	 * 发牌
	 * */
	
	/**
	 * 停止下注
	 * */
	public abstract void StopBet();
	
	/**
	 * 比牌
	 * */
	public abstract void ComparePocker();
	/**
	 * 清理资源
	 * */
	public abstract void finish();
}
