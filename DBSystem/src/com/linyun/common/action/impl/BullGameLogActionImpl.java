package com.linyun.common.action.impl;

import org.apache.ibatis.session.SqlSession;

import com.linyun.common.action.BullGameLogAction;
import com.linyun.common.entity.BullGameLog;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.BullGameLogMapper;

/**
*  @Author walker
*  @Since 2018年5月31日
**/

public class BullGameLogActionImpl implements BullGameLogAction
{

	@Override
	public void addBullGameLog(BullGameLog b)
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			BullGameLogMapper mapper = session.getMapper(BullGameLogMapper.class);
			mapper.addBullGameLog(b);
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
