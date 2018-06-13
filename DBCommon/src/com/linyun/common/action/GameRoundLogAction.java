package com.linyun.common.action;

import com.linyun.common.entity.GameRoundLog;

/**
*  @Author walker
*  @Since 2018年4月22日
**/

public interface GameRoundLogAction 
{
   public void addGameRoundLogAction(GameRoundLog g);
   
   public GameRoundLog getRoundDetails(int clubId, int roomNum, int round);
}
