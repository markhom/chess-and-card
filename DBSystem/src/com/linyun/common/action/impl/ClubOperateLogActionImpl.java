package com.linyun.common.action.impl;

import org.apache.ibatis.session.SqlSession;

import com.linyun.common.action.ClubOperateLogAction;
import com.linyun.common.entity.ClubOperateLog;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.ClubOperateLogMapper;

public class ClubOperateLogActionImpl implements ClubOperateLogAction {

	@Override
	public void addOneClubOperateLog(ClubOperateLog col)
	{
		SqlSession session = null ;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			ClubOperateLogMapper colMapper = session.getMapper(ClubOperateLogMapper.class);
			colMapper.addOneClubOperateLog(col);
			session.commit();
		} 
		finally
		{
			if(session != null)
			{
				session.close();
			}
		}

	}

}
