package com.linyun.common.action.impl;

import org.apache.ibatis.session.SqlSession;

import com.linyun.bottom.cached.RedisResource;
import com.linyun.common.action.ClubCommonAction;
import com.linyun.common.entity.Club;
import com.linyun.common.entity.ClubMember;
import com.linyun.common.entity.ClubMessage;
import com.linyun.common.entity.ClubOperateLog;
import com.linyun.common.entity.User;
import com.linyun.common.taurus.club.eum.ClubOperateType;
import com.linyun.common.taurus.club.eum.ClubPosition;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.ClubMapper;
import com.linyun.data.mapper.ClubMemberMapper;
import com.linyun.data.mapper.ClubMessageMapper;
import com.linyun.data.mapper.ClubOperateLogMapper;
import com.linyun.data.mapper.UserMapper;


public class ClubCommonActionImpl implements ClubCommonAction
{
	@Override
	public void CreatorCreateClub(Club club,ClubMessage clubMsg,ClubMember clubMember)
	{
		/**1. 增加一条俱乐部操作记录信息
		 * 2. 生成一条消息  针对创建者
		 * 3. 增加俱乐部 
		 * 4. 增加俱乐部成员表的记录
		 * 5. 玩家拥有的俱乐部数量+1
		 * */
		SqlSession session = null ;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			 
			ClubOperateLog col = new ClubOperateLog();//1
			col.setClubId(club.getClubId());
			col.setOperatorId(club.getCreatorId());
			col.setClubPosition(ClubPosition.POSITION_CREATOR.value);
			col.setOperateType(ClubOperateType.TYPE_CREATE.value);
			col.setRemark("玩家:userId="+club.getCreatorId()+"创建私人俱乐部clubId="+club.getClubId());
			ClubOperateLogMapper clubOperateLogMapper = session.getMapper(ClubOperateLogMapper.class);
			clubOperateLogMapper.addOneClubOperateLog(col);//1 
			
			ClubMessageMapper clubMessageMapper = session.getMapper(ClubMessageMapper.class);//2
			clubMessageMapper.addOneClubMessage(clubMsg);//2
			
			ClubMapper clubMapper = session.getMapper(ClubMapper.class);//3
			clubMapper.createClub(club);//3
			
			ClubMemberMapper clubMemberMapper = session.getMapper(ClubMemberMapper.class);//4
			clubMemberMapper.addOneClubMember(clubMember);//4
			
			UserMapper userMapper = session.getMapper(UserMapper.class);//5
			userMapper.incrementUserJoinClubCount(club.getCreatorId());//5
			session.commit();
			
			//设置玩家redis相关
			int userId = club.getCreatorId();
			User user = userMapper.getExistUser(userId);
			RedisResource.set(UserActionImpl.generateId(String.valueOf(userId)), user);
			
			//创建者加入俱乐部，插入数据到redis
			RedisResource.setUserClubData(String.valueOf(userId), String.valueOf(club.getClubId()));
			
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
	public void CreatorDeleteClub(int creatorId, int clubId, ClubMessage msgCreator)
	{
		/**1. 删除俱乐部成员表的记录
		 * 2. 删除俱乐部
		 * 3. 生成一条消息  针对创建者
		 * 4. 增加一条俱乐部操作记录信息
		 * 5. 玩家拥有的私人俱乐部数量-1
		 * */
		SqlSession session = null ;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			ClubOperateLogMapper clubOperateLogMapper = session.getMapper(ClubOperateLogMapper.class); 
			
			ClubOperateLog log = new ClubOperateLog();//1
			log.setClubId(clubId);
			log.setOperatorId(creatorId);
			log.setClubPosition(ClubPosition.POSITION_CREATOR.value);
			log.setOperateType(ClubOperateType.TYPE_DELETE.value);
			log.setRemark("创建者("+creatorId+")删除俱乐部"); 
			clubOperateLogMapper.addOneClubOperateLog(log); //1
			
			//生成一条消息记录
			ClubMessageMapper clubMessageMapper = session.getMapper(ClubMessageMapper.class);//2
			clubMessageMapper.addOneClubMessage(msgCreator);//2
			
			//将群主添加到俱乐部成员列表中
			ClubMemberMapper clubMemberMapper = session.getMapper(ClubMemberMapper.class);//3
			clubMemberMapper.delClubMember(creatorId, clubId);//3
			
			//生成私人俱乐部
			ClubMapper clubMapper = session.getMapper(ClubMapper.class);//4
			clubMapper.delClub(clubId);//4
			
			UserMapper userMapper = session.getMapper(UserMapper.class);//5
			userMapper.decrementUserJoinClubCount(creatorId);//5
			session.commit();
			
			//设置玩家redis相关
			User user = userMapper.getExistUser(creatorId);
			RedisResource.set(UserActionImpl.generateId(String.valueOf(creatorId)), user);
			
			//club redis删除
			RedisResource.delUserClubInfo(String.valueOf(creatorId), String.valueOf(clubId));
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
	public void UserApplyJoinClub(int userId, int clubId, ClubMessage msgCretor)
	{
		/**
		 * 1.生成一条俱乐部操作记录
		 * 2.玩家正在申请的俱乐部个数+1
		 * 3.生成一条针对俱乐部创建者的消息
		 * */
		SqlSession session = null ;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			
			ClubOperateLog log = new ClubOperateLog();//1
			log.setClubId(clubId);
			log.setClubPosition(ClubPosition.POSITION_NOT_MEMBER.value);
			log.setOperateType(ClubOperateType.TYPE_APPLY_JOIN.value);
			log.setOperatorId(userId);
			log.setRemark("玩家(" + userId + ")申请加入俱乐部");
			ClubOperateLogMapper logMapper = session.getMapper(ClubOperateLogMapper.class);
			logMapper.addOneClubOperateLog(log);//1
			
			UserMapper userMapper = session.getMapper(UserMapper.class);//2
			userMapper.incrementUserApplyClubCount(userId);//2
			
			ClubMessageMapper msgMapper = session.getMapper(ClubMessageMapper.class);
			msgMapper.addOneClubMessage(msgCretor);
			session.commit();
			
			//设置玩家redis相关
			User user = userMapper.getExistUser(userId);
			RedisResource.set(UserActionImpl.generateId(String.valueOf(userId)), user);
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
	public void MemberExitClub(int memberId, int clubId, ClubMessage msgMember, ClubMessage msgCreator)
	{
		/**
		 * 1. 俱乐部人数减少一个
		 * 2. 生成一条俱乐部操作记录
		 * 3. 生成两条未读消息记录  一条针对操作退出者  一条针对创建者
		 * 4. 在俱乐部成员表中删除记录
		 * 5. 玩家已加入的俱乐部个数-1
		 * */
		SqlSession session = null ;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			ClubMapper clubMapper = session.getMapper(ClubMapper.class);
			clubMapper.decrementClubCountOne(clubId); //1.
			
			ClubOperateLog log = new ClubOperateLog();//2
			log.setClubId(clubId);
			log.setClubPosition(ClubPosition.POSITION_MEMBER.value);
			log.setOperateType(ClubOperateType.TYPE_EXIT.value);
			log.setOperatorId(memberId);
			log.setRemark("玩家退出俱乐部");
			ClubOperateLogMapper logMapper = session.getMapper(ClubOperateLogMapper.class);
			logMapper.addOneClubOperateLog(log);//2
			
			ClubMessageMapper msgMapper = session.getMapper(ClubMessageMapper.class); //3
			msgMapper.addOneClubMessage(msgMember); 
			msgMapper.addOneClubMessage(msgCreator); //3
			
			ClubMemberMapper memberMapper = session.getMapper(ClubMemberMapper.class);//4
			memberMapper.delClubMember(memberId, clubId);//4
			
			UserMapper userMapper = session.getMapper(UserMapper.class);//5
			userMapper.decrementUserJoinClubCount(memberId);//5
			
			session.commit();
			
			//设置玩家redis相关
			User user = userMapper.getExistUser(memberId);
			RedisResource.set(UserActionImpl.generateId(String.valueOf(memberId)), user);
			
			//club redis删除
			RedisResource.delUserClubInfo(String.valueOf(memberId), String.valueOf(clubId));
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
	public void CreatorInviteUserJoinClub(int creatorId, int inviteId, int clubId, ClubMessage msgInvite, ClubMessage msgCreator, ClubMember member)
	{
		/**
		 * 1. 俱乐部人数新增一个
		 * 2. 生成一条俱乐部操作记录
		 * 3. 生成两条未读消息记录  一条针对被邀请者  一条针对创建者
		 * 4. 在俱乐部成员表中新增记录
		 * 5. 玩家已加入的俱乐部个数+1
		 * */
		SqlSession session = null ;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			ClubMapper clubMapper = session.getMapper(ClubMapper.class);
			clubMapper.incrementClubCountOne(clubId); //1.
			
			ClubOperateLog log = new ClubOperateLog();//2
			log.setClubId(clubId);
			log.setClubPosition(ClubPosition.POSITION_CREATOR.value);
			log.setOperateType(ClubOperateType.TYPE_INVITE_NEW_MEMBER.value);
			log.setOperatorId(creatorId);
			log.setRemark("俱乐部创建者邀请(" + inviteId + ")加入俱乐部");
			ClubOperateLogMapper logMapper = session.getMapper(ClubOperateLogMapper.class);
			logMapper.addOneClubOperateLog(log);//2
			
			ClubMessageMapper msgMapper = session.getMapper(ClubMessageMapper.class); //3
			msgMapper.addOneClubMessage(msgInvite); 
			msgMapper.addOneClubMessage(msgCreator); //3
			
			ClubMemberMapper memberMapper = session.getMapper(ClubMemberMapper.class);//4
			memberMapper.addOneClubMember(member);//4
			
			UserMapper userMapper = session.getMapper(UserMapper.class);//5
			userMapper.incrementUserJoinClubCount(inviteId);//5
			
			session.commit();
			
			//设置玩家redis相关
			User user = userMapper.getExistUser(inviteId);
			RedisResource.set(UserActionImpl.generateId(String.valueOf(inviteId)), user);
			
			//被邀请者加入俱乐部,数据插入缓存
			RedisResource.setUserClubData(String.valueOf(inviteId), String.valueOf(clubId));
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
	public void CreatorAgreeApplyJoinClub(int msgId, int creatorId, int clubId, int applyId, ClubMessage msgApply, ClubMessage msgCreator, ClubMember member)
	{
		/**
		 * 1. 俱乐部人数新增一个
		 * 2. 生成一条俱乐部操作记录
		 * 3. 生成两条未读消息记录  一条针对申请人  一条针对创建者
		 * 4. 在俱乐部成员表中新增记录
		 * 5. 申请者正在申请的俱乐部数量-1  已加入的俱乐部个数+1
		 * 6. 对已经处理的申请加入信息删除
		 * */
		SqlSession session = null ;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			ClubMapper clubMapper = session.getMapper(ClubMapper.class);
			clubMapper.incrementClubCountOne(clubId); //1.
			
			ClubOperateLog log = new ClubOperateLog();//2
			log.setClubId(clubId);
			log.setClubPosition(ClubPosition.POSITION_CREATOR.value);
			log.setOperateType(ClubOperateType.TYPE_HANDLE_AGREE_JOIN.value);
			log.setOperatorId(creatorId);
			log.setRemark("俱乐部创建者通过了加入申请(" + applyId + ")");
			ClubOperateLogMapper logMapper = session.getMapper(ClubOperateLogMapper.class);
			logMapper.addOneClubOperateLog(log);//2
			
			ClubMessageMapper msgMapper = session.getMapper(ClubMessageMapper.class); //3
			msgMapper.addOneClubMessage(msgApply); 
			msgMapper.addOneClubMessage(msgCreator); //3
			msgMapper.delMsg(msgId); //6
			
			ClubMemberMapper memberMapper = session.getMapper(ClubMemberMapper.class);//4
			memberMapper.addOneClubMember(member);//4
			
			UserMapper userMapper = session.getMapper(UserMapper.class);//5
			userMapper.decrementUserApplyClubCount(applyId);
			userMapper.incrementUserJoinClubCount(applyId);//5
			
			session.commit();
			
			//设置玩家redis相关
			User user = userMapper.getExistUser(applyId);
			RedisResource.set(UserActionImpl.generateId(String.valueOf(applyId)), user);
			
			//申请者加入俱乐部，数据写入缓存
			RedisResource.setUserClubData(String.valueOf(applyId), String.valueOf(clubId));
			
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
	public void CreatorRefuseApplyJoinClub(int msgId,int creatorId, int clubId, int applyId, ClubMessage msgApply, ClubMessage msgCreator)
	{
		/**
		 * 1. 生成一条俱乐部操作记录
		 * 2. 生成两条未读消息记录  一条针对申请人  一条针对创建者
		 * 3. 申请人正在申请的俱乐部数量 -1
		 * 4. 对已经处理的申请加入信息删除
		 * */
		SqlSession session = null ;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			
			ClubOperateLog log = new ClubOperateLog();//1
			log.setClubId(clubId);
			log.setClubPosition(ClubPosition.POSITION_CREATOR.value);
			log.setOperateType(ClubOperateType.TYPE_HANDLE_REFUSE_JOIN.value);
			log.setOperatorId(creatorId);
			log.setRemark("俱乐部创建者拒绝了加入申请(" + applyId + ")");
			ClubOperateLogMapper logMapper = session.getMapper(ClubOperateLogMapper.class);
			logMapper.addOneClubOperateLog(log);//1
			
			ClubMessageMapper msgMapper = session.getMapper(ClubMessageMapper.class); //2
			msgMapper.addOneClubMessage(msgApply); 
			msgMapper.addOneClubMessage(msgCreator); //2
			msgMapper.delMsg(msgId);//4
			
			UserMapper userMapper = session.getMapper(UserMapper.class);//3
			userMapper.decrementUserApplyClubCount(applyId);//3
			
			session.commit();
			
			//设置玩家redis相关
			User user = userMapper.getExistUser(applyId);
			RedisResource.set(UserActionImpl.generateId(String.valueOf(applyId)), user);
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
	public void CreatorKickClubMember(int creatorId, int clubId, int memberId, ClubMessage msgKick, ClubMessage msgCreator)
	{
		/**
		 * 1. 俱乐部人数减少一个
		 * 2. 生成一条俱乐部操作记录
		 * 3. 生成两条未读消息记录  一条针对被踢出者  一条针对创建者
		 * 4. 在俱乐部成员表中删除记录
		 * 5. 玩家已加入的俱乐部个数-1
		 * */
		SqlSession session = null ;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			ClubMapper clubMapper = session.getMapper(ClubMapper.class);
			clubMapper.decrementClubCountOne(clubId); //1.
			
			ClubOperateLog log = new ClubOperateLog();//2
			log.setClubId(clubId);
			log.setClubPosition(ClubPosition.POSITION_CREATOR.value);
			log.setOperateType(ClubOperateType.TYPE_KICK_MEMBER.value);
			log.setOperatorId(creatorId);
			log.setRemark("俱乐部创建者提出群成员(" + memberId + ")");
			ClubOperateLogMapper logMapper = session.getMapper(ClubOperateLogMapper.class);
			logMapper.addOneClubOperateLog(log);//2
			
			ClubMessageMapper msgMapper = session.getMapper(ClubMessageMapper.class); //3
			msgMapper.addOneClubMessage(msgKick); 
			msgMapper.addOneClubMessage(msgCreator); //3
			
			ClubMemberMapper memberMapper = session.getMapper(ClubMemberMapper.class);//4
			memberMapper.delClubMember(memberId, clubId);//4
			
			UserMapper userMapper = session.getMapper(UserMapper.class);//5
			userMapper.decrementUserJoinClubCount(memberId);//5
			
			session.commit();
			//设置玩家redis相关
			User user = userMapper.getExistUser(memberId);
			RedisResource.set(UserActionImpl.generateId(String.valueOf(memberId)), user);
			
			RedisResource.delUserClubInfo(String.valueOf(memberId), String.valueOf(clubId));
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
	public void CreatorSetClubConfigInfo(int clubId, String clubName, String clubIntroduce, String iconUrl)
	{
		/**
		 * 1.更新俱乐部的信息
		 * */
		SqlSession session = null ;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			ClubMapper clubMapper = session.getMapper(ClubMapper.class);
			Club c = new Club();
			c.setClubId(clubId);
			c.setClubName(clubName);
			c.setClubIntroduce(clubIntroduce);
			c.setIconUrl(iconUrl);
			clubMapper.updateClubConfigInfo(c);
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
	public void JoinPublicClub(int clubId, int userId, ClubMember clubMember, ClubMessage msgApply)
	{
		/** 加入公共俱乐部
		 * 1.生成一条操作记录
		 * 2.公共俱乐部人数+1
		 * 3.公共俱乐部成员表更新
		 * 4.生成一条针对玩家的消息 (您已经加入XXX(clubId)俱乐部)
		 * */
		SqlSession session = null ;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();

			ClubOperateLog log = new ClubOperateLog();//1
			log.setClubId(clubId);
			log.setClubPosition(ClubPosition.POSITION_NOT_MEMBER.value);
			log.setOperateType(ClubOperateType.TYPE_APPLY_JOIN.value);
			log.setOperatorId(userId);
			log.setRemark("玩家加入公共俱乐部");
			ClubOperateLogMapper logMapper = session.getMapper(ClubOperateLogMapper.class);
			logMapper.addOneClubOperateLog(log);//1
			
			ClubMapper clubMapper = session.getMapper(ClubMapper.class);//2
			clubMapper.incrementClubCountOne(clubId); //2
			
			ClubMemberMapper memberMapper = session.getMapper(ClubMemberMapper.class);//3
			memberMapper.addOneClubMember(clubMember);//3
			
			ClubMessageMapper msgMapper = session.getMapper(ClubMessageMapper.class);//4
			msgMapper.addOneClubMessage(msgApply);//4
			
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
	public void SysHandleTimeOutApply(int msgId, int applyId, ClubMessage msgApply)
	{
		/**
		 * 1. 删除申请消息
		 * 2. 生成未读消息记录 针对申请人  
		 * 3. 申请人正在申请的俱乐部数量 -1
		 * */
		SqlSession session = null ;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			
			ClubMessageMapper msgMapper = session.getMapper(ClubMessageMapper.class); //1、2
			msgMapper.delMsg(msgId);//1
			msgMapper.addOneClubMessage(msgApply); //2
			
			UserMapper userMapper = session.getMapper(UserMapper.class);//3
			userMapper.decrementUserApplyClubCount(applyId);//3
			session.commit();
			
			//设置玩家redis相关
			User user = userMapper.getExistUser(applyId);
			RedisResource.set(UserActionImpl.generateId(String.valueOf(applyId)), user);
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
