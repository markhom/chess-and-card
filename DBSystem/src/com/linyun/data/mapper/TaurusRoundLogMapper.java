package com.linyun.data.mapper;

import java.util.List;

import com.linyun.common.entity.TaurusRoundLog;

public interface TaurusRoundLogMapper {
	
	//每一局牌中玩家的手牌的结果记录
	void addOneRoundRecord(TaurusRoundLog u);
	
	//根据房间号得到一场（10 or 20）局的玩家游戏的手牌的集合
	List<TaurusRoundLog> getUserEveryRoundHandCardLog(int roomNum);
		
	//通过每局游戏的唯一索引得到一局手牌记录的对象
	TaurusRoundLog getHandCardByRoomIndex(String roomIndex);

}
