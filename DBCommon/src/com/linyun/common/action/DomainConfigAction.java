package com.linyun.common.action;

import java.util.List;

import com.linyun.common.entity.DomainConfig;

/**
*  @Author walker
*  @Since 2018年5月15日
**/

public interface DomainConfigAction 
{
	List<DomainConfig> getDomainNames(int platformId);
	
	void deleteDomainName(int id);
	
	void addDomainName(DomainConfig d);

}
