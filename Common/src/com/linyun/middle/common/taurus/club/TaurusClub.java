package com.linyun.middle.common.taurus.club;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.linyun.bottom.container.GameSession;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.common.entity.Club;
import com.linyun.common.entity.PrivateRoom;
import com.linyun.common.entity.User;
import com.linyun.common.taurus.eum.BankerMode;
import com.linyun.middle.common.taurus.room.TaurusRoom;
import com.linyun.middle.common.taurus.server.ActionAware;
import com.linyun.middle.common.taurus.service.BaseServer;
import com.linyun.middle.common.taurus.table.TaurusTable;

/**
 * @author liangbingbing
 * 思路  在服务器启动的时候  加载所有数据库的俱乐部信息(包括俱乐部成员信息)到内存中
 *    在创建或者删除俱乐部 --- 增加、移除俱乐部信息
 *    批准加入或者踢出成员  --- 增加、移除成员信息
 * 
 * */

public class TaurusClub
{
	private static Logger logger = LoggerFactory.getLogger(TaurusClub.class);
	public static final ActionAware m_s_action = new ActionAware();
	
	private ConcurrentHashMap<Integer, GameSession> userSessionMap = new ConcurrentHashMap<Integer, GameSession>();//俱乐部里面的所有人的session集合
	private ConcurrentHashMap<Integer, TaurusClubMember> clubMemberMap = new ConcurrentHashMap<Integer, TaurusClubMember>();
	private LinkedHashMap<Integer, TaurusRoom> roomMap = new LinkedHashMap<Integer, TaurusRoom>();
	private Object object_online_lock = new Object();
	private Object object_people_count_lock = new Object();
	private Object object_room_lock = new Object();
	
	/** 俱乐部相关的一些信息 */
	private final Club club;
	private int onlineCount; //在线人数
	
	public TaurusClub(Club _club)
	{
		club = _club;
	}
	
	public boolean ContainsUser(Integer userId)
	{
		return clubMemberMap.containsKey(userId);
	}
	
	public boolean IsCreator(Integer userId)
	{
		return (userId == this.getCreatorId());
	}
	
	public void addUserSession(Integer userId, GameSession session)
	{
		if ((userId != null) && (session != null))
		{
			userSessionMap.put(userId, session);
		}
	}
	public void removeUserSession(String userId)
	{
		userSessionMap.remove(Integer.valueOf(userId));
	}
	
	public GameSession getUserSession(int userId)
	{   
		return  userSessionMap.get(userId);
	}
	public void sendMessage(short protocolId, OutputMessage om)
	{
		Collection<GameSession> sessions = userSessionMap.values();
		for (GameSession session : sessions)
		{
			if (session.getStatus() == GameSession.OPEN)
			{
				session.sendMessage(protocolId, om);
			}
		}
	}
	/*---------------------------------------------*/
	public void addRoom(TaurusRoom room)
	{
		if (room != null)
		{
			synchronized (object_room_lock) 
			{
				roomMap.put(room.getRoomId(), room);
			}
		}
	}
	public TaurusRoom getRoom(int roomId)
	{
		synchronized (object_room_lock) 
		{
			return roomMap.get(roomId);
		}
	}
	public void removeRoom(int roomId)
	{
		synchronized (object_room_lock) 
		{
			roomMap.remove(roomId);
		}
	}
	public int getAllRoomCount()
	{
		synchronized (object_room_lock) 
		{
			return roomMap.size();
		}
	}
	public OutputMessage getAllRoomInfo()
	{
		ActionAware action = new ActionAware();
		OutputMessage om = new OutputMessage(true);
		om.putByte((byte)roomMap.size());
		PrivateRoom privateRoom  = null;
		User user = null;
		
		Set<Integer> keySet = roomMap.keySet();
		Integer[] rooms = new Integer[keySet.size()];
		Integer[] array = keySet.toArray(rooms);
		for (int i= array.length-1; i>=0; i--)
		{
			Integer roomId = array[i];
		    privateRoom = m_s_action.roomAction().getPrivateRoom(roomId);
			om.putString(String.valueOf(privateRoom.getRoomNum()));
			byte upBankerMode = privateRoom.getUpBankerMode();
			om.putByte(upBankerMode);
			if(BankerMode.ValueOf(upBankerMode) == BankerMode.BANKER_MODE_ALL_COMPARE)
			{
				om.putInt(privateRoom.getAllCompareBaseScore());
			}
			else
			{
				om.putInt(privateRoom.getBaseScore());
			}
			om.putInt(privateRoom.getGameTime());
			om.putByte((byte)privateRoom.getSeatNum());
			om.putByte((byte)privateRoom.getSitDownNum());
			String roomOwnerId = String.valueOf(privateRoom.getRoomOwnerId());
			user = action.userAction().getExistUser(roomOwnerId);
			om.putString(roomOwnerId);
			om.putString(user.getNickName());
			om.putString(user.getHeadImgUrl());
			om.putByte(privateRoom.getRoomStatus());
		}
		
		return om;
	}
	
	
	//在该俱乐部已开房间列表中，被踢出俱乐部玩家是否是房主，是则不可以踢出
	public boolean isRoomOwner(int userId)
	{
		Set<Map.Entry<Integer, TaurusRoom>> entrySet = roomMap.entrySet();
		for (Entry<Integer, TaurusRoom> entry : entrySet)
		{
			int roomOwnerId = entry.getValue().getRoomOwnerId();
			if(userId == roomOwnerId)
			{
				return true ;
			}
		}
		return false ;
	}
	
	//群主踢出成员时，玩家是否在该俱乐部已开的房间内坐下，坐下则不可以踢出
	public boolean bindCurrentClubTable(String userId)
	{
		TaurusTable taurusTable = BaseServer.getUserTable(userId);
		if(taurusTable != null && roomMap.get(Integer.parseInt(taurusTable.getTableId())) != null)
		{
			return true ;
		}
		return false ;
	}
	
	/*--------------------------------------------*/
	
	public void addMember(TaurusClubMember member)
	{
		clubMemberMap.put(member.getUserId(), member);
	}
	public void removeMember(int userId)
	{
		clubMemberMap.remove(userId);
	}
	public TaurusClubMember getMember(int userId)
	{
		return clubMemberMap.get(userId);
	}
	public List<TaurusClubMember> getAllMember()
	{
		List<TaurusClubMember> list = new ArrayList<TaurusClubMember>();
		for (Map.Entry<Integer, TaurusClubMember> entry: clubMemberMap.entrySet())
		{
			list.add(entry.getValue());
		}
		return list;
	}
	
	public Club getClub() {
		return club;
	}

	public int getClubId()
	{
		return club.getClubId();
	}	
	public String getClubName()
	{
		return club.getClubName();
	}
	public void setClubName(String clubName)
	{
		this.club.setClubName(clubName);
	}
	public boolean isPrivateClub()
	{
		return club.getClubType()==0;
	}
	public String getClubCity()
	{
		return club.getCity();
	}
	public void setClubCity(String clubCity)
	{
		this.club.setCity(clubCity);
	}
	public String getClubIntroduce()
	{
		return club.getClubIntroduce();
	}
	public void setClubIntroduce(String clubIntroduce)
	{
		this.club.setClubIntroduce(clubIntroduce);
	}
	public void setClubCoinPool(int coinPool)
	{
		this.club.setCoinPool(coinPool);
	}
	public String getIconUrl() {
		return club.getIconUrl();
	}
	public void setIconUrl(String iconUrl) {
		this.club.setIconUrl(iconUrl);
	}
	public int getCreatorId() {
		return club.getCreatorId();
	}

	public byte getClubType() {
		return club.getClubType();
	}
	
	public int getDiamondPercent()
	{
		return club.getDiamondPercent();
	}
	public void setDiamondPercent(int percent)
	{
		club.setDiamondPercent(percent);
	}
	
	public int getScorePool()
	{
		return club.getScorePool();
	}
	
	public void setScorePool(int scorePool)
	{
		this.club.setScorePool(scorePool);
	}
    
	public int  getExpandRate()
	{
		return this.club.getExpandRate();
	}
	public void setExpandRate(int rate)
	{
		club.setExpandRate(rate);
	}
	
	public int  getScoreRate()
	{
		return club.getScoreRate();
	}
	
	public void setScoreRate(int rate)
	{
		club.setScoreRate(rate);
	}
	public int getTotalCount()
	{
		synchronized (object_people_count_lock)
		{
			return club.getPeopleCount();
		}
	}
	public void addOneTotalCount()
	{
		synchronized (object_people_count_lock)
		{
			club.setPeopleCount(club.getPeopleCount()+1);
		}
	}
	public void subOneTotalCount()
	{
		synchronized (object_people_count_lock)
		{
			club.setPeopleCount(club.getPeopleCount()-1);
		}
	}

	public int getOnlineCount()
	{
		synchronized (object_online_lock)
		{   
			if(onlineCount < 0)
			{
				onlineCount = 0 ;
			}
			return onlineCount;
		}
	}
	public void addOneOnlineCount()
	{
		synchronized (object_online_lock)
		{
			this.onlineCount += 1;
			if (this.onlineCount > this.getTotalCount())
			{
				this.onlineCount = this.getTotalCount();
			}
			logger.error("TaurusClub::clubId="+this.getClubId()+" addOneOnlineCount current onlineCount : " + onlineCount);
		}
	}
	public void subOneOnlineCount()
	{
		synchronized (object_online_lock)
		{
			this.onlineCount -= 1;
			logger.error("TaurusClub::clubId="+this.getClubId()+"subOneOnlineCount current onlineCount : " + onlineCount);
		}
	}
	
}
