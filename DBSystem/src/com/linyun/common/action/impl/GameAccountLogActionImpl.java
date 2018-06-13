package com.linyun.common.action.impl;

import org.apache.ibatis.session.SqlSession;

import com.linyun.common.action.GameAccountLogAction;
import com.linyun.common.entity.GameAccountLog;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.GameAccountLogMapper;

/**
*  @Author walker
*  @Since 2018年5月31日
**/

public class GameAccountLogActionImpl implements GameAccountLogAction
{

	@Override
	public void addGameAccountLog(GameAccountLog g) 
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			GameAccountLogMapper mapper = session.getMapper(GameAccountLogMapper.class);
			mapper.addGameAccountLog(g);
			session.commit();
			
		}finally
		{
			if(session != null)
			{
				session.commit();
			}
		}
	}

}
