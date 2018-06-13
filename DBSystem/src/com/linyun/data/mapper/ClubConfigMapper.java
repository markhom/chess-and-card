package com.linyun.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface ClubConfigMapper {
	
	//更新club配置表的信息
	void updateClubConfigByType(@Param("count")int count ,@Param("type")int type);
	
	//从配置表中查询所有的值
	List<Integer> getValueFromConfig();

}
