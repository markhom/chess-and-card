package com.linyun.game.taurus.timer;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.linyun.bottom.cached.RedisResource;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.common.entity.PrivateRoom;
import com.linyun.common.taurus.eum.BankerMode;
import com.linyun.common.taurus.eum.RoomPayMode;
import com.linyun.game.taurus.service.GameServer;
import com.linyun.middle.common.taurus.manager.RoomManager;
import com.linyun.middle.common.taurus.manager.TableManager;
import com.linyun.middle.common.taurus.room.TaurusRoom;
import com.linyun.middle.common.taurus.server.ActionAware;
import com.linyun.middle.common.taurus.service.BaseServer;
import com.linyun.middle.common.taurus.table.TaurusSeat;
import com.linyun.middle.common.taurus.table.TaurusTable;
import com.linyun.middle.common.taurus.utils.DiamondUtils;

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
					List<String> roomNumList = action.roomAction().getTimeoutRoomList();
					if (roomNumList.isEmpty())
					{
						return;
					}
					
					final OutputMessage om = new OutputMessage(true);
					for (String roomNum : roomNumList)
					{
						//清除玩家的进入房间记录
						RedisResource.deleteDataFromRedis(String.valueOf(roomNum));
						PrivateRoom db_room = action.roomAction().getPrivateRoom(roomNum); 
						//如果是房主支付的话，需要退还房主支付钻石数
						if (db_room.getPayMode() == RoomPayMode.PAY_MODE_ONE.value)
						{
							int diamond = DiamondUtils.getPayDiamond(BankerMode.ValueOf(db_room.getUpBankerMode()), RoomPayMode.ValueOf(db_room.getPayMode()),db_room.getRoundNum());
							action.userAction().returnRoomPayDiamond(String.valueOf(db_room.getRoomOwnerId()), diamond);
						}
						//删除数据库中的房间记录
						action.roomAction().deletePrivateRoom(Integer.valueOf(roomNum));
						
						TaurusRoom room = BaseServer.roomMap.get(roomNum);
						if (room != null)
						{
							synchronized (room) 
							{
								//1.通知桌子内的所有玩家房间超时被解散
								room.sendMessage(GameServer.PROTOCOL_Ser_Timeout_Dissolution_Notice, om);
								Set<String> userIdList = room.getUserIdList();
								//2.移除server中的session
								for (String userId: userIdList)
								{
									BaseServer.userSessionMap.remove(userId);
									BaseServer.unbindUserRoom(userId);
								}
								
								//2.清除房间
								BaseServer.roomMap.remove(roomNum);
								room.clear();
								RoomManager.addRoom(room);
							}
						}
						
						TaurusTable table = BaseServer.tableMap.get(roomNum);
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
									String playerId = seat.getPlayer().getPlayerId();
									BaseServer.unbindUserRoom(playerId);// 桌子上的玩家  在掉线的时候 房间解散了 需要清理和房间的绑定信息
									BaseServer.unbindUserTable(playerId);
								}
								BaseServer.tableMap.remove(roomNum);
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
