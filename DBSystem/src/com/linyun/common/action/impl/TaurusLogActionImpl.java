package com.linyun.common.action.impl;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.linyun.common.action.TaurusLogAction;
import com.linyun.common.entity.TaurusLog;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.TaurusLogMapper;
import com.linyun.data.server.ActionAware;

public class TaurusLogActionImpl extends ActionAware implements TaurusLogAction {

	@Override
	public void addGameRecord(TaurusLog u)
	{
		SqlSession session = null; 
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			TaurusLogMapper userGameMapper = session.getMapper(TaurusLogMapper.class);
			userGameMapper.addGameRecord(u);
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
	public TaurusLog getOneRecord(int roomNum)
	{
		SqlSession session = null; 
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			TaurusLogMapper userGameMapper = session.getMapper(TaurusLogMapper.class);
			TaurusLog log = userGameMapper.getOneRecord(roomNum);
			return log;
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
	public void updateRecord(TaurusLog u)
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			TaurusLogMapper userGameMapper = session.getMapper(TaurusLogMapper.class);
			userGameMapper.updateRecord(u);
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
	public List<TaurusLog> getUserGameRecard(String userId)
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			TaurusLogMapper userGameMapper = session.getMapper(TaurusLogMapper.class);
			TaurusLog u = new TaurusLog();
			u.setUserId(userId);
			List<TaurusLog> userGameRecardList = userGameMapper.getUserGameRecard(u);
			return userGameRecardList;
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
	public List<TaurusLog> getUserOnlyRoomNumInFourDays(int roomNum)
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			TaurusLogMapper userGameMapper = session.getMapper(TaurusLogMapper.class);
			List<TaurusLog> userGameRecardList = userGameMapper.getUserOnlyRoomNumInFourDays(roomNum);
			return userGameRecardList;
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
	public TaurusLog getUserGameLogByRoomNum(int roomNum)
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			TaurusLogMapper userGameMapper = session.getMapper(TaurusLogMapper.class);
			TaurusLog taurusLog = userGameMapper.getUserGameLogByRoomNum(roomNum);
			return taurusLog;
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
