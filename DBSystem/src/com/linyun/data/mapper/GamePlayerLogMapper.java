package com.linyun.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.linyun.common.entity.GamePlayerLog;

/**
*  @Author walker
*  @Since 2018年4月13日
**/

public interface GamePlayerLogMapper 
{
	void addGamePlayerLog(GamePlayerLog g);
	
	void insertPlayerLogBatch(List<GamePlayerLog> list);
	
	List<GamePlayerLog> getAllPlayerLog(@Param(value="clubId") int clubId, @Param(value="roomNum") int roomNum);
	
	List<Integer> getAllPlayerIds(@Param("clubId") int clubId, @Param("roomNum") int roomNum);
	
	void updatePlayerLog(@Param("score")int score,@Param("result") int result, @Param("clubId")int clubId, @Param("roomNum")int roomNum, @Param("playerId")int playerId);
	
	void updatePlayerBuyscoreHistory(@Param("buyScore")int buyScore,@Param("clubId")int clubId, @Param("roomNum")int roomNum, @Param("playerId")int playerId);
   
	GamePlayerLog getPlayerLogById(@Param("clubId")int clubId, @Param("roomNum")int roomNum, @Param("playerId")int playerId);
	
	List<GamePlayerLog> getGameRecord(int playerId);
}
