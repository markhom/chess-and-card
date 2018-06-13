package com.linyun.common.action.impl;

import org.apache.ibatis.session.SqlSession;

import com.linyun.common.action.GameRoundLogAction;
import com.linyun.common.entity.GameRoundLog;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.ClubMapper;
import com.linyun.data.mapper.GameRoundLogMapper;

/**
*  @Author walker
*  @Since 2018年4月22日
**/

public class GameRoundLogActionImpl implements GameRoundLogAction 
{

	@Override
	public void addGameRoundLogAction(GameRoundLog g) 
	{
		 SqlSession session = null ;
		 try
		 {
			 session = SqlSessionFactoryUtil.ssf.openSession();
			 GameRoundLogMapper gameRoundLogMapper = session.getMapper(GameRoundLogMapper.class);
			 gameRoundLogMapper.addGameRoundLog(g);
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

	@Override
	public GameRoundLog getRoundDetails(int clubId, int roomNum, int round) 
	{
		 SqlSession session = null ;
		 try
		 {
			 session = SqlSessionFactoryUtil.ssf.openSession();
			 GameRoundLogMapper gameRoundLogMapper = session.getMapper(GameRoundLogMapper.class);
			 GameRoundLog roundDetails = gameRoundLogMapper.getRoundDetails(clubId, roomNum, round);
			 return roundDetails;
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
