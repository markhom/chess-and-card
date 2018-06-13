package com.linyun.common.action.impl;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.linyun.common.action.DomainConfigAction;
import com.linyun.common.entity.DomainConfig;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.DomainConfigMapper;

/**
*  @Author walker
*  @Since 2018年5月15日
**/

public class DomainConfigActionImpl implements DomainConfigAction
{

	@Override
	public List<DomainConfig> getDomainNames(int platformId) 
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			DomainConfigMapper domainConfigMapper = session.getMapper(DomainConfigMapper.class);
			List<DomainConfig> configs = domainConfigMapper.getDomainNames(platformId);
			return configs;
		}
		finally
		{
			if (session != null)
			{
				session.close();
			}
		}
	}

	@Override
	public void deleteDomainName(int id) 
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			DomainConfigMapper domainConfigMapper = session.getMapper(DomainConfigMapper.class);
			domainConfigMapper.deleteDomain(id);
			session.commit();
		}
		finally
		{
			if (session != null)
			{
				session.close();
			}
		}
	}

	@Override
	public void addDomainName(DomainConfig d) 
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			DomainConfigMapper domainConfigMapper = session.getMapper(DomainConfigMapper.class);
			domainConfigMapper.addDomainName(d);
			session.commit();
		}
		finally
		{
			if (session != null)
			{
				session.close();
			}
		}
		
	}

}
