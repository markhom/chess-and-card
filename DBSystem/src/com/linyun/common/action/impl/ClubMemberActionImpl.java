package com.linyun.common.action.impl;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.linyun.bottom.cached.RedisResource;
import com.linyun.common.action.ClubMemberAction;
import com.linyun.common.entity.ClubMember;
import com.linyun.common.entity.ClubMessage;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.ClubMemberMapper;
import com.linyun.data.mapper.ClubMessageMapper;

public class ClubMemberActionImpl implements ClubMemberAction {

	@Override
	public void addOnePrivateClubMember(ClubMember cm)
	{
		SqlSession session = null ;
        try
        {
       	 session = SqlSessionFactoryUtil.ssf.openSession();
       	 ClubMemberMapper cmMapper = session.getMapper(ClubMemberMapper.class);
       	 cmMapper.addOneClubMember(cm);
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
	public void exitPrivateClub(int userId, int clubId)
	{
		SqlSession session = null ;
        try
        {
       	 session = SqlSessionFactoryUtil.ssf.openSession();
       	 ClubMemberMapper cmMapper = session.getMapper(ClubMemberMapper.class);
       	 cmMapper.delClubMember(userId,clubId);
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
	public List<ClubMember> getAllMember(int clubId)
	{
		SqlSession session = null ;
        try
        {
	       	 session = SqlSessionFactoryUtil.ssf.openSession();
	       	 ClubMemberMapper cmMapper = session.getMapper(ClubMemberMapper.class);
	       	 List<ClubMember> list = cmMapper.getAllMember(clubId);
	       	 return list;
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
	public List<String> getAllClubId(int userId)
	{
		SqlSession session = null ;
        try
        {
	       	 session = SqlSessionFactoryUtil.ssf.openSession();
	       	 ClubMemberMapper cmMapper = session.getMapper(ClubMemberMapper.class);
	       	 List<String> list = cmMapper.getAllClubId(userId);
	       	 if(!list.isEmpty())
	       	 {
	       		 RedisResource.flushUserClubData(String.valueOf(userId), list);
	       	 }
	       	 return list;
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
	public ClubMember getClubMemberByClubId(int clubId, int userId)
	{
		SqlSession session = null ;
        try
        {
	       	 session = SqlSessionFactoryUtil.ssf.openSession();
	       	 ClubMemberMapper cmMapper = session.getMapper(ClubMemberMapper.class);
	       	 ClubMember cm = cmMapper.getClubMemberByClubId(clubId,userId);
	       	 return cm;
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
	public void addCostDiamondLimit(int clubId, int userId, int diamondLimit, ClubMessage clubMessage)
	{

		SqlSession session = null ;
        try
        {
	       	 session = SqlSessionFactoryUtil.ssf.openSession();
	       	 ClubMemberMapper cmMapper = session.getMapper(ClubMemberMapper.class);
	       	 cmMapper.addDiamondLimit(clubId,userId,diamondLimit);
	       	 
	       	ClubMessageMapper clubMessageMapper = session.getMapper(ClubMessageMapper.class);//2
			clubMessageMapper.addOneClubMessage(clubMessage);//2
			
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
	public void updateClubMemberScoreLimit(int clubId, int userId, int scoreLimit, ClubMessage clubMessage) 
	{
	    SqlSession session = null;
	    try
	    {
	    	session = SqlSessionFactoryUtil.ssf.openSession();
	       	ClubMemberMapper cmMapper = session.getMapper(ClubMemberMapper.class);
	   	    ClubMember cm = cmMapper.getClubMemberByClubId(clubId,userId);
	   	    if(scoreLimit > 0)
	   	    {
	   	      cmMapper.updateClubMemberScoreLimit(clubId, userId, cm.getScoreLimit()+scoreLimit, cm.getCurrentScore()+scoreLimit);
	   	    }else
	   	    {
	   	      cmMapper.updateClubMemberScoreLimit(clubId, userId, cm.getScoreLimit(),cm.getCurrentScore()+scoreLimit);
	   	    }
	   	  
	   		ClubMessageMapper clubMessageMapper = session.getMapper(ClubMessageMapper.class);//2
			clubMessageMapper.addOneClubMessage(clubMessage);//2
			
			session.commit();
	    }catch(Exception e)
	    {
	    	e.printStackTrace();
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
	public void updateClubMemberCurrentScore(int clubId, int userId, int currentScore) 
	{
		SqlSession session = null;
	    try
	    {
	    	session = SqlSessionFactoryUtil.ssf.openSession();
	       	ClubMemberMapper cmMapper = session.getMapper(ClubMemberMapper.class);
	   	    cmMapper.updateClubMemberCurrentScore(clubId, userId, currentScore);
	    	
			session.commit();
	    }catch(Exception e)
	    {
	    	e.printStackTrace();
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
	public void updateClubMemberCoinLimit(int clubId, int userId, int coinLimit, ClubMessage clubMessage) 
	{
		SqlSession session = null;
	    try
	    {
	    	session = SqlSessionFactoryUtil.ssf.openSession();
	       	ClubMemberMapper cmMapper = session.getMapper(ClubMemberMapper.class);
	   	    cmMapper.updateClubMemberCoinLimit(clubId, userId, coinLimit);
	   	    
	   	    ClubMessageMapper clubMessageMapper = session.getMapper(ClubMessageMapper.class);//2
			clubMessageMapper.addOneClubMessage(clubMessage);//2
			session.commit();
	    }catch(Exception e)
	    {
	    	e.printStackTrace();
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
