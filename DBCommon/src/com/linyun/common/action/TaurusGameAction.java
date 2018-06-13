package com.linyun.common.action;

public interface TaurusGameAction 
{
	//是否需要重连
	boolean isNeedReconnect(String userId);
	
	//冻结玩家时，玩家是否进入好友场游戏
	void bindUserTable(String userId);
	
	//统计好友场的在线人数
	int addOnlineCount();

}
