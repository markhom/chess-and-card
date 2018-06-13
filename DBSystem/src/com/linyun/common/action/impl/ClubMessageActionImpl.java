package com.linyun.common.action.impl;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.linyun.common.action.ClubMessageAction;
import com.linyun.common.entity.ClubMessage;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.ClubMessageMapper;

public class ClubMessageActionImpl implements ClubMessageAction {

	@Override
	public void addOneClubMessage(ClubMessage cm)
	{
         SqlSession session = null ;
         try
         {
        	 session = SqlSessionFactoryUtil.ssf.openSession();
        	 ClubMessageMapper cmMapper = session.getMapper(ClubMessageMapper.class);
        	 cmMapper.addOneClubMessage(cm);
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
	public List<ClubMessage> getAllMessage(int userId)
	{
		SqlSession session = null ;
		List<ClubMessage> msgList = null ;
        try
        {
       	 session = SqlSessionFactoryUtil.ssf.openSession();
       	 ClubMessageMapper cmMapper = session.getMapper(ClubMessageMapper.class);
       	 msgList = cmMapper.getAllMessage(userId);
       	return msgList;
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
	public ClubMessage getMessage(int msgId)
	{
		SqlSession session = null ;
        try
        {
	       	session = SqlSessionFactoryUtil.ssf.openSession();
	       	ClubMessageMapper cmMapper = session.getMapper(ClubMessageMapper.class);
	       	return cmMapper.getMessage(msgId);
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
	public void readMessage(int msgId)
	{
		SqlSession session = null ;
        try
        {
	       	session = SqlSessionFactoryUtil.ssf.openSession();
	       	ClubMessageMapper cmMapper = session.getMapper(ClubMessageMapper.class);
	       	cmMapper.readMessage(msgId);
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
	public List<ClubMessage> getAllTimeoutNoHandleMsg()
	{
		SqlSession session = null ;
        try
        {
	       	session = SqlSessionFactoryUtil.ssf.openSession();
	       	ClubMessageMapper cmMapper = session.getMapper(ClubMessageMapper.class);
	       	List<ClubMessage> list = cmMapper.getAllTimeoutNoHandleMsg();
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
	public void delMsg(int msgId)
	{
		SqlSession session = null ;
        try
        {
	       	session = SqlSessionFactoryUtil.ssf.openSession();
	       	ClubMessageMapper cmMapper = session.getMapper(ClubMessageMapper.class);
	       	cmMapper.delMsg(msgId);
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
	public void bakMsg()
	{
		SqlSession session = null ;
        try
        {
	       	session = SqlSessionFactoryUtil.ssf.openSession();
	       	ClubMessageMapper cmMapper = session.getMapper(ClubMessageMapper.class);
	       	cmMapper.bakClubMsg();
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
	public void delTimeoutClubMsg() 
	{
		SqlSession session = null ;
        try
        {
	       	session = SqlSessionFactoryUtil.ssf.openSession();
	       	ClubMessageMapper cmMapper = session.getMapper(ClubMessageMapper.class);
	       	cmMapper.delTimeoutClubMsg();
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
	public List<ClubMessage> selectAllApplyMsgInDelClub(int clubId)
	{
		SqlSession session = null ;
        try
        {
	       	session = SqlSessionFactoryUtil.ssf.openSession();
	       	ClubMessageMapper cmMapper = session.getMapper(ClubMessageMapper.class);
	       	List<ClubMessage> msgList = cmMapper.selectAllApplyMsgInDelClub(clubId);
	       	return msgList;
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
	public ClubMessage isApplyJoinClub(int clubId, int applyId)
	{
		SqlSession session = null ;
        try
        {
	       	session = SqlSessionFactoryUtil.ssf.openSession();
	       	ClubMessageMapper cmMapper = session.getMapper(ClubMessageMapper.class);
	       	ClubMessage clubMessage = cmMapper.isApplyJoinClub(clubId,applyId);
	       	return clubMessage;
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
	public List<ClubMessage> getUserUnreadMsg(int userId)
	{
		SqlSession session = null ;
        try
        {
	       	session = SqlSessionFactoryUtil.ssf.openSession();
	       	ClubMessageMapper cmMapper = session.getMapper(ClubMessageMapper.class);
	       	List<ClubMessage> list = cmMapper.getUserUnreadMsg(userId);
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

}
