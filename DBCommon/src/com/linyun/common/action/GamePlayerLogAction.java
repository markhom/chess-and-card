package com.linyun.common.action;

import java.util.List;

import com.linyun.common.entity.GamePlayerLog;

/**
*  @Author walker
*  @Since 2018年4月20日
**/

public interface GamePlayerLogAction 
{
    public void addGamePlayerLog(GamePlayerLog g);
    
    public void insertPlayerLogBatch(List<GamePlayerLog> list);
	
	public List<GamePlayerLog> getAllPlayerLog(int clubId,  int roomNum);
	
	public GamePlayerLog getPlayerLogById(int clubId,int roomNum, int playerId);
	
	public void updatePlayerLog(int score,int result,int clubId,int roomNum, int playerId);
	
	public void updatePlayerBuyScoreHistory(int buyscore,int clubId,int roomNum, int playerId);
	
	public List<Integer> getAllPlayerIds(int clubId, int roomNum);
	
	public List<GamePlayerLog> getGameRecord(int playerId);
}
