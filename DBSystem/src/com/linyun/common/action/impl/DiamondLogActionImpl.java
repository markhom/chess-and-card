package com.linyun.common.action.impl;

import org.apache.ibatis.session.SqlSession;

import com.linyun.common.action.DiamondLogAction;
import com.linyun.common.entity.DiamondLog;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.DiamondLogMapper;

public class DiamondLogActionImpl implements DiamondLogAction
{
	@Override
	public void addOneRecord(DiamondLog log)
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			DiamondLogMapper mapper = session.getMapper(DiamondLogMapper.class);
			mapper.addOneRecord(log);
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
