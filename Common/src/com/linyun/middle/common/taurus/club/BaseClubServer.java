package com.linyun.middle.common.taurus.club;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import com.linyun.bottom.common.exception.GameException;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.common.entity.User;
import com.linyun.common.taurus.eum.BankerMode;
import com.linyun.middle.common.taurus.server.ActionAware;
import com.linyun.middle.common.taurus.service.BaseServer;
import com.linyun.middle.common.taurus.utils.DiamondUtil;


public class BaseClubServer extends BaseServer
{
	public static ConcurrentHashMap<Integer, TaurusClub> clubMap = new ConcurrentHashMap<Integer, TaurusClub>();//clubId->club
	public static ConcurrentHashMap<String, TaurusClub> userClubMap = new ConcurrentHashMap<String, TaurusClub>();//userId->club
	public static ConcurrentSkipListSet<Integer> publicClubIdList = new ConcurrentSkipListSet<Integer>();
	
	private static final ActionAware ACTION_AWARE = new ActionAware();
	
	//***************************
	public static void bindClub(TaurusClub club)
	{
		clubMap.put(club.getClubId(), club);
	}
	public static void removeClub(int clubId)
	{
		clubMap.remove(clubId);
	}
	public static TaurusClub getClub(int clubId)
	{
		return clubMap.get(clubId);
	}
	//***********************************----------------------
	public static void bindUserClub(String userId, TaurusClub club)
	{
		userClubMap.put(userId, club);
	}
	public static void unbindUserClub(String userId)
	{
		userClubMap.remove(userId);
	}
	public static TaurusClub getUserClub(String userId)
	{
		return userClubMap.get(userId);
	}
	
	//************************************************************************
	public OutputMessage getAllPublicMessage()
	{
		TaurusClub club = null;
		OutputMessage om = new OutputMessage(true);
		om.putByte((byte)(publicClubIdList.size()));
		for (int publicClubId:publicClubIdList)
		{
			club = getClub(publicClubId);
			if (club.getClubType() == 1)
			{
				getClubMessage(club, om);
			}
		}
		return om;
	}
	
	
	public OutputMessage getClubMessage(TaurusClub club, OutputMessage om)
	{
		om.putInt(club.getClubId());
		om.putString(club.getClubName());
		om.putString(club.getClubIntroduce());
		om.putInt(ClubConfig.INSTANCE.getConfig().getPriClubPeopleMaxNum());
		om.putString(club.getIconUrl());
		if (club.getClubType() == 0 )
		{   
			String creatorId = String.valueOf(club.getCreatorId());
			User user = getExistUser(creatorId);
			om.putString(creatorId);
			om.putString(user.getNickName());
			om.putString(user.getHeadImgUrl());
		}
		else
		{
			om.putString("");
			om.putString("");
			om.putString("");
		}
		om.putString(club.getClubCity());
		om.putInt(club.getAllRoomCount());//俱乐部目前开桌数
		om.putInt(club.getOnlineCount());//俱乐部目前在线人数
		om.putInt(club.getTotalCount());//俱乐部总成员数
		return om;
	}
	
	/*
	 * 单点登录，同一个userId进行两次以上的登录，以最后一次登录的用户为准，剔除前面的用户
	 */
	public User filterSessionId(String userId, String sessionId) throws GameException {
		/*
		 * 1.从缓存中或者数据库中拉取玩家，此时的玩家的sessionId是用这个userId最后一次登录生成的sessionId
		 * 2.如有用户发起操作，其中所带的sessionId与从缓存中拿出来的最新的sessionId不等，则该设备上的玩家剔除
		 */
		User user = userAction().getExistUser(userId);
		if (!sessionId.equals(user.getSessionId())) {
			throw new GameException(GameException.USER_LOGIN_BY_OTHERWAY, "玩家userId=" + userId + "已在其他设备上登录！");
		}
		return user;
	}
	
	public static void deductClubMemberDiamond(int userId, int clubId, int round, int gameType,int diamond)
	{
		TaurusClub taurusClub = getClub(clubId);
		//公共俱乐部开房消耗钻石弃用，现与私人俱乐部开房消耗钻石一样
		TaurusClubMember taurusClubMember = taurusClub.getMember(userId);
		if(taurusClub.getClubType() == 0)
		{   
			//私人俱乐部:玩家可用额度是否足够
			int creatorId = taurusClub.getCreatorId();
			if(taurusClubMember.getUserId() == creatorId)
			{
				if(taurusClubMember.getDiamondLimit() < diamond)
				{
					throw new GameException(GameException.CLUB_CREATOR_DIAMOND_IS_NOT_ENOUGH, 
							"玩家：userId is"+userId+"是群主，在俱乐部:clubId is "+clubId+"创建房间时，总额度不足");
				}
				taurusClubMember.setDiamondLimit(taurusClubMember.getDiamondLimit() - diamond);
			}
			else
			{
				if(taurusClubMember.getDiamondLimit() - taurusClubMember.getCostDiamond() < diamond)
				{
					 throw new GameException(GameException.USER_REMAINING_AMOUNT_NOT_ENOUGH, 
							 "玩家：userId is"+userId+"是成员，在俱乐部:clubId is "+clubId+"创建房间时，可用额度不足");
				}
				User creatorUser = ACTION_AWARE.userAction().getExistUser(String.valueOf(creatorId));
				if(creatorUser.getDiamond() < diamond)
				{
					throw new GameException(GameException.CLUB_CREATOR_DIAMOND_IS_NOT_ENOUGH,
							"玩家：userId:"+userId+"在俱乐部clubId:"+clubId+"创建房间时，群主钻石不足");
				}
				TaurusClubMember memberCreator = taurusClub.getMember(creatorId);
				memberCreator.setDiamondLimit(memberCreator.getDiamondLimit() - diamond); 
			}
		}
		else
		{   
			//公共俱乐部：玩家可用额度是否足够
			//diamond = ClubConfig.INSTANCE.getConfig().getPubClubCreateRoomCostLimit();
			int pubClubUserCostLimit = ClubConfig.INSTANCE.getConfig().getPubClubUserCostLimit();
			if(pubClubUserCostLimit - taurusClubMember.getCostDiamond() < diamond)
			{
				throw new GameException(GameException.USER_REMAINING_AMOUNT_NOT_ENOUGH, "玩家：userId is"+userId+"在俱乐部:clubId is "+clubId+"创建房间时，可用额度不足");
			}
		}
		taurusClubMember.setCostDiamond(taurusClubMember.getCostDiamond()+diamond);
	}
	
	public static void returnClubMemberDiamond(int userId, int clubId, int round, int gameType, int roomNum)
	{
		TaurusClub taurusClub = getClub(clubId);
		//公共俱乐部每次开房消耗钻石弃用，与私人俱乐部开房消耗钻石一样
		int diamond =  DiamondUtil.getPayDiamond(round, BankerMode.ValueOf(gameType));
		TaurusClubMember taurusClubMember = taurusClub.getMember(userId);
		//私人俱乐部，在俱乐部成员内存中，房间解散返还群主总额度
		if(taurusClub.getClubType() == 0)
		{
			int creatorId = taurusClub.getCreatorId();
			TaurusClubMember memberCreator = taurusClub.getMember(creatorId);
			memberCreator.setDiamondLimit(memberCreator.getDiamondLimit() + diamond);
		}
		ACTION_AWARE.userAction().returnRoomPayDiamond(String.valueOf(userId), diamond, clubId,roomNum);
		taurusClubMember.setCostDiamond(taurusClubMember.getCostDiamond()-diamond);
	}
}
