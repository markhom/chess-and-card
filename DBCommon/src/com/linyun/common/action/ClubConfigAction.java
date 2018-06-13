package com.linyun.common.action;

import java.util.List;

public interface ClubConfigAction {
	
	//更新club配置表的配置的具体值
	void updateClubConfigByType(int count,int type);
	
	//查询配置表中所有的值
	List<Integer> getValueFromConfig();

}
