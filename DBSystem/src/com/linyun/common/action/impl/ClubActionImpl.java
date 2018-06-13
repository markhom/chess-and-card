package com.linyun.common.action.impl;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.linyun.common.action.ClubAction;
import com.linyun.common.entity.Club;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.ClubMapper;

public class ClubActionImpl implements ClubAction{
	@Override
	public void createClub(Club c) 
	{
		 SqlSession session = null ;
		 try
		 {
			 session = SqlSessionFactoryUtil.ssf.openSession();
			 ClubMapper clubMapper = session.getMapper(ClubMapper.class);
			 clubMapper.createClub(c);
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
	public Club selectPrivateClub(int clubId)
	{
		SqlSession session = null ;
		Club privateClub = null ; 
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			ClubMapper clubMapper = session.getMapper(ClubMapper.class);
			privateClub = clubMapper.getClub(clubId);
			return privateClub;
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
	public List<Club> getAllClub()
	{
		SqlSession session = null ;
		List<Club> allClubs = null ;
		try 
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			ClubMapper clubMapper = session.getMapper(ClubMapper.class);
			allClubs = clubMapper.getAllClub();
		    return allClubs;
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
	public Club getClubByCreatorId(int userId)
	{   
		SqlSession session = null ;
		Club club = null ; 
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			ClubMapper clubMapper = session.getMapper(ClubMapper.class);
			club = clubMapper.getClubByCreatorId(userId);
			return club;
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
	public void updateClubPercent(int percent, int clubId) 
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			ClubMapper clubMapper = session.getMapper(ClubMapper.class);
			clubMapper.updateClubPercent(percent, clubId);
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
	public void updateClubScorePool(int scorePool, int clubId) 
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			ClubMapper clubMapper = session.getMapper(ClubMapper.class);
			clubMapper.updateClubScorePool(scorePool, clubId);
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
	public void updateClubRate(int expandRate, int scoreRate, int clubId) 
	{
	   
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			ClubMapper clubMapper = session.getMapper(ClubMapper.class);
			clubMapper.updateClubRate(expandRate, scoreRate, clubId);
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
	public void updateClubCoinPool(int clubId, int coinPool) 
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			ClubMapper clubMapper = session.getMapper(ClubMapper.class);
			clubMapper.updateClubCoinPool(clubId, coinPool);
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
