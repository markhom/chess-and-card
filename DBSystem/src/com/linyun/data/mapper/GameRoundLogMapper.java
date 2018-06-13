package com.linyun.data.mapper;

import org.apache.ibatis.annotations.Param;

import com.linyun.common.entity.GameRoundLog;

/**
*  @Author walker
*  @Since 2018年4月13日
**/

public interface GameRoundLogMapper 
{
	void addGameRoundLog(GameRoundLog g);
	
	GameRoundLog getRoundDetails(@Param("clubId") int clubId, @Param("roomNum") int roomNum ,@Param("round") int round);

}
