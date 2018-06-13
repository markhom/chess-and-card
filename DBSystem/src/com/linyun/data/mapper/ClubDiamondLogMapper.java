package com.linyun.data.mapper;

import com.linyun.common.entity.ClubDiamondLog;

public interface ClubDiamondLogMapper {
	
	//俱乐部里面玩家开启牌局，消耗创始人钻石记录
	void addOneRecordwithCostDiamond(ClubDiamondLog cdl);

}
