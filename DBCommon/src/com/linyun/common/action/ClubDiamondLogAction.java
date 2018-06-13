package com.linyun.common.action;

import com.linyun.common.entity.ClubDiamondLog;

public interface ClubDiamondLogAction {
	
	//俱乐部玩家创建牌局游戏，消费俱乐部群主钻石的记录
	void  addOneRecordwithCostDiamond(ClubDiamondLog cdl);
	
}
