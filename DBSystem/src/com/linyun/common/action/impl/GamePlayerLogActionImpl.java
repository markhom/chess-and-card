package com.linyun.common.action.impl;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.linyun.common.action.GamePlayerLogAction;
import com.linyun.common.entity.GamePlayerLog;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.GamePlayerLogMapper;

/**
*  @Author walker
*  @Since 2018年4月20日
**/

public class GamePlayerLogActionImpl implements GamePlayerLogAction
{

	@Override
	public void addGamePlayerLog(GamePlayerLog g) 
	{
		SqlSession session = null; 
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			GamePlayerLogMapper gamePlayerLogMapper = session.getMapper(GamePlayerLogMapper.class);
			gamePlayerLogMapper.addGamePlayerLog(g);
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
	public List<GamePlayerLog> getAllPlayerLog(int clubId, int roomNum) 
	{
		SqlSession session = null; 
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			GamePlayerLogMapper gamePlayerLogMapper = session.getMapper(GamePlayerLogMapper.class);
			List<GamePlayerLog> allPlayerLog = gamePlayerLogMapper.getAllPlayerLog(clubId, roomNum);
			return allPlayerLog;
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
	public void updatePlayerLog(int score,int result, int clubId, int roomNum, int playerId) 
	{
		
		SqlSession session = null; 
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			GamePlayerLogMapper gamePlayerLogMapper = session.getMapper(GamePlayerLogMapper.class);
			gamePlayerLogMapper.updatePlayerLog(score,result, clubId, roomNum, playerId);
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
	public void updatePlayerBuyScoreHistory(int buyscore, int clubId, int roomNum, int playerId) 
	{
		SqlSession session = null; 
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			GamePlayerLogMapper gamePlayerLogMapper = session.getMapper(GamePlayerLogMapper.class);
			gamePlayerLogMapper.updatePlayerBuyscoreHistory(buyscore, clubId, roomNum, playerId);
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
	public GamePlayerLog getPlayerLogById(int clubId, int roomNum, int playerId) 
	{
		SqlSession session = null; 
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			GamePlayerLogMapper gamePlayerLogMapper = session.getMapper(GamePlayerLogMapper.class);
			GamePlayerLog g = gamePlayerLogMapper.getPlayerLogById(clubId, roomNum, playerId);
			return g;
		}catch(Exception e)
		{
			e.printStackTrace();
			return null;
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
	public void insertPlayerLogBatch(List<GamePlayerLog> list) 
	{
		SqlSession session = null; 
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			GamePlayerLogMapper gamePlayerLogMapper = session.getMapper(GamePlayerLogMapper.class);
			gamePlayerLogMapper.insertPlayerLogBatch(list);
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
	public List<Integer> getAllPlayerIds(int clubId, int roomNum) 
	{
		SqlSession session = null; 
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			GamePlayerLogMapper gamePlayerLogMapper = session.getMapper(GamePlayerLogMapper.class);
			List<Integer> allPlayerLog = gamePlayerLogMapper.getAllPlayerIds(clubId, roomNum);
			return allPlayerLog;
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
	public List<GamePlayerLog> getGameRecord(int playerId) 
	{
		SqlSession session = null; 
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			GamePlayerLogMapper gamePlayerLogMapper = session.getMapper(GamePlayerLogMapper.class);
			List<GamePlayerLog> allPlayerLog = gamePlayerLogMapper.getGameRecord(playerId);
			return allPlayerLog;
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
