package com.linyun.common.action.impl;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.linyun.common.action.ClubRoomLogAction;
import com.linyun.common.entity.ClubRoomLog;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.ClubRoomLogMapper;

/**
*  @Author walker
*  @Since 2018年4月14日
**/

public class ClubRoomLogActionImpl implements ClubRoomLogAction
{

	@Override
	public void addGameRoomLog(ClubRoomLog c) 
	{
		SqlSession session = null ;
		 try
		 {
			 session = SqlSessionFactoryUtil.ssf.openSession();
			 ClubRoomLogMapper clubRoomLogMapper = session.getMapper(ClubRoomLogMapper.class);
			 clubRoomLogMapper.addClubRoomLog(c);
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
	public List<ClubRoomLog> getOnlyRoomNumInfourDays()
	{
		
		SqlSession session = null ;
		 try
		 {
			 session = SqlSessionFactoryUtil.ssf.openSession();
			 ClubRoomLogMapper clubRoomLogMapper = session.getMapper(ClubRoomLogMapper.class);
			 List<ClubRoomLog> result = clubRoomLogMapper.getOnlyRoomNumInfourDays();
			 return result;
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
	public void updatePlayedRound(int roomNum, int playedRound) 
	{
		SqlSession session = null ;
		 try
		 {
			 session = SqlSessionFactoryUtil.ssf.openSession();
			 ClubRoomLogMapper clubRoomLogMapper = session.getMapper(ClubRoomLogMapper.class);
		     clubRoomLogMapper.updateRoomRound(roomNum, playedRound);
		     
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
	public ClubRoomLog getClubRoomLogByRoomId(int roomNum) 
	{
		SqlSession session = null ;
		 try
		 {
			 session = SqlSessionFactoryUtil.ssf.openSession();
			 ClubRoomLogMapper clubRoomLogMapper = session.getMapper(ClubRoomLogMapper.class);
			 ClubRoomLog clubRoomLog = clubRoomLogMapper.getClubRoomLogByRoomId(roomNum);
			 return clubRoomLog;
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
