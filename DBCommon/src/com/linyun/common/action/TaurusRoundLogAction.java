package com.linyun.common.action;

import java.util.List;

import com.linyun.common.entity.TaurusRoundLog;

public interface TaurusRoundLogAction {
	
	 //增加一局游戏结果记录
	 void addOneRoundRecord(TaurusRoundLog u);
	 
	//根据房间号得到玩家这一场（10 or 20局）的游戏记录
	List<TaurusRoundLog> getUserEveryRoundHandCardLog(int roomNum);
		 
	//通过每局游戏的唯一索引得到一局手牌记录的对象
	TaurusRoundLog getHandCardByRoomIndex(String roomIndex);
    
}
