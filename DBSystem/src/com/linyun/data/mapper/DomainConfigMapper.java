package com.linyun.data.mapper;

import java.util.List;

import com.linyun.common.entity.DomainConfig;

/**
*  @Author walker
*  @Since 2018年5月15日
**/

public interface DomainConfigMapper 
{

	//获取对应平台的域名
	List<DomainConfig> getDomainNames(int platformId);
	
	//删除无效域名
	void deleteDomain(int id);
	
	//新增域名
	void addDomainName(DomainConfig d);
	
}
