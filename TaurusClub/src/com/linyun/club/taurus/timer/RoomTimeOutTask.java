package com.linyun.club.taurus.timer;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.linyun.bottom.common.exception.GameException;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.club.taurus.service.ClubGameServer;
import com.linyun.middle.common.taurus.club.BaseClubServer;
import com.linyun.middle.common.taurus.club.TaurusClub;
import com.linyun.middle.common.taurus.club.TaurusClubMember;
import com.linyun.middle.common.taurus.manager.RoomManager;
import com.linyun.middle.common.taurus.manager.TableManager;
import com.linyun.middle.common.taurus.player.TaurusPlayer;
import com.linyun.middle.common.taurus.room.TaurusRoom;
import com.linyun.middle.common.taurus.server.ActionAware;
import com.linyun.middle.common.taurus.service.BaseServer;
import com.linyun.middle.common.taurus.table.TaurusSeat;
import com.linyun.middle.common.taurus.table.TaurusTable;

public class RoomTimeOutTask 
{
	private static Logger logger = LoggerFactory.getLogger(RoomTimeOutTask.class);
	public static final int ROOM_TIMEOUT_DISSOLUTION_TIME = 30*60; //unit seconds
	
	public static final int ROOM_TIMEOUT_DISSOLUTION_TIME_COUNT = 60;  //unit seconds
	public static final ActionAware action = new ActionAware();
	
	private static RoomTimeOutTask roomTimeOutTask = new RoomTimeOutTask();
		
	public static RoomTimeOutTask getInstance()
	{
		return roomTimeOutTask;
	}
	
	/**
	 * 每分钟检查一次 房间表中的 超过三十分钟未开始游戏的记录
	 * */
	public void start()
	{
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);  
	      
		Runnable runnable = new Runnable() 
		{
			@Override
			public void run() 
			{
				try
				{
					List<Integer> roomNumList = action.roomAction().getClubTimeOutRoomList();
					if (roomNumList.isEmpty())
					{
						return;
					}
					logger.info("有超时的俱乐部房间个数是："+roomNumList.size());
					final OutputMessage om = new OutputMessage(true);
					for (Integer roomNum : roomNumList)
					{
						//清除玩家的进入房间记录
						int clubId = action.clubRoomAction().getRoomClub(roomNum);
						
						TaurusClub taurusClub = BaseClubServer.getClub(clubId);
						if(taurusClub == null)
						{
							throw new GameException(GameException.CLUB_IS_NOT_EXIST, "俱乐部房间删除超时房间时，俱乐部不存在:clubId is"+clubId);
						}

						/*
						 * 1.公共俱乐部返还玩家的额度
						 * 2.私人俱乐部返还创建房间的玩家额度，同时返还群主钻石
						 * 3.私人俱乐部成员表返还群主的总额度
						 */
						TaurusRoom room = taurusClub.getRoom(roomNum);
						if(room == null)																																																																																																																																														
						{
							throw new GameException(GameException.ROOM_NOT_EXIST, "俱乐部房间删除超时房间时，找不到房间,clubId is"+clubId);
						}
                        
						if(room.getConfig().getGameTime() ==0)
						{
							//退还创建房间时消耗的俱乐部额度
							BaseClubServer.returnClubMemberDiamond(room.getRoomOwnerId(), clubId, room.getConfig().getRoundNum(), room.getConfig().getBankerMode().value,roomNum);
						}
						
						//删除数据库中的房间记录
						action.roomAction().deletePrivateRoom(roomNum);
						action.clubRoomAction().unBindRoomClub(room.getRoomId());
						
						if (room != null)
						{
							synchronized (room) 
							{
								//1.通知桌子内的所有玩家房间超时被解散
								room.sendMessage(ClubGameServer.PROTOCOL_Ser_Timeout_Dissolution_Notice, om);
								Set<String> userIdList = room.getUserIdList();
								//2.移除server中的session
								for (String userId: userIdList)
								{   
									//房间超时解散，玩家回到俱乐部房间列表界面，session没有断开
									//BaseServer.userSessionMap.remove(userId);
									BaseServer.unbindUserRoom(userId);
								}
								
								//2.清除房间
								taurusClub.removeRoom(roomNum);
								BaseServer.removeRoom(String.valueOf(roomNum));
								room.clear();
								RoomManager.addRoom(room);
							}
						}
						
						TaurusTable table = BaseServer.tableMap.get(String.valueOf(roomNum));
						if (table != null)
						{
							synchronized (table) 
							{//清除桌子，
								for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
								{
									TaurusSeat seat = table.getSeat(i);
									if (seat.isCanSitDown())
									{
										continue;
									}
									
									//返回坐下玩家的积分
									TaurusPlayer player = seat.getPlayer();
									
									if(room.getConfig().getGameTime() != 0)
									{
										int playerId = Integer.valueOf(player.getPlayerId());
										int scoreTotal = player.getScoreTotal();
										TaurusClubMember member = taurusClub.getMember(playerId);
										action.clubMemberAction().updateClubMemberCurrentScore(taurusClub.getClubId(), playerId, member.getCurrentScore()+ scoreTotal);
										member.setCurrentScore(member.getCurrentScore()+ scoreTotal);
										
										int temp = taurusClub.getDiamondPercent()*scoreTotal;
										int diamond = temp%100 == 0 ? temp/100 : (temp/100)+1;
										action.userAction().returnRoomOwnerDiamond(player.getPlayerId(), diamond, taurusClub.getClub(), roomNum);
										
									}
									
									BaseServer.unbindUserRoom(player.getPlayerId());// 桌子上的玩家  在掉线的时候 房间解散了 需要清理和房间的绑定信息
									BaseServer.unbindUserTable(player.getPlayerId());
								}
								
								BaseServer.tableMap.remove(String.valueOf(roomNum));
								table.clear();
								TableManager.addTaurusTable(table);
							}
						}
						logger.info("Room "+roomNum +"超时，被解散");
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
	    service.scheduleAtFixedRate(runnable, 0, ROOM_TIMEOUT_DISSOLUTION_TIME_COUNT, TimeUnit.SECONDS);  
	}

}
