package com.linyun.common.action.impl;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.session.SqlSession;

import com.linyun.bottom.cached.RedisResource;
import com.linyun.bottom.common.exception.GameException;
import com.linyun.common.action.UserAction;
import com.linyun.common.entity.Club;
import com.linyun.common.entity.ClubDiamondLog;
import com.linyun.common.entity.ClubMember;
import com.linyun.common.entity.DiamondLog;
import com.linyun.common.entity.GiftCodeBindInfo;
import com.linyun.common.entity.User;
import com.linyun.common.taurus.eum.DiamondChangedType;
import com.linyun.common.utils.ConstantsUtils;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.ClubDiamondLogMapper;
import com.linyun.data.mapper.ClubMapper;
import com.linyun.data.mapper.ClubMemberMapper;
import com.linyun.data.mapper.DiamondLogMapper;
import com.linyun.data.mapper.UserMapper;
import com.linyun.data.server.ActionAware;


public class UserActionImpl extends ActionAware implements UserAction 
{
	private static final String USER_PRIFIX = "date_user" ;
	
	@Override
	public User registerUser(User user) throws GameException
	{
		if (user == null)
		{
			throw new GameException(GameException.USER_NOT_EXIST, "注册用户时，user的值为null");
		}
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			userMapper.register(user);
			Set<Integer> set = userMapper.getAllUserIdList();
			session.commit();
			
			user = getUserByPhoneNum(user.getPhoneNum());
			if (set != null)
			{
				set.add(user.getUserId());
				RedisResource.set(ConstantsUtils.INVITE_CODE_INDEX, set);
			}
			
			RedisResource.set(generateId(String.valueOf(user.getUserId())), user);
			return user ;
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
	public User registerPlatformUser(User user) throws GameException
	{
		if (user == null)
		{
			throw new GameException(GameException.USER_NOT_EXIST, "注册用户时，user的值为null");
		}
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			userMapper.register(user);
			Set<Integer> set = userMapper.getAllUserIdList();
			session.commit();
			
			user = getPlatformUser(user.getPlatformId(), user.getUserName());
			if (set != null)
			{
				set.add(user.getUserId());
				RedisResource.set(ConstantsUtils.INVITE_CODE_INDEX, set);
			}
			
			RedisResource.set(generateId(String.valueOf(user.getUserId())), user);
			return user ;
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
	public User loginByWXId(String wxId) throws GameException
	{
		SqlSession session = null;
		try
		{	
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			User u = userMapper.getUserByWXId(wxId);
			if(u != null&&u.getIsFrozen()==User.STATE_FROZEN)
			{
				throw new GameException(GameException.USER_IS_FROZEN,"登录时用户被冻结,wxId="+wxId);
			}
			if(u != null)
			{
				RedisResource.set(generateId(String.valueOf(u.getUserId())), u);
			}
			
			return u;
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
	public User getExistUser(String userId) throws GameException
	{
		User user = RedisResource.get(generateId(userId));
		if (user != null)
		{   
			if(user.getIsFrozen() == User.STATE_FROZEN)
			{
				throw new GameException(GameException.USER_IS_FROZEN, "用户被冻结，userId = " + userId);
			}
			return user;
		}
		else
		{
			int iUserId = Integer.valueOf(userId);
			
			SqlSession session = null;
			try
			{
				session = SqlSessionFactoryUtil.ssf.openSession();
				UserMapper userMapper = session.getMapper(UserMapper.class);
				User u = userMapper.getExistUser(iUserId);
				if (u == null)
				{
					throw new GameException(GameException.USER_NOT_EXIST, "，用户不存在，userId = " + userId);
				}
				
				if(u.getIsFrozen()== User.STATE_FROZEN)
				{
					throw new GameException(GameException.USER_IS_FROZEN, "用户被冻结，userId = " + userId);
				}
				
				RedisResource.set(generateId(userId), u);
				return u;
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

	@Override
	public User getExistUserBySql(String userId) throws GameException
	{
		if (userId == null || userId.trim().isEmpty())
		{
			throw new GameException(GameException.USER_NOT_EXIST, "用户不存在，userId = " + userId);
		}
		
		int iUserId = Integer.valueOf(userId);
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			User u = userMapper.getExistUser(iUserId);
			if (u == null)
			{
				throw new GameException(GameException.USER_NOT_EXIST, "，用户不存在，userId = " + userId);
			}
			
			if(u.getIsFrozen()== User.STATE_FROZEN)
			{
				throw new GameException(GameException.USER_IS_FROZEN, "用户被冻结，userId = " + userId);
			}
			RedisResource.set(generateId(userId), u);
			return u;
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
	public void updateLoginInfo(int userId, String ip, String location) 
	{
		String strUserId = String.valueOf(userId);
		
		User user = RedisResource.get(generateId(strUserId));
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			if (user == null)
			{
				user = userMapper.getExistUser(userId);
			}
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("userId", userId);
			map.put("ip", ip);
			map.put("loginAddress", location);
			userMapper.updateLoginInfo(map);
			session.commit();
			
			user.setLoginAddress(location);
			user.setLoginTime(new Date(System.currentTimeMillis()));
			user.setLoginIp(ip);
			RedisResource.set(generateId(strUserId), user);
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
	public User getExistUserByWXId(String wxId) throws GameException
	{   
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			User u = userMapper.getUserByWXId(wxId);
			if(u == null)
			{
				throw new GameException(GameException.USER_NOT_EXIST, "获取用户时，user的值为null，wxId ="+wxId);
			}
			if(u.getIsFrozen()== User.STATE_FROZEN)
			{
				throw new GameException(GameException.USER_IS_FROZEN, "获取用户时,用户被冻结，wxId = " + wxId);
			}
			RedisResource.set(generateId(String.valueOf(u.getUserId())), u);
			return u;
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
	public void deductDiamond(String userId, int diamond)
	{
		int iUserId = Integer.valueOf(userId);
	    User user = null ;
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			
			user =RedisResource.get(generateId(userId));
			if (user == null)
			{
				user = userMapper.getExistUser(iUserId);
			}
			int oldDiamond = user.getDiamond();
			int newDiamond = oldDiamond - diamond;
			user.setDiamond(newDiamond);
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("userId", userId);
			map.put("diamond", user.getDiamond());
			userMapper.updateDiamond(map);
			
			DiamondLog diamondLog = new DiamondLog();
			diamondLog.setUserId(iUserId);
			diamondLog.setOldDiamond(oldDiamond);
			diamondLog.setNewDiamond(newDiamond);
			diamondLog.setChangedDiamond(diamond*(-1));
			diamondLog.setChangedType(DiamondChangedType.TYPE_DEDUCT.value);
			DiamondLogMapper diamondLogMapper = session.getMapper(DiamondLogMapper.class);
			diamondLogMapper.addOneRecord(diamondLog);
			RedisResource.set(generateId(userId), user);
			
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
	public void deductDiamond(String userId, int diamond,Club club,int roomNum)
	{
		int iUserId = Integer.valueOf(userId);
	    User user = null ;
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			
			//私人俱乐部创建房间处理
			if(club.getClubType() == 0)
			{
				int creatorId = club.getCreatorId();
				user =RedisResource.get(generateId(String.valueOf(creatorId)));
				if (user == null)
				{
					user = userMapper.getExistUser(creatorId);
				}
				int oldDiamond = user.getDiamond();
				int newDiamond = oldDiamond - diamond;
				user.setDiamond(newDiamond);
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("userId", creatorId);
				map.put("diamond", user.getDiamond());
				userMapper.updateDiamond(map);
				RedisResource.set(generateId(String.valueOf(creatorId)), user);
				
				/** 钻石账变记录 */
				DiamondLog diamondLog = new DiamondLog();
				diamondLog.setUserId(creatorId);
				diamondLog.setOldDiamond(oldDiamond);
				diamondLog.setNewDiamond(newDiamond);
				diamondLog.setChangedDiamond(diamond*(-1));
				diamondLog.setChangedType(DiamondChangedType.TYPE_CLUB_BUYSCORE_DEDUCT.value);
				DiamondLogMapper diamondLogMapper = session.getMapper(DiamondLogMapper.class);
				diamondLogMapper.addOneRecord(diamondLog);
				
			}
			
			/**俱乐部成员消耗钻石记录*/
			ClubDiamondLog cdl = new ClubDiamondLog();
			cdl.setClubId(club.getClubId());
			cdl.setRoomNum(roomNum);
			cdl.setUserId(iUserId);
			cdl.setDiamond(diamond*(-1));
			cdl.setType(DiamondChangedType.TYPE_CLUB_BUYSCORE_DEDUCT.value);
			cdl.setRemark("俱乐部成员买入积分消耗");
			ClubDiamondLogMapper clubDiamondMapper = session.getMapper(ClubDiamondLogMapper.class);
			clubDiamondMapper.addOneRecordwithCostDiamond(cdl);
			
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
	public void rechargeDiamond(String userId, int diamond)
	{
		int iUserId = Integer.valueOf(userId);
		
		User user =RedisResource.get(generateId(userId));
		SqlSession session = null; 
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			if (user == null)
			{
				user = userMapper.getExistUser(iUserId);
			}
			
			int oldDiamond = user.getDiamond();
			int newDiamond = oldDiamond + diamond;
			user.setDiamond(newDiamond);
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("userId", userId);
			map.put("diamond", user.getDiamond());
			userMapper.updateDiamond(map);
			
			session.commit();

			RedisResource.set(generateId(userId), user);
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
	public void addUserOneRound(String userId)
	{		
		int iUserId = Integer.valueOf(userId);
		User user =RedisResource.get(generateId(userId));
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			if (user == null)
			{
				user = userMapper.getExistUser(iUserId);
			}
			user.setRoundNum(user.getRoundNum()+1);
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("userId", userId);
			map.put("roundNum", user.getRoundNum());
			userMapper.updateRoundNum(map);
			session.commit();
			
			RedisResource.set(generateId(userId), user);
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
	public void updateUserSessionId(String userId, String sessionId)
	{
		User user =RedisResource.get(generateId(userId));
		SqlSession session = null; 
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			if (user == null)
			{
				user = userMapper.getExistUser(Integer.valueOf(userId));
			}
			Map<String,Object> paramMap = new HashMap<String,Object>();
			paramMap.put("userId", userId);
			paramMap.put("sessionId", sessionId);
			userMapper.updateUserSessionId(paramMap);
			session.commit();
			
			//更新缓存
			user.setSessionId(sessionId);
			RedisResource.set(generateId(userId), user);
		}
		finally
		{
			if (session != null)
			{
				session.close();
			}
		}
	}
    
	//绑定玩家的邀请码
	@Override
	public void bindUserInviteCode(String userId, int inviteCode)
	{
		User user =RedisResource.get(generateId(userId));
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			if (user == null)
			{
				user = userMapper.getExistUser(Integer.valueOf(userId));
			}
			Map<String,Object> paramMap = new HashMap<String,Object>();
			paramMap.put("userId", userId);
			paramMap.put("inviteCode", inviteCode);
			userMapper.updateUserInviteCode(paramMap);
			session.commit();
			
			//更新到缓存
			user.setInviteCode(inviteCode);
			RedisResource.set(generateId(userId), user);
		}
		finally
		{
			if (session != null)
			{
				session.close();
			}
		}
	}
  
	public User getUser(String userId)
	{
		 User user = RedisResource.get(generateId(userId));
		 if(user == null )
		 {   
			 SqlSession session = null;
			 try
			 {
				 session = SqlSessionFactoryUtil.ssf.openSession();
				 UserMapper userMapper = session.getMapper(UserMapper.class);
				 user = userMapper.getExistUser(Integer.parseInt(userId)); 
				 RedisResource.set(generateId(userId), user);
			 }
			 finally
			 {
				 if (session != null)
				 {
					 session.close();
				 }
			 }
		 }
		 
		 return user ;
	}

	public static String generateId(String userId)
	{
		return USER_PRIFIX + "_" + userId;
	}

	@Override
	public void returnRoomPayDiamond(String userId, int diamond) 
	{
		int iUserId = Integer.valueOf(userId);
		User user =RedisResource.get(generateId(userId));
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			if (user == null)
			{
				user = userMapper.getExistUser(Integer.valueOf(userId));
			}
			int oldDiamond = user.getDiamond();
			int newDiamond = oldDiamond + diamond;
			user.setDiamond(newDiamond);
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("userId", userId);
			map.put("diamond", user.getDiamond());
			userMapper.updateDiamond(map);
			
			/** 钻石账变记录 */
			DiamondLog diamondLog = new DiamondLog();
			diamondLog.setUserId(iUserId);
			diamondLog.setOldDiamond(oldDiamond);
			diamondLog.setNewDiamond(newDiamond);
			diamondLog.setChangedDiamond(diamond);
			diamondLog.setChangedType(DiamondChangedType.TYPE_RETURN.value);
			DiamondLogMapper diamondLogMapper = session.getMapper(DiamondLogMapper.class);
			diamondLogMapper.addOneRecord(diamondLog);
			
			session.commit();
			RedisResource.set(generateId(userId), user);
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
	public void returnRoomPayDiamond(String userId, int diamond, int clubId,int roomNum)
	{
		int iUserId = Integer.valueOf(userId);
	    User user = null ;
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			
			//在俱乐部里面消耗的是群主的钻石
			ClubMapper clubMapper = session.getMapper(ClubMapper.class);
			Club club = clubMapper.getClub(clubId);
			
			ClubMemberMapper clubMemberMapper = session.getMapper(ClubMemberMapper.class);
			//私人俱乐部创建房间处理
			if(club.getClubType() == 0)
			{
				int creatorId = club.getCreatorId();
				user =RedisResource.get(generateId(String.valueOf(creatorId)));
				if (user == null)
				{
					user = userMapper.getExistUser(creatorId);
				}
				int oldDiamond = user.getDiamond();
				int newDiamond = oldDiamond + diamond;
				user.setDiamond(newDiamond);
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("userId", userId);
				map.put("diamond", user.getDiamond());
				userMapper.updateDiamond(map);
				RedisResource.set(generateId(String.valueOf(creatorId)), user);
				
				/** 钻石账变记录 */
				DiamondLog diamondLog = new DiamondLog();
				diamondLog.setUserId(creatorId);
				diamondLog.setOldDiamond(oldDiamond);
				diamondLog.setNewDiamond(newDiamond);
				diamondLog.setChangedDiamond(diamond);
				diamondLog.setChangedType(DiamondChangedType.TYPE_CLUB_RETURN.value);
				DiamondLogMapper diamondLogMapper = session.getMapper(DiamondLogMapper.class);
				diamondLogMapper.addOneRecord(diamondLog);
				
				/**在俱乐部成员表中增加群主的总额度*/
				ClubMember clubCreator = clubMemberMapper.getClubMemberByClubId(clubId, creatorId);
				int diamondLimit = clubCreator.getDiamondLimit() + diamond ;
				clubCreator.setDiamondLimit(diamondLimit);
				clubMemberMapper.updateClubCreatorDiamondLimit(clubId,creatorId,diamondLimit);
			}
			
			/**俱乐部成员房间解散，返还群主钻石钻石记录*/
			ClubDiamondLog cdl = new ClubDiamondLog();
			cdl.setClubId(clubId);
			cdl.setRoomNum(roomNum);
			cdl.setUserId(iUserId);
			cdl.setDiamond(diamond);
			cdl.setType(DiamondChangedType.TYPE_CLUB_RETURN.value);
			cdl.setRemark("游戏未开始解散，归还额度");
			ClubDiamondLogMapper clubDiamondMapper = session.getMapper(ClubDiamondLogMapper.class);
			clubDiamondMapper.addOneRecordwithCostDiamond(cdl);
			
			/**俱乐部成员已花费钻石减少*/
			ClubMember clubMember = clubMemberMapper.getClubMemberByClubId(clubId, iUserId);
			int _diamond = clubMember.getCostDiamond() - diamond ;
			clubMember.setCostDiamond(_diamond);
			clubMemberMapper.updateClubMemberCostDiamond(clubId, iUserId, _diamond);
			
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
	public void updateUserWxInfo(String userId, String headImgUrl, String nickName) 
	{
		User user = RedisResource.get(generateId(userId));
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			if (user == null)
			{
				user = userMapper.getExistUser(Integer.valueOf(userId));
			}
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("userId", userId);
			map.put("headImgUrl", headImgUrl);
			map.put("nickName", nickName);
			userMapper.updateUserWxInfo(map);
			session.commit();
			
			user.setHeadImgUrl(headImgUrl);
			user.setNickName(nickName);
			RedisResource.set(generateId(userId), user);
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
	public void updateUserTableNum(String userId, String tableNum)
	{
		if (tableNum.isEmpty())
		{
			tableNum = null;
		}
		
		User user = RedisResource.get(generateId(userId));
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			if (user == null)
			{
				user = userMapper.getExistUser(Integer.valueOf(userId));
			}
			
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("userId", userId);
			map.put("tableNum", tableNum);
			userMapper.updateUserTableNum(map);
			session.commit();
			
			user.setTableNum(tableNum);
			RedisResource.set(generateId(userId), user);
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
	public boolean isBindGiftCode(String userId, String giftCode)
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();			
			UserMapper userMapper = session.getMapper(UserMapper.class);
			Set<String> set = userMapper.getBindGiftCode(Integer.valueOf(userId));
			for (String str: set)
			{
				if (str.equals(giftCode))
				{
					return true;
				}
			}
			return false;
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
	public void bindGiftCode(String userId, String giftCode, int presentDiamond)
	{
		int iUserId = Integer.valueOf(userId);
		User user = RedisResource.get(generateId(userId));
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			//user 相关
			UserMapper userMapper = session.getMapper(UserMapper.class);
			if (user == null)
			{
				user = userMapper.getExistUser(iUserId);
			}
			int oldDiamond = user.getDiamond();
			int newDiamond = oldDiamond + presentDiamond;
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("userId", iUserId);
			map.put("diamond", newDiamond);
			userMapper.updateDiamond(map);
			
			//giftCodeBindInfo 相关
			GiftCodeBindInfo giftCodeInfo = new GiftCodeBindInfo();
			giftCodeInfo.setUserId(iUserId);
			giftCodeInfo.setGiftCode(giftCode);
			giftCodeInfo.setDiamond(presentDiamond);
			userMapper.bindGiftCode(giftCodeInfo);
			
			//diamond相关
			DiamondLog diamondLog = new DiamondLog();
			diamondLog.setUserId(iUserId);
			diamondLog.setOldDiamond(oldDiamond);
			diamondLog.setNewDiamond(newDiamond);
			diamondLog.setChangedDiamond(presentDiamond);
			diamondLog.setChangedType(DiamondChangedType.TYPE_ACTIVITY.value);
			
			DiamondLogMapper diamondLogMapper = session.getMapper(DiamondLogMapper.class);
			diamondLogMapper.addOneRecord(diamondLog);
			session.commit();
			
			user.setDiamond(newDiamond);
			RedisResource.set(generateId(userId), user);
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
	public void updateUserClubCount(int userId)
	{
		SqlSession session = null;
		User user = RedisResource.get(generateId(String.valueOf(userId)));
		try
		{   
			session = SqlSessionFactoryUtil.ssf.openSession();			
			UserMapper userMapper = session.getMapper(UserMapper.class);
			if (user == null)
		    {
			    user = userMapper.getExistUser(userId);
		    }
			user.setClubCount(user.getClubCount()+1); 
			
		    userMapper.incrementUserJoinClubCount(userId);
			session.commit();
			
			RedisResource.set(generateId(String.valueOf(userId)), user);
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
	public void updateUserRegisterInfo(String userId, String registerIp, String position)
	{
		SqlSession session = null;
		User user = RedisResource.get(generateId(String.valueOf(userId)));
		try
		{   
			int iUserId = Integer.valueOf(userId);
			session = SqlSessionFactoryUtil.ssf.openSession();			
			UserMapper userMapper = session.getMapper(UserMapper.class);
			if (user == null)
		    {
			    user = userMapper.getExistUser(iUserId);
		    }
			
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("userId", iUserId);
			map.put("registerIp", registerIp);
			map.put("loginAddress", position);
			map.put("loginIp", registerIp);
			userMapper.updateRegisterInfo(map);
			session.commit();
			
			Date date = new Date(System.currentTimeMillis());
			user.setRegisterIp(registerIp);
			user.setRegisterTime(date);
			user.setLoginIp(registerIp);
			user.setLoginAddress(position);
			user.setLoginTime(date);
			RedisResource.set(generateId(String.valueOf(userId)), user);
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
	public void updateUserLoginInfo(String userId, String loginIp, String position)
	{
		SqlSession session = null;
		User user = RedisResource.get(generateId(String.valueOf(userId)));
		try
		{   
			int iUserId = Integer.valueOf(userId);
			session = SqlSessionFactoryUtil.ssf.openSession();			
			UserMapper userMapper = session.getMapper(UserMapper.class);
			if (user == null)
		    {
			    user = userMapper.getExistUser(iUserId);
		    }
			
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("userId", iUserId);
			map.put("loginAddress", position);
			map.put("loginIp", loginIp);
			userMapper.updateRegisterInfo(map);
			session.commit();
			
			user.setLoginIp(loginIp);
			user.setLoginAddress(position);
			user.setLoginTime(new Date(System.currentTimeMillis()));
			RedisResource.set(generateId(String.valueOf(userId)), user);
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
	public void setProxy(int userId)
	{
		SqlSession session = null;
		User user = RedisResource.get(generateId(String.valueOf(userId)));
		try
		{   
			int iUserId = Integer.valueOf(userId);
			session = SqlSessionFactoryUtil.ssf.openSession();			
			UserMapper userMapper = session.getMapper(UserMapper.class);
			if (user == null)
		    {
			    user = userMapper.getExistUser(iUserId);
		    }
			userMapper.setProxy(userId);
			session.commit();
			
			user.setProxy(true);
			RedisResource.set(generateId(String.valueOf(userId)), user);
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
	public void cancelProxy(int userId)
	{
		SqlSession session = null;
		User user = RedisResource.get(generateId(String.valueOf(userId)));
		try
		{   
			int iUserId = Integer.valueOf(userId);
			session = SqlSessionFactoryUtil.ssf.openSession();			
			UserMapper userMapper = session.getMapper(UserMapper.class);
			if (user == null)
		    {
			    user = userMapper.getExistUser(iUserId);
		    }
			userMapper.cancelProxy(userId);
			session.commit();
			
			user.setProxy(false);
			RedisResource.set(generateId(String.valueOf(userId)), user);
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
	public void manualRechargeDiamond(String userId, int diamond,String remark)
	{
		int iUserId = Integer.valueOf(userId);
		
		User user =RedisResource.get(generateId(userId));
		SqlSession session = null; 
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			if (user == null)
			{
				user = userMapper.getExistUser(iUserId);
			}
			
			int oldDiamond = user.getDiamond();
			int newDiamond = oldDiamond + diamond;
			user.setDiamond(newDiamond);
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("userId", userId);
			map.put("diamond", user.getDiamond());
			userMapper.updateDiamond(map);
			
			ClubMapper clubMapper = session.getMapper(ClubMapper.class);
			Club club = clubMapper.getClubByCreatorId(iUserId);
			if(club != null)
			{
				 ClubMemberMapper clubMemberMapper = session.getMapper(ClubMemberMapper.class);
				 ClubMember clubMember = clubMemberMapper.getClubMemberByClubId(club.getClubId(), iUserId);
				 clubMember.setDiamondLimit(newDiamond);
				 clubMemberMapper.updateClubCreatorDiamondLimit(club.getClubId(), iUserId, newDiamond);
			}
			
			/** 钻石账变记录 */
			DiamondLog diamondLog = new DiamondLog();
			diamondLog.setUserId(iUserId);
			diamondLog.setOldDiamond(oldDiamond);
			diamondLog.setNewDiamond(newDiamond);
			diamondLog.setChangedDiamond(diamond);
			diamondLog.setChangedType(DiamondChangedType.TYPE_MANUAL_RECHARGE.value);
			diamondLog.setRemark(remark);
			DiamondLogMapper diamondLogMapper = session.getMapper(DiamondLogMapper.class);
			diamondLogMapper.addOneRecord(diamondLog);
			session.commit();

			RedisResource.set(generateId(userId), user);
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
	public void manualDeductDiamond(String userId, int diamond,String remark)
	{
		int iUserId = Integer.valueOf(userId);
	    User user = null ;
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			
			user =RedisResource.get(generateId(userId));
			if (user == null)
			{
				user = userMapper.getExistUser(iUserId);
			}
			int oldDiamond = user.getDiamond();
			int newDiamond = oldDiamond - diamond;
			user.setDiamond(newDiamond);
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("userId", userId);
			map.put("diamond", user.getDiamond());
			userMapper.updateDiamond(map);
			
			ClubMapper clubMapper = session.getMapper(ClubMapper.class);
			Club club = clubMapper.getClubByCreatorId(iUserId);
			if(club != null)
			{
				 ClubMemberMapper clubMemberMapper = session.getMapper(ClubMemberMapper.class);
				 ClubMember clubMember = clubMemberMapper.getClubMemberByClubId(club.getClubId(), iUserId);
				 clubMember.setDiamondLimit(newDiamond);
				 clubMemberMapper.updateClubCreatorDiamondLimit(club.getClubId(), iUserId, newDiamond);
			}
			
			DiamondLog diamondLog = new DiamondLog();
			diamondLog.setUserId(iUserId);
			diamondLog.setOldDiamond(oldDiamond);
			diamondLog.setNewDiamond(newDiamond);
			diamondLog.setChangedDiamond(diamond*(-1));
			diamondLog.setChangedType(DiamondChangedType.TYPE_MANUAL_DEDUCT.value);
			diamondLog.setRemark(remark);
			DiamondLogMapper diamondLogMapper = session.getMapper(DiamondLogMapper.class);
			diamondLogMapper.addOneRecord(diamondLog);
			RedisResource.set(generateId(userId), user);
			
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
	public void setUserFrozenStatue(String userId, int statue) 
	{   
		SqlSession session = null ;
		int iUserId = Integer.parseInt(userId);
		User user =RedisResource.get(generateId(userId));
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			if(user == null)
			{
				user = userMapper.getExistUser(iUserId);
			}
			user.setIsFrozen((byte)statue);
			userMapper.setUserFrozenStatue(user);
			RedisResource.set(generateId(userId), user);
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
	public User getExistUserNoCareFrozen(String userId) throws GameException 
	{
		if (userId == null || userId.trim().isEmpty())
		{
			throw new GameException(GameException.USER_NOT_EXIST, "冻结用户不存在，userId = " + userId);
		}
		
		int iUserId = Integer.valueOf(userId);
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			User u = userMapper.getExistUser(iUserId);
			if (u == null)
			{
				throw new GameException(GameException.USER_NOT_EXIST, "，冻结用户不存在，userId = " + userId);
			}
			RedisResource.set(generateId(userId), u);
			return u;
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
	public User getUserByPhoneNum(long phoneNum)
	{
		SqlSession session = null;
		try
		{	
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			User u = userMapper.getUserByPhoneNum(phoneNum);
			if(u != null && u.getIsFrozen()==User.STATE_FROZEN)
			{
				throw new GameException(GameException.USER_IS_FROZEN,"用户通过手机号登录时被冻结,phoneNum="+phoneNum);
			}
			if(u != null)
			{
				RedisResource.set(generateId(String.valueOf(u.getUserId())), u);
			}
			return u;
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
	public void updateUserNickName(int userId, String nickName)
	{
		SqlSession session = null;
		User user = RedisResource.get(generateId(String.valueOf(userId)));
		try
		{	
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("userId", userId);
			map.put("nickName", nickName);
			userMapper.updateUserNickName(map);
			
			if(user == null)
			{
				user = userMapper.getExistUser(userId);
			}
			else 
			{
				user.setNickName(nickName);
			}
			session.commit();
			RedisResource.set(generateId(String.valueOf(userId)), user);
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
	public void updateUserLoginTime(String userId)
	{   
		SqlSession session = null;
		int iUserId = Integer.parseInt(userId);
		User user = RedisResource.get(generateId(userId));
		try 
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			if(user == null)
			{
				user = userMapper.getExistUser(iUserId);
			}
			userMapper.updateUserLoginTime(iUserId);
			user.setLoginTime(new Date(System.currentTimeMillis()));
			RedisResource.set(generateId(userId),user);
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
	public void returnRoomOwnerDiamond(String userId, int diamond, Club club,int roomNum)
	{
		int iUserId = Integer.valueOf(userId);
	    User user = null ;
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			
			//私人俱乐部创建房间处理
			if(club.getClubType() == 0)
			{
				int creatorId = club.getCreatorId();
				user =RedisResource.get(generateId(String.valueOf(creatorId)));
				if (user == null)
				{
					user = userMapper.getExistUser(creatorId);
				}
				int oldDiamond = user.getDiamond();
				int newDiamond = oldDiamond + diamond;
				user.setDiamond(newDiamond);
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("userId", creatorId);
				map.put("diamond", user.getDiamond());
				userMapper.updateDiamond(map);
				RedisResource.set(generateId(String.valueOf(creatorId)), user);
				
				/** 钻石账变记录 */
				DiamondLog diamondLog = new DiamondLog();
				diamondLog.setUserId(creatorId);
				diamondLog.setOldDiamond(oldDiamond);
				diamondLog.setNewDiamond(newDiamond);
				diamondLog.setChangedDiamond(diamond);
				diamondLog.setChangedType(DiamondChangedType.TYPE_CLUB_RETURN.value);
				DiamondLogMapper diamondLogMapper = session.getMapper(DiamondLogMapper.class);
				diamondLogMapper.addOneRecord(diamondLog);
				
			}
			
			/**俱乐部成员房间解散，返还群主钻石钻石记录*/
			ClubDiamondLog cdl = new ClubDiamondLog();
			cdl.setClubId(club.getClubId());
			cdl.setRoomNum(roomNum);
			cdl.setUserId(iUserId);
			cdl.setDiamond(diamond);
			cdl.setType(DiamondChangedType.TYPE_CLUB_RETURN.value);
			cdl.setRemark("游戏未开始玩家买入坐下切出房间或者房间解散，归还额度");
			ClubDiamondLogMapper clubDiamondMapper = session.getMapper(ClubDiamondLogMapper.class);
			clubDiamondMapper.addOneRecordwithCostDiamond(cdl);
			
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
	public User getPlatformUser(int platformId, String userName) throws GameException
	{
		SqlSession session = null;
		try
		{	
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			HashMap<String,Object> map = new HashMap<String,Object>();
			map.put("platformId", platformId);
			map.put("userName",userName);
			User u = userMapper.getPlatformUser(map);
			if(u != null&&u.getIsFrozen()==User.STATE_FROZEN)
			{
				throw new GameException(GameException.USER_IS_FROZEN,"登录时用户被冻结,platformId="+platformId+",userName="+userName);
			}
			if(u != null)
			{
				RedisResource.set(generateId(String.valueOf(u.getUserId())), u);
			}
			
			return u;
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
	public void udpateCoin(String userId, int coin) 
	{
        
		
		User user = RedisResource.get(generateId(userId));
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			if (user == null)
			{
				user = userMapper.getExistUser(Integer.parseInt(userId));
			}
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("userId", userId);
			map.put("coin",coin);
			userMapper.updateCoin(map);
			session.commit();
			
			user.setCoin(coin);
			RedisResource.set(generateId(userId), user);
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
