package com.linyun.common.action;

import com.linyun.common.entity.ClubOperateLog;

public interface ClubOperateLogAction {
	
	//非俱乐部成员申请加入俱乐部,以及俱乐部成员操作生成的操作记录
	void  addOneClubOperateLog(ClubOperateLog col);
	

}
