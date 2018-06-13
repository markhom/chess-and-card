package com.linyun.common.action.impl;

import org.apache.ibatis.session.SqlSession;

import com.linyun.common.action.OnlineTaurusAction;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.OnlineTaurusMapper;
import com.linyun.data.server.ActionAware;

public class OnlineTaurusActionImpl extends ActionAware implements OnlineTaurusAction
{

	@Override
	public void updateOnlineCount(int count)
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			OnlineTaurusMapper mapper = session.getMapper(OnlineTaurusMapper.class);
			mapper.updateOnlineCount(count);
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
