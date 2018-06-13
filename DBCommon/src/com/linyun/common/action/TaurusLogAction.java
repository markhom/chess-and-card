package com.linyun.common.action;

import java.util.List;

import com.linyun.common.entity.TaurusLog;

public interface TaurusLogAction {
	
	//新增一条房间游戏记录
	void  addGameRecord(TaurusLog u);
	 
	//根据房间号获取最新的一条记录
	TaurusLog getOneRecord(int roomNum);
	 
	//更新游戏记录的局数和局数索引
	void updateRecord(TaurusLog log);
	
	 //玩家拉取最近三天的战绩
	 List<TaurusLog> getUserGameRecard(String userId);
	 
	 //检查表里面t_game_log三天内是否有生成重复的房间号,确保房间号唯一
	 List<TaurusLog> getUserOnlyRoomNumInFourDays(int roomNum);
	 
	//通过房间号找到TaurusLog对象(3天内任意一条)
	 TaurusLog getUserGameLogByRoomNum(int roomNum);
	
}
