package com.linyun.game.taurus.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import com.linyun.bottom.cached.RedisResource;
import com.linyun.bottom.common.exception.GameException;
import com.linyun.bottom.container.Container;
import com.linyun.bottom.container.GameSession;
import com.linyun.bottom.container.NotificationListener;
import com.linyun.bottom.exception.JuiceException;
import com.linyun.bottom.handler.SocketRequest;
import com.linyun.bottom.handler.SocketResponse;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.util.DateUtils;
import com.linyun.bottom.util.InputMessage;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.common.entity.PrivateRoom;
import com.linyun.common.entity.User;
import com.linyun.common.taurus.eum.BankerChooseBaseScore;
import com.linyun.common.taurus.eum.BankerMode;
import com.linyun.common.taurus.eum.BaseScoreType;
import com.linyun.common.taurus.eum.DissolutionStatus;
import com.linyun.common.taurus.eum.GameStatus;
import com.linyun.common.taurus.eum.RoomPayMode;
import com.linyun.common.taurus.eum.RoundNum;
import com.linyun.middle.common.taurus.card.HandCard;
import com.linyun.middle.common.taurus.engine.GameEngine;
import com.linyun.middle.common.taurus.engine.factory.EngineFactory;
import com.linyun.middle.common.taurus.manager.RoomManager;
import com.linyun.middle.common.taurus.manager.TableManager;
import com.linyun.middle.common.taurus.player.TaurusPlayer;
import com.linyun.middle.common.taurus.room.TaurusRoom;
import com.linyun.middle.common.taurus.service.BaseServer;
import com.linyun.middle.common.taurus.table.TaurusSeat;
import com.linyun.middle.common.taurus.table.TaurusTable;
import com.linyun.middle.common.taurus.task.GameTimer;
import com.linyun.middle.common.taurus.utils.DiamondUtils;
import com.linyun.middle.common.taurus.utils.MessageUtils;

public class GameServer extends BaseServer
{
	private static Logger logger = LoggerFactory.getLogger(GameServer.class);
	public static final String ONLINE_PREFIX_GAME = "data_online_game";
	
	public static final Object lock_enterTable = new Object();
	public static final Object lock_enterRoom = new Object();
	
	
	/**---------------------------- 以下协议为客户端发送给服务器的 -------------------------------------*/
	  
	
	/** 加入房间 */
	public static final short PROTOCOL_Cli_Room_Enter = 7001;
	/** 退出房间 */
	public static final short PROTOCOL_Cli_Room_Exit = 7002;
	/** 进入桌子，坐下*/
	public static final short PROTOCOL_Cli_Table_SitDown = 7003;
	/** 退出桌子，离开 */
	public static final short PROTOCOL_Cli_Table_Exit = 7004;
	/** 申请解散，某个玩家申请解散房间 */
	public static final short PROTOCOL_Cli_Room_Dissolution_Apply = 7005;
	/** 解散选择，其他玩家对申请解散房间做出选择 */
	public static final short PROTOCOL_Cli_Room_Dissolution_Choice = 7006;
	/** 游戏结果，房间解散的时候的总的游戏结果 */
	public static final short PROTOCOL_Cli_Room_Result_Info = 7007;
	/** 开始游戏，只有房主点击开始游戏 */
	public static final short PROTOCOL_Cli_Start = 7008;
	/** 准备,在一局游戏完成后、下一局游戏开始之前可进行的操作 */
	public static final short PROTOCOL_Cli_Ready = 7009;
	/** 庄家选择押注底分情况，在创建房间时，底分模式选择了"庄家选择"时触发 */
	public static final short PROTOCOL_Cli_Banker_Choose_BaseCoin = 7010;
	/** 押注，玩家一局的下注分数 */
	public static final short PROTOCOL_Cli_Bet_Coin = 7011;
	/** 亮牌，客户端选择确认亮牌 */
	public static final short PROTOCOL_Cli_Open_Cards = 7012;
	/** 玩家抢庄，在庄家模式为自由抢庄和明牌抢庄的时候使用 */
	public static final short PROTOCOL_Cli_Rob_Banker = 7013;
	/** 房主解散房间，由客户端发起 */
	public static final short PROTOCOL_Cli_Owner_Dissolution = 7014;
	/** 玩家进行托管操作 */
	public static final short PROTOCOL_Cli_Auto_Action = 7015;
	/** 玩家拉取上局局号 */
	public static final short PROTOCOL_Cli_Last_Round_Index = 7016;
	/** 固定庄家下庄协议 */
	public static final short PROTOCOL_Cli_FIXED_Banker_Close_Game = 7017;
	
	/**
	 * ----------------------------------------end------------------------------
	 * ----------------
	 */
	/**
	 * ---------------------------- 以下协议为服务器发送给客户端的 * -------------------------------------
	 */
	/** 进入桌子，坐下 */
	public static final short PROTOCOL_Ser_Table_SitDown = 8001;
	/** 退出桌子，离开 */
	public static final short PROTOCOL_Ser_Table_Exit = 8002;
	/** 申请解散，某个玩家申请解散房间 */
	public static final short PROTOCOL_Ser_Room_Dissolution_Apply = 8003;
	/** 解散选择，其他玩家对申请解散房间做出选择 */
	public static final short PROTOCOL_Ser_Room_Dissolution_Choice = 8004;
	/** 解散结果，对一个解散申请最后的处理结果 */
	public static final short PROTOCOL_Ser_Room_Dissolution_Result = 8005;
	/** 游戏开始，房主开始游戏 */
	public static final short PROTOCOL_Ser_Game_Start = 8006;
	/** 游戏状态，当游戏状态发生变化的时候发送客户端 */
	public static final short PROTOCOL_Ser_Game_Status_Changed = 8007;
	/** 准备,在一局游戏完成后、下一局游戏开始之前可进行的操作 */
	public static final short PROTOCOL_Ser_Ready = 8008;
	/** 玩家抢庄，在庄家模式为自由抢庄和明牌抢庄的时候使用 */
	public static final short PROTOCOL_Ser_Rob_Banker = 8009;
	/** 庄家确认，在庄家确定之后通知客户端庄家信息 */
	public static final short PROTOCOL_Ser_Banker_Info = 8010;
	/** 庄家选择押注底分情况，在创建房间时，底分模式选择了"庄家选择"时触发 */
	public static final short PROTOCOL_Ser_Banker_Choose_BaseCoin = 8011;
	/** 押注，玩家一局的下注分数 */
	public static final short PROTOCOL_Ser_Bet_Coin = 8012;
	/** 发牌，将最多6个位置上的玩家的牌信息分别发送给玩家 */
	public static final short PROTOCOL_Ser_Deal_Cards = 8013;
	/** 亮牌，客户端选择确认亮牌 */
	public static final short PROTOCOL_Ser_Open_Cards = 8014;
	/** 比牌，进行玩家的牌型计算，将比牌结果<也就是游戏结果>发送给客户端,一局的比牌结果 */
	public static final short PROTOCOL_Ser_Compare_Cards_Result = 8015;
	/** 房主解散房间，通知房间内的其他人 */
	public static final short PROTOCOL_Ser_Owner_Dissolution_Notice = 8016;
	/** 房间超时被解散，通知房间内的所有人 */
	public static final short PROTOCOL_Ser_Timeout_Dissolution_Notice = 8017;
	/** 观战玩家收到发牌协议 */
	public static final short PROTOCOL_Ser_Deal_Cards_Watch = 8018;
	/** 明牌抢庄发牌，将最多6个位置上的玩家的牌信息分别发送给玩家 */
	public static final short PROTOCOL_Ser_Rob_Banker_Deal_Cards = 8021;
	/** 观战玩家收到明牌抢庄发牌协议 */
	public static final short PROTOCOL_Ser_ROB_Banker_Deal_Cards_Watch = 8022;
	/** 固定庄家下庄通知客户端 */
	public static final short PROTOCOL_Ser_FIXED_Banker_Close_Game = 8023;
	/** 固定庄家庄家分数不足通下庄通知客户端 */
	public static final short PROTOCOL_Ser_FIXED_Banker_No_Enough_Coin_Close_Game = 8024;
	/**
	 * ----------------------------------------end------------------------------
	 * ----------------
	 */
	/** 游戏内的重连协议 */
	public static final short PROTOCOL_Cli_reconnect = 9001;
	/** 游戏外重连 */
	public static final short PROTOCOL_Cli_reconnect_out = 9002;

	/** 玩家掉线通知客户端 */
	public static final short PROTOCOL_Ser_Disconnect = 9011;
	/** 玩家掉线重连回来通知客户端 */
	public static final short PROTOCOL_Ser_reconnect = 9012;
	/**通知玩家在其他设备上登录*/
	public static final short PROTOCOL_Ser_Outconnect = 9013 ;
	/**玩家被冻结协议*/
	public static final short PROTOCOL_Ser_Frozen = 9015 ;

	public GameServer()
	{
		Container.addNotificationListener(new NotificationListener()
		{
			@Override
			public void handler(Object obj) throws JuiceException
			{
				// 进入房间后，退出房间前掉线
				if (obj instanceof GameSession)
				{
					GameSession session = (GameSession) obj;
					if ((String)session.getObject(USER_ID) != null)
					{
						clearSession(session);
					}
				} 
				else if (obj instanceof String)
				{
				
				}
			}
		});
	}
	
	private void clearSession(GameSession session)
	{
		Semaphore sem = null;
		try
		{
			synchronized (session)
			{
				String userId = (String) session.getObject(USER_ID);
				logger.info("玩家" + userId + "<掉线>");
				if (userId == null)
				{
					return;
				}
				sem = getSemp(userId);
				sem.acquire();
				// ---------------------------------房间-------------------------
				TaurusRoom room = getUserRoom(userId);
				if (room != null)
				{
					// 固定清除session相关
					room.removeSession(userId);
					
					TaurusTable table = getUserTable(userId);
					if (table == null)
					{// 房间内未加入桌子的玩家
						logger.info("玩家" + userId + "未加入桌子，在房间内掉线，" + room.getRoomId());
						unbindUserRoom(userId);
					}
					else
					{//桌子内的玩家
						logger.info("玩家" + userId + "加入桌子，在房间内掉线,"+room.getRoomId());
						TaurusSeat seat = table.getUserSeat(userId);
						seat.setAutoAction(true);
						seat.setOnline(false);

						OutputMessage om = new OutputMessage(true);
						om.putString(userId);
						room.sendMessage(GameServer.PROTOCOL_Ser_Disconnect, om);
					}
				}
			}
		} 
		catch (Exception e)
		{
			logger.error("有玩家<掉线>退出房间时，发生错误");
			logger.error(e.getMessage(),e);
		}
		finally 
		{
			removeUser(session);
			if (sem != null)
			{
				sem.release();
			}
		}
	}

	/** 游戏内断线重连 */
	public void reconnect(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession newSession = request.getSession();
		
		Semaphore sem = null; 
		try
		{
			String userId = msg.getUTF();
			sem = getSemp(userId);
			sem.acquire();
			
			logger.info("user " + userId + " reconnect");
			
			TaurusRoom room = getUserRoom(userId);
			if (room == null)
			{
				throw new GameException(GameException.GAME_ERROR_WATCH_PALYER_RECONNECT, "玩家" + userId + "断线重连时，找不到房间");
			}
			TaurusTable table = getUserTable(userId);
			if (table == null)
			{
				throw new GameException(GameException.TABLE_NOT_EXIST, "玩家" + userId + "断线重连时，找不到桌子");
			}

			/** 处理 session， 先清理后加进来 */
			synchronized (room)
			{
				GameSession oldSession = getUserSession(userId);
				if (oldSession != null)
				{
					synchronized (oldSession)
					{
						room.removeSession(userId);
						// baseServer
						removeUser(oldSession);
					}
				}
				
				room.addSession(userId, newSession);
				addUser(userId, newSession);
				table.getUserSeat(userId).setAutoAction(false);

				OutputMessage om = MessageUtils.getReconnectMessage(room, table);
				om.putInt(0);
				om.putInt(0);
				om.putInt(0);
				om.putBoolean(table.getUserSeat(userId)==null ? false : true);
				
				newSession.sendMessage(PROTOCOL_Cli_reconnect, om);
				//通知客户端，该玩家重连回来
				
				OutputMessage other_om = new OutputMessage(true);
				other_om.putString(userId);
				room.sendMessage(PROTOCOL_Ser_reconnect, other_om, newSession);
				logger.info("userId" + userId + "重连" + room.getRoomId() + "成功");
			}
		} 
		catch (GameException e)
		{
			sendError(response, PROTOCOL_Cli_reconnect, e.getId());
			logger.error(e.getMessage(), e.getCause());
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage(),e);
		}
		finally 
		{
			if (sem != null)
			{
				sem.release();
			}
		}
	}

	/** 游戏外重连 */
	public void outReconnect(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession newSession = request.getSession();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			sem = getSemp(userId);
			sem.acquire();
			logger.info("用户" + userId + " 游戏外重连");
			
			TaurusRoom room = getUserRoom(userId);
			if (room == null)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST, "玩家" + userId + "游戏外断线重连时，找不到房间");
			}
			TaurusTable table = getUserTable(userId);
			if (table == null)
			{
				throw new GameException(GameException.TABLE_NOT_EXIST, "玩家" + userId + "游戏外断线重连时，找不到房间");
			}

			synchronized (room)
			{
				/** 处理 session， 先清理后加进来 */
				GameSession oldSession = getUserSession(userId);
				if (oldSession != null)
				{
					synchronized (oldSession)
					{
						oldSession.sendMessage(GameServer.PROTOCOL_Ser_Outconnect, new OutputMessage(true));
						room.removeSession(userId);
						// baseServer
						removeUser(oldSession);
						
					}
				}
				
				room.addSession(userId, newSession);
				addUser(userId, newSession);
				table.getUserSeat(userId).setAutoAction(false);

				/** 游戏外重连 */
				OutputMessage om = MessageUtils.getOutReconnectMessage(room, table);
				//加一个玩家是否可以坐下判断
				boolean noEnter = room.getConfig().getAdvancedOptions().isNoEnter();
				om.putBoolean(room.isGameStart() && noEnter ? false : true);

				newSession.sendMessage(PROTOCOL_Cli_reconnect_out, om);
				
				OutputMessage other_om = new OutputMessage(true);
				other_om.putString(userId);
				room.sendMessage(PROTOCOL_Ser_reconnect, other_om, newSession);
				logger.info("用户" + userId + "游戏外重连" + room.getRoomId() + "成功");
			}
		}
		catch (GameException e)
		{
			logger.error(e.getMessage(), e.getCause());
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage(),e);
		}
		finally 
		{
			if (sem != null)
			{
				sem.release();
			}
		}
	}

	/** 进入房间 */
	public void enterRoom(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		int other_roomId = 0;
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			String roomNum = msg.getUTF();
			int iRoomNum = Integer.valueOf(roomNum);
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in enter room, userId is " + userId + "roomNum is " + roomNum);
			getExistUserBySql(userId);// 校验用户是否存在
			
			TaurusRoom tempRoom = getUserRoom(userId);
			if ((tempRoom != null) && (tempRoom.getRoomId() != iRoomNum))
			{
				other_roomId = getUserRoom(userId).getRoomId();
				throw new GameException(GameException.ROOM_UNFINISHED_GAME,
						"userId:" + userId + "在房间" + other_roomId + "有未完成的牌局");
			}
			
			PrivateRoom db_room = roomAction().getPrivateRoom(iRoomNum);
			
			if (db_room == null || db_room.getClubId() != 0)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST,
						"玩家加入房间时，房间不存在，userId=" + userId + "roomNum=" + roomNum);
			}

			TaurusRoom game_room = null;
			synchronized (lock_enterRoom)
			{
				game_room = getRoom(roomNum);
				if (game_room != null && game_room.ContainUserId(userId))
				{
					//清理玩家旧的session相关
					GameSession oldSession = getUserSession(userId);
					game_room.removeSession(userId);
					unbindUserRoom(userId);
					removeUser(oldSession);
				}

				if (game_room == null)
				{// 第一个玩家加入房间，申请资源,然后保存到管理的地方
					game_room = RoomManager.getRoom();
					if (game_room == null)
					{
						logger.info("room" + roomNum + "不存在，创建房间");
						game_room = new TaurusRoom(db_room);
						addRoom(roomNum, game_room);
					} 
					else
					{
						logger.info("room" + roomNum + "从资源池获取房间");
						logger.info("从资源池中获取的上一个房间没有初始化前的房主："+game_room.getRoomOwnerId());
						game_room.Init(db_room);
						logger.info("从资源池中获取的上一个房间经过初始化后的房主："+game_room.getRoomOwnerId());
						addRoom(roomNum, game_room);
					}
				}
			}
			
			synchronized (game_room)
			{
				game_room.addSession(userId, session);
				addUser(userId, session);
				bindUserRoom(userId, game_room);
				/** 发送客户端需要的数据 */
				TaurusTable table = getTable(roomNum);
				OutputMessage om = MessageUtils.getEnterRoomMessage(game_room.getConfig(), roomNum, game_room, table,game_room.getRoomOwnerId());
				om.putInt(0);
				om.putInt(0);
				om.putInt(0);
				
				//加一个玩家是否可以坐下判断
				boolean noEnter = game_room.getConfig().getAdvancedOptions().isNoEnter();
				om.putBoolean(game_room.isGameStart() && noEnter ? false : true);
				session.sendMessage(PROTOCOL_Cli_Room_Enter, om);
				logger.info("userId" + userId + "进入房间" + roomNum + "成功");
			}
		} 
		catch (GameException e)
		{
			if (e.getId() == GameException.ROOM_UNFINISHED_GAME)
			{
				OutputMessage om = new OutputMessage(false);
				om.putShort(GameException.ROOM_UNFINISHED_GAME);
				om.putString(String.valueOf(other_roomId));
				session.sendMessage(PROTOCOL_Cli_Room_Enter, om);
			}
			else
			{
				sendError(response, PROTOCOL_Cli_Room_Enter, e.getId());
			}
			
			logger.error(e.getMessage(), e.getCause());
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage(),e);
		}
		finally 
		{
			if (sem != null)
			{
				sem.release();
			}
		}
	}

	/** 退出房间 */
	public void exitRoom(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		
		String userId = null;
		GameSession session = request.getSession();
		boolean isCanExit = true;
		Semaphore sem = null;
		try
		{
			userId = msg.getUTF();
			String roomNum = msg.getUTF();
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in exit room, userId is " + userId + "roomNum is " + roomNum);
			
			TaurusRoom game_room = getRoom(roomNum);
			if ((getUserTable(userId) != null)&&(game_room != null) && (game_room.isGameStart()))
			{
				isCanExit = false;
				throw new GameException(GameException.GAME_ERROR_Game_Statred_Not_Exit,
						"玩家userId" + userId + "退出房间失败，游戏已经开始");
			}

			if (game_room != null)
			{// 玩家退出房间时，将玩家的session相关从BaseServer移除
				synchronized (game_room)
				{
					game_room.removeSession(userId);
					/** 需要判断房间对应的桌子是否存在，如果桌子存在则不能清理 */
					if (game_room.getSessionList().isEmpty() && getTable(String.valueOf(game_room.getRoomId()))==null)
					{// 房间内已经没有人了，将房间销毁
						removeRoom(roomNum);
						game_room.clear();
						RoomManager.addRoom(game_room);
					}
				}

				if (getUserTable(userId) != null)
				{
					synchronized (getUserTable(userId))
					{
						TaurusTable table = getExistTable(roomNum);
						int locationId = table.getUserLocation(userId);
						exitExistTable(userId, table);
						OutputMessage om = new OutputMessage(true);
						om.putString(userId);
						om.putByte((byte) locationId);
						game_room.sendMessage(PROTOCOL_Ser_Table_Exit, om, session);
					}
				}
			}
		} catch (GameException e)
		{
			sendError(response, PROTOCOL_Cli_Room_Exit, e.getId());
			logger.error(e.getMessage(), e.getCause());
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage(),e);
		} 
		finally
		{
			if (isCanExit)
			{
				synchronized (session)
				{
					session.sendMessage(PROTOCOL_Cli_Room_Exit, new OutputMessage(true));
					removeUser(session);
					if (userId != null)
					{
						unbindUserRoom(userId);
					}
				}
			}
			if (sem != null)
			{
				sem.release();
			}
		}
	}

	/** 坐下，进入桌子 */
	public void enterTable(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		Semaphore sem = null; 
		try
		{
			String userId = msg.getUTF();
			String tableId = msg.getUTF(); // 作为房间号码
			byte seatId = msg.getByte(); //座位号
			int iTableId = Integer.valueOf(tableId); 
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in enter table, userId is " + userId + ",tableId is " + tableId);
			GameSession session = request.getSession();
			
			getExistUser(userId);// 校验用户是否存在

			// ---------------------------房间-------------------------------------
			TaurusRoom game_room = getUserRoom(userId);
			if (game_room == null)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST, "玩家在加入桌子时，房间不存在，userId="+userId + ",roomId="+tableId);
			}

			User user = userAction().getExistUser(userId);
			int diamond = user.getDiamond();
			if (game_room.getConfig().getPayMode() == RoomPayMode.PAY_MODE_ALL)
			{// AA支付需要校验玩家的房费
				if ((game_room.getConfig().getBankerMode() != BankerMode.BANKER_MODE_ROTATE)
						&& (game_room.getConfig().getRoundNum() == RoundNum.ROUND_NUM_20))
				{// 不是轮庄模式，并且局数为20局的时候
					// ---
					// 需要支付2个钻石
					if (diamond < 2)
					{
						throw new GameException(GameException.TABLE_ENTER_ERROR_DIAMOND_NOT_ENOUGH, "玩家在加入桌子时，钻石不足，userId="+userId + ",需要钻石："+2+",玩家钻石"+diamond);
					}
				}
				else
				{//1.轮庄模式或者 2.非轮庄模式局数为10局的时候 --- 需要支付1个钻石
					if (diamond < 1)
					{
						throw new GameException(GameException.TABLE_ENTER_ERROR_DIAMOND_NOT_ENOUGH, "玩家在加入桌子时，钻石不足，userId="+userId + ",需要钻石："+1+",玩家钻石"+diamond);
					}
				}
			}
			
			//---------------------------桌子-------------------------------------
			TaurusTable user_table = getUserTable(userId);
			if ((user_table!=null) && (!user_table.getTableId().equals(tableId)))
			{
				throw new GameException(GameException.ROOM_UNFINISHED_GAME, "userId:" + userId + "在桌子" + user_table.getTableId() + "有未完成的牌局");
			}
			
			TaurusTable game_table = null;
			synchronized (lock_enterTable) 
			{
				game_table = getTable(tableId);
				if (game_table != null)
				{
					if (game_table.containsUser(userId))
					{
						throw new GameException(GameException.TABLE_REPEAT_JOIN, "玩家在加入桌子时，重复加入桌子，userId="+userId+",roomId="+tableId);
					}
					if (game_room.isGameStart() && game_room.getConfig().getBankerMode() == BankerMode.BANKER_MODE_ROTATE)
					{//房间已经开始游戏并且房间时轮庄模式的时候
						throw new GameException(GameException.TABLE_ENTER_ERROR_ROTATE_BANKER_START, "玩家在加入桌子时，轮庄模式时，游戏已经开始，不能加入，userId="+userId+",roomId="+tableId);
					}
					if (game_room.isGameStart() && (game_room.getConfig().getAdvancedOptions().isNoEnter()))
					{
						throw new GameException(GameException.TABLE_ENTER_ERROR_SET_START_NO_ENTER, "玩家在加入桌子时，房间内游戏已经开始，设置了游戏开始后禁止加入，不能加入，userId="+userId+",roomId="+tableId);
					}
					
					if (!game_table.isCanEnter())
					{
						throw new GameException(GameException.TABLE_ENTER_ERROR_TABLE_IS_FULL, "玩家在加入桌子时，桌子已满，不能加入，userId="+userId);
					}
				}
				
				if (game_table == null)
				{//第一个玩家加入房间，申请资源,然后保存到管理的地方
					//获取自己加入桌子时需要的数据
					logger.info("桌子" + tableId + "不存在，创建桌子");
					//从资源池中获取对象
				    game_table = TableManager.getTaurusTable();
				    if(game_table == null)
				    {
				    	game_table = new TaurusTable(tableId);
				    	addTable(game_table);
				    }
				    else //资源池中已经有存在的桌子对象，桌子的所有数据已经被清空
				    {   
				    	logger.info("从资源库获取对象，没有初始化前的桌子号是："+game_table.getTableId());
				    	game_table.Init(tableId);
				    	logger.info("从资源库获取对象，经过初始化之后的桌子号是:"+game_table.getTableId());
				    	addTable(game_table);
				    }
				}
				TaurusPlayer player = new TaurusPlayer(tableId, userId);
			    boolean addPlayer = game_table.addPlayer(player, user, seatId);
			    
			    if(!addPlayer)
			    {
			    	throw new GameException(GameException.GAME_ERROR_SEAT_CAN_NOT_SITDOWN, "玩家在加入桌子时，对应位置坐不下，userId="+userId);
			    }
			}
			
			synchronized (game_table)
			{   
				bindUserTable(userId, game_table);
				OutputMessage self_om = MessageUtils.getSelfEnterTableMessage(game_room, game_table, seatId);
				session.sendMessage(PROTOCOL_Cli_Table_SitDown, self_om);
				
				//通知别人自己加入房间
				OutputMessage other_om = new OutputMessage(true);
				other_om.putByte((byte)game_table.getUserLocation(userId));
				other_om.putString(userId);
				other_om.putString(user.getNickName());
				other_om.putString(user.getHeadImgUrl());
				other_om.putString(user.getLoginAddress());
				other_om.putString(user.getLoginIp());
				other_om.putInt(user.getRoundNum());
				other_om.putString(DateUtils.getFormatDay(user.getRegisterTime()));
				game_room.sendMessage(PROTOCOL_Ser_Table_SitDown, other_om, session);
				//更新数据库中的房间中的已经坐下的玩家人数
				roomAction().addOnePlayer(iTableId);
			}
		}
		catch (GameException e)
		{
			sendError(response, PROTOCOL_Cli_Table_SitDown, e.getId());
			logger.error(e);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(),e);
		}
		finally 
		{
			if (sem != null)
			{
				sem.release();
			}
		}
	}
//	/** 离开桌子 */
//	public void exitTable(SocketRequest request, SocketResponse response)
//	{
//		InputMessage msg = request.getInputMessage();
//		String userId = msg.getUTF();
//		String tableId = msg.getUTF();  //作为房间号码
//		
//		logger.info("in exit table, userId is " + userId + ",tableId is " + tableId );
//		GameSession session = request.getSession();
//		try
//		{   
//			
//		}
//		catch (GameException e)
//		{
//			logger.error(e.getMessage(), e.getCause());
//		}
//		catch (Exception e)
//		{
//			logger.error(e);
//		}
//	}
	/** 离开桌子 */
	private void exitExistTable(String userId, TaurusTable table) throws GameException
	{
		roomAction().subOnePlayer(Integer.valueOf(table.getTableId()));
		table.removePlayer(userId);
		unbindUserTable(userId);
		if (table.getRealPlayer() == 0)
		{
			//table.destory();
			//桌子上已经没有人了，销毁桌子
			removeTable(table);
			table.clear();
			TableManager.addTaurusTable(table);
		}
	}
	
	/** 房主解散房间 */
	public void roomOwnerDissolution(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			String roomNum = msg.getUTF();
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in roomOwnerDissolution , userId is " + userId + ",roomNum is " + roomNum );
			
			getExistUser(userId);//校验用户是否存在
			
			TaurusRoom room = getRoom(roomNum);
			if (room == null)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST, "房主解散房间失败，找不到对应的房间,userId=" + userId + ",RoomNum=" + roomNum);
			}
			
			synchronized (room)
			{
				if (room.getRoomOwnerId() != Integer.valueOf(userId))
				{
					throw new GameException(GameException.ROOM_DISSOLUTION_ERROR_ISNOT_OWNER, "房主解散房间失败，不是当前房间的房主,userId=" + userId + ",RoomNum=" + roomNum);
				}
				if (room.isGameStart())
				{
					throw new GameException(GameException.ROOM_DISSOLUTION_ERROR_GAME_START, "房主解散房间失败，游戏牌局已经开始");
				}
				
				TaurusTable table = getTable(roomNum);
				if (table != null)
				{
					for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
					{
						TaurusSeat seat = table.getSeat(i);
						if (seat.isCanSitDown())
						{
							continue;
						}
						
						unbindUserTable(seat.getPlayer().getPlayerId());
					}
					//table.destory();
					removeTable(table);
					table.clear();
					TableManager.addTaurusTable(table);
				}
				
				if (room.getConfig().getPayMode() == RoomPayMode.PAY_MODE_ONE)
				{
					//支付方式为房主支付时，房主解散房间，需要退还房主钻石
					int diamond = DiamondUtils.getPayDiamond(room.getConfig().getBankerMode(), room.getConfig().getPayMode(), room.getConfig().getRoundNum());
					userAction().returnRoomPayDiamond(userId, diamond);
				}
				
				//清除玩家的进入房间记录
				RedisResource.deleteDataFromRedis(String.valueOf(roomNum));
				//删除数据库中的房间记录
				roomAction().deletePrivateRoom(Integer.valueOf(roomNum));
				
				//通知所有人房间已解散
				OutputMessage om = new OutputMessage(true);
				//这里发送客户端需要的数据到客户端
				room.sendMessage(PROTOCOL_Ser_Owner_Dissolution_Notice, om);
				
				List<GameSession> sessions = room.getSessionList();
				for (int i=0; i<sessions.size(); ++i)
				{
					GameSession session_destory = sessions.get(i);
					removeUser(session_destory);
				}
				Set<String> userIdList = room.getUserIdList();
				for (String userId1: userIdList)
				{
					unbindUserRoom(userId1);
				}
				
				room.clear();
				RoomManager.addRoom(room);
				removeRoom(roomNum);
			}
		}
		catch (GameException e)
		{
			sendError(response, PROTOCOL_Cli_Owner_Dissolution, e.getId());
			logger.error(e.getMessage(), e.getCause());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(),e);
		}
		finally 
		{
			if (sem != null)
			{
				sem.release();
			}
		}
	}
	
	/** 申请解散房间 */
	public void applyDissolution(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		Semaphore sem = null;
		try
		{   
			String userId = msg.getUTF();
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in applyDissolution , userId is " + userId );
			
			getExistUser(userId);//校验用户是否存在
			TaurusTable table = getUserTable(userId);
			if (table == null)
			{
				throw new GameException(GameException.START_GAME_ERROR_TABLE_NOT_EXIST, "玩家申请解散房间失败，桌子不存在,userId=" + userId);
			}
			TaurusRoom room = getRoom(table.getTableId());
			if (room == null)
			{
				throw new GameException(GameException.START_GAME_ERROR_ROOM_NOT_EXIST, "玩家申请解散房间失败，房间不存在,userId=" + userId + ",RoomNum=" + table.getTableId());
			}
			if (!room.isGameStart())
			{
				throw new GameException(GameException.GAME_ERROR_DISSOLUTION_GAME_IS_NOT_START, "玩家申请解散房间失败，游戏未开始，不能申请解散,userId=" + userId + ",RoomNum=" + table.getTableId());
			}
			
			//校验是否在解散阶段
			if (table.isDissolutionStage())
			{
				throw new GameException(GameException.GAME_ERROR_IN_DISSOLUTION_STAGE, "玩家申请解散房间失败，房间处于解散阶段");
			}
			
			/** 下面是申请解散房间的相关处理 */
			//1.设置当前玩家的解散状态为同意解散
			table.getUserSeat(userId).setDissolutionStatus(DissolutionStatus.DISSOLUTION_AGREE);
			//2.设置当前桌子的解散状态为解散阶段
			table.setDissolutionStage(true);
			//3.通知其他在座玩家，有人申请解散房间，请做出选择
			OutputMessage other_om = new OutputMessage(true);
			other_om.putString(userId);
			room.sendMessage(PROTOCOL_Ser_Room_Dissolution_Apply, other_om);
		}
		catch (GameException e)
		{
			sendError(response, PROTOCOL_Cli_Room_Dissolution_Apply, e.getId());
			logger.error(e.getMessage(), e.getCause());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(),e);
		}
		finally 
		{
			if (sem != null)
			{
				sem.release();
			}
		}
	}
	/** 对解散房间的申请作出选择 */
	public void chooseDissolution(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		Semaphore sem = null;
		try
		{   
			String userId = msg.getUTF();
			boolean choice = msg.getBoolean();//0或者1   1-同意，0-不同意
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in chooseDissolution , userId is " + userId + "choice is" + choice);
			
			getExistUser(userId);//校验用户是否存在
			
			TaurusTable table = getUserTable(userId);
			if (table == null)
			{
				throw new GameException(GameException.START_GAME_ERROR_TABLE_NOT_EXIST, "玩家对解散房间的申请的选择失败，桌子不存在,userId=" + userId);
			}
			TaurusRoom room = getRoom(table.getTableId());
			if (room == null)
			{
				throw new GameException(GameException.START_GAME_ERROR_ROOM_NOT_EXIST, "玩家对解散房间的申请的选择失败，房间不存在,userId=" + userId + ",RoomNum=" + table.getTableId());
			}
			if (!room.isGameStart())
			{
				throw new GameException(GameException.GAME_ERROR_DISSOLUTION_GAME_IS_NOT_START, "玩家选择解散房间失败，游戏未开始，不能对申请解散做出选择,userId=" + userId + ",RoomNum=" + table.getTableId());
			}
			
			//校验是否还在解散阶段
			if (!table.isDissolutionStage())
			{
				throw new GameException(GameException.GAME_ERROR_NOT_IN_DISSOLUTION_STAGE, "玩家对解散房间的申请的选择失败，房间未处于解散阶段");
			}
			
			/** 下面是对解散房间申请的选择的相关处理 */
			if (choice)
			{//同意解散房间
				DissolutionStatus dissolutionStatus = DissolutionStatus.DISSOLUTION_AGREE;
				//1.设置当前玩家的解散状态为同意解散
				table.getUserSeat(userId).setDissolutionStatus(dissolutionStatus);
			}
			//2.通知其他在座玩家，玩家对解散房间申请的选择结果
			OutputMessage other_om = new OutputMessage(true);
			other_om.putString(userId);
			other_om.putBoolean(choice);
			room.sendMessage(PROTOCOL_Ser_Room_Dissolution_Choice, other_om);
			
			if (!choice)
			{//不同意，一票否决
				//1.清理解散相关的资源
				table.clearDissolution();
				//2.通知其他在座玩家，不同意解散房间
				OutputMessage om = new OutputMessage(true);
				other_om.putBoolean(false);
				room.sendMessage(PROTOCOL_Ser_Room_Dissolution_Result, om);
				return;
			}
		}
		catch (GameException e)
		{
			sendError(response, PROTOCOL_Cli_Room_Dissolution_Choice, e.getId());
			logger.error(e.getMessage(), e.getCause());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(),e);
		}
		finally 
		{
			if (sem != null)
			{
				sem.release();
			}
		}
	}
	/** 开始游戏 ，只有房主才能开始游戏 */
	public void startGame(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{   
			String userId = msg.getUTF();
			String strRoomNum = msg.getUTF();
			int roomNum = Integer.valueOf(strRoomNum);
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in start game, userId is " + userId + ",tableId is " + roomNum );
			
			getExistUser(userId);//校验用户是否存在
			
			TaurusRoom room = getRoom(strRoomNum);
			if (room == null)
			{
				throw new GameException(GameException.START_GAME_ERROR_ROOM_NOT_EXIST, "房主开始游戏失败，房间不存在,userId=" + userId + ",RoomNum=" + roomNum);
			}
			if (room.isGameStart())
			{
				throw new GameException(GameException.GAME_ERROR_ROOM_OWNER_START_GAME_STARTED, "房主开始游戏失败，游戏已经开始,userId=" + userId + ",RoomNum=" + roomNum);
			}
			if (room.getRoomOwnerId() != Integer.valueOf(userId))
			{
				throw new GameException(GameException.START_GAME_ERROR_IS_NOT_OWNER, "房主开始游戏失败，不是当前房间的房主,userId=" + userId + ",RoomNum=" + roomNum);
			}
			TaurusTable table = getTable(strRoomNum);
			if (table == null)
			{
				throw new GameException(GameException.START_GAME_ERROR_TABLE_NOT_EXIST, "房主开始游戏失败，桌子不存在,userId=" + userId + ",RoomNum=" + roomNum);
			}
			if (table.getRealPlayer() < 2)
			{
				throw new GameException(GameException.START_GAME_ERROR_PLAYER_COUNT_ERROR, "房主开始游戏失败，桌子上的玩家数量少于两人,userId=" + userId + ",RoomNum=" + roomNum);
			}
			
			/** 下面是开始游戏的相关处理 */
			if (room.getConfig().getBankerMode() == BankerMode.BANKER_MODE_ROTATE || room.getConfig().getAdvancedOptions().isNoEnter())
			{//轮庄模式 或者 选择了游戏开始后禁止加入游戏时， 将桌子的状态设置为禁止加入
				table.setCanEnter(false);
			}
			
			if (room.getConfig().getBankerMode() == BankerMode.BANKER_MODE_ROTATE)
			{  //轮庄模式
				byte roundTotal = (byte)(table.getRealPlayer()*3);
				room.getConfig().setRoundNum(roundTotal);//轮庄模式需要在开始游戏的时候 根据开局的人数来确定总局数
				//需要更新总局数到数据库
				roomAction().updateRoundNum(roomNum, roundTotal);
			}
			
			GameEngine engine = EngineFactory.newEngine(table, room);
			GameTimer.getInstance().addEngine(engine);
			room.setGameStatus(GameStatus.GAME_STATUS_TABLE_READY);
			table.startGame();
			roomAction().startGame(roomNum);
			
			session.sendMessage(PROTOCOL_Cli_Start, new OutputMessage(true));
			
			//通知所有玩家游戏开始
			OutputMessage om = new OutputMessage(true);
			om.putInt(1);//第一局
			om.putByte(GameStatus.GAME_STATUS_TABLE_READY.value);
			if(room.getConfig().getBankerMode() == BankerMode.BANKER_MODE_ROTATE)
			{
				om.putInt(room.getConfig().getRoundNum());//轮庄模式时，发送最终局数人数X3
			}
			room.sendMessage(PROTOCOL_Ser_Game_Status_Changed, om);
		}
		catch (GameException e)
		{
			sendError(response, PROTOCOL_Cli_Start, e.getId());
			logger.error(e.getMessage(), e.getCause());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(),e);
		}
		finally 
		{
			if (sem != null)
			{
				sem.release();
			}
		}
	}
	
	/** 准备 */
	public void ready(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in ready, userId is " + userId);
			getExistUser(userId);//校验用户是否存在
			
			TaurusTable table = getUserTable(userId);
			if (table == null)
			{
				throw new GameException(GameException.TABLE_NOT_EXIST, "玩家准备时，找不到桌子,userId =" + userId);
			}
			
			TaurusRoom room = getRoom(table.getTableId());
			if (room == null)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST, "玩家准备时，找不到房间,userId =" + userId);
			}
			
			if (!table.userIsExist(userId))
			{
				throw new GameException(GameException.GAME_ERROR_USER_NOT_EXIST, "玩家准备时，在桌子内找不到对应的玩家,userId =" + userId + ",tableId=" + table.getTableId());
			}
			//校验是否在解散阶段
			if (table.isDissolutionStage())
			{
				throw new GameException(GameException.GAME_ERROR_IN_DISSOLUTION_STAGE_OTHER, "玩家准备时，房间处于解散阶段，userId=" + userId);
			}
			
			if (table.getUserSeat(userId).isReady())
			{
				throw new GameException(GameException.GAME_ERROR_REPEAT_READY, "玩家准备时，重复准备，userId=" + userId);
			}
			
			table.userReady(userId);
			
			//通知准备
			OutputMessage other_om = new OutputMessage(true);
			other_om.putString(userId);
			room.sendMessage(PROTOCOL_Ser_Ready, other_om);
		}
		catch (GameException e)
		{
			sendError(response, PROTOCOL_Cli_Ready, e.getId());
			logger.error(e.getMessage(), e.getCause());
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage(),e);
		}
		finally 
		{
			if (sem != null)
			{
				sem.release();
			}
		}
	}
	/** 庄家选择底分 */
	public void bankerChooseBaseCoin(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{ 
			String userId = msg.getUTF();
			byte baseCoin = msg.getByte();
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in bankerChooseBaseCoin, userId is " + userId + "baseCoin is " + baseCoin);
			getExistUser(userId);//校验用户是否存在
			
			TaurusTable table = getUserTable(userId);
			if (table == null)
			{
				throw new GameException(GameException.TABLE_NOT_EXIST, "庄家选择押注底分时，找不到桌子,userId =" + userId);
			}
			
			TaurusRoom room = getRoom(table.getTableId());
			if (room == null)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST, "庄家选择押注底分时，找不到房间,userId =" + userId);
			}
			if (room.getConfig().getBaseScore() != BaseScoreType.MODE_BANKER_CHOICE)
			{
				throw new GameException(GameException.GAME_ERROR_ROOM_BASESCORE_IS_NOT_BANKER, "庄家选择押注底分时，找不到房间,userId =" + userId);
			}
			
			if (!table.userIsExist(userId))
			{
				throw new GameException(GameException.GAME_ERROR_USER_NOT_EXIST, "庄家选择押注底分时，在桌子内找不到对应的玩家,userId =" + userId + ",tableId=" + table.getTableId());
			}
			if (!table.isBanker(userId))
			{
				throw new GameException(GameException.GAME_ERROR_USER_IS_NOT_BANKER, "庄家选择底分押注时，不是当前桌子的庄家，userId = " + userId);
			}
			//校验是否在解散阶段
			if (table.isDissolutionStage())
			{
				throw new GameException(GameException.GAME_ERROR_IN_DISSOLUTION_STAGE_OTHER, "庄家选择押注底分时，房间处于解散阶段，userId=" + userId);
			}
			
			if (table.isChooseBaseCoin())
			{
				throw new GameException(GameException.GAME_ERROR_REPEAT_CHOOSE_BASESCORE, "庄家选择押注底分时，重复选择底分，userId=" + userId);
			}
			
			BankerChooseBaseScore baseScore = BankerChooseBaseScore.ValueOf(baseCoin);
			if (baseScore == BankerChooseBaseScore.BANKER_BASE_SCORE_0)
			{
				throw new GameException(GameException.GAME_ERROR_CHOOSE_BASE_COIN, "庄家选择押注底分时，错误的游戏底分选项，userId=" + userId);
			}
			/** 进行底分选择的相关处理 */
			//房间的实际底分处理，底分选项
			table.setChooseBaseCoin(baseScore);
			
			//通知所有人 庄家的底分选择情况
			OutputMessage all_om = new OutputMessage(true);
			all_om.putByte(baseScore.value);
			session.sendMessage(PROTOCOL_Ser_Banker_Choose_BaseCoin, all_om);
		}
		catch (GameException e)
		{
			sendError(response, PROTOCOL_Cli_Banker_Choose_BaseCoin, e.getId());
			logger.error(e.getMessage(), e.getCause());
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage(),e);
		}
		finally 
		{
			if (sem != null)
			{
				sem.release();
			}
		}
	}
	
	/** 押注 */
	public void bet(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			int bet_coin = msg.getInt();
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in bet, userId is " + userId + "bet_coin is " + bet_coin);
			getExistUser(userId);//校验用户是否存在
			
			TaurusTable table = getUserTable(userId);
			if (table == null)
			{
				throw new GameException(GameException.TABLE_NOT_EXIST, "玩家押注时，找不到桌子,userId =" + userId);
			}
			
			TaurusRoom room = getRoom(table.getTableId());
			if (room == null)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST, "玩家押注时，找不到房间,userId =" + userId);
			}
			if (!table.userIsExist(userId))
			{
				throw new GameException(GameException.GAME_ERROR_USER_NOT_EXIST, "玩家押注时，在桌子内找不到对应的玩家,userId =" + userId + ",tableId=" + table.getTableId());
			}
			//校验是否在解散阶段
			if (table.isDissolutionStage())
			{
				throw new GameException(GameException.GAME_ERROR_IN_DISSOLUTION_STAGE_OTHER, "玩家押注时，房间处于解散阶段，userId=" + userId);
			}
			
			TaurusSeat seat = table.getUserSeat(userId);
			if ((!checkBet(room.getConfig().getBaseScore(), bet_coin)) && (bet_coin!=seat.getCurRoundInjectionScore()))
			{
				throw new GameException(GameException.GAME_ERROR_BET_ERROR_BET_COIN, "玩家押注时，错误的押注筹码，userId=" + userId +",coin:" + bet_coin);
			}
			
			if (seat.isBet())
			{
				throw new GameException(GameException.GAME_ERROR_REPEAT_BET, "玩家押注时，重复押注，userId=" + userId +",coin:" + bet_coin);
			}
				
			seat.getPlayer().setBetCoin(bet_coin);
			seat.bet();
			
			//@need通知其他玩家押注
			OutputMessage other_om = new OutputMessage(true);
			other_om.putString(userId);
			other_om.putInt(bet_coin);
			room.sendMessage(PROTOCOL_Ser_Bet_Coin, other_om);
			
		}
		catch (GameException e)
		{
			sendError(response, PROTOCOL_Cli_Bet_Coin, e.getId());
			logger.error(e.getMessage(), e.getCause());
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage(),e);
		}
		finally 
		{
			if (sem != null)
			{
				sem.release();
			}
		}
	}
	
	private boolean checkBet(BaseScoreType type, int betCoin)
	{
		if (type == BaseScoreType.MODE_1_2)
		{
			return (betCoin==1) || (betCoin==2);
		}
		if (type == BaseScoreType.MODE_2_4)
		{
			return (betCoin==2) || (betCoin==4);
		}
		if (type == BaseScoreType.MODE_4_8)
		{
			return (betCoin==4) || (betCoin==8);
		}
		
		return (betCoin==2)||(betCoin==4)||(betCoin==8)||(betCoin==16)||(betCoin==32);
	}
	
	/** 亮牌 */
	public void openCards(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in openCards, userId is " + userId);
			getExistUser(userId);//校验用户是否存在
			
			TaurusTable table = getUserTable(userId);
			if (table == null)
			{
				throw new GameException(GameException.TABLE_NOT_EXIST, "玩家亮牌时，找不到桌子,userId =" + userId);
			}
			
			TaurusRoom room = getRoom(table.getTableId());
			if (room == null)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST, "玩家亮牌时，找不到房间,userId =" + userId);
			}
			if (!table.userIsExist(userId))
			{
				throw new GameException(GameException.GAME_ERROR_USER_NOT_EXIST, "玩家亮牌时，在桌子内找不到对应的玩家,userId =" + userId + ",tableId=" + table.getTableId());
			}
			//校验是否在解散阶段
			if (table.isDissolutionStage())
			{
				throw new GameException(GameException.GAME_ERROR_IN_DISSOLUTION_STAGE_OTHER, "玩家亮牌时，房间处于解散阶段，userId=" + userId);
			}
			
			TaurusSeat seat = table.getUserSeat(userId); 
			if (seat.isOpenCards())
			{
				throw new GameException(GameException.GAME_ERROR_REPEAT_OPENCARDS, "玩家亮牌时，重复亮牌，userId=" + userId);
			}
			seat.openCards();
					
			//通知其他玩家自己的牌信息
			OutputMessage other_om = new OutputMessage(true);
			other_om.putString(userId);
			HandCard handCard = table.getUserSeat(userId).getCards();
			other_om.putShort(handCard.getCards()[0]);
			other_om.putShort(handCard.getCards()[1]);
			other_om.putShort(handCard.getCards()[2]);
			other_om.putShort(handCard.getCards()[3]);
			other_om.putShort(handCard.getCards()[4]);
			other_om.putByte(handCard.getSpecType().value);
			room.sendMessage(PROTOCOL_Ser_Open_Cards, other_om);
		}
		catch (GameException e)
		{
			sendError(response, PROTOCOL_Cli_Open_Cards, e.getId());
			logger.error(e.getMessage(), e.getCause());
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage(),e);
		}
		finally 
		{
			if (sem != null)
			{
				sem.release();
			}
		}
	}
	
	/** 抢庄,自由抢庄和明牌抢庄的时候存在 */
	public void robBanker(SocketRequest request, SocketResponse response)
	{  
		InputMessage msg = request.getInputMessage();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			byte bankerMode = msg.getByte();
			byte robBankerNum = msg.getByte();
			logger.info("in robBanker, userId is " + userId + ", bankerMode = " + bankerMode + "robBankerNum =" + robBankerNum);
			sem = getSemp(userId);
			sem.acquire();
			getExistUser(userId);//校验用户是否存在
			
			TaurusTable table = getUserTable(userId);
			if (table == null)
			{
				throw new GameException(GameException.TABLE_NOT_EXIST, "玩家抢庄时，找不到桌子,userId =" + userId);
			}
			
			TaurusRoom room = getRoom(table.getTableId());
			if (room == null)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST, "玩家抢庄时，找不到房间,userId =" + userId);
			}
			if (!table.userIsExist(userId))
			{
				throw new GameException(GameException.GAME_ERROR_USER_NOT_EXIST, "玩家抢庄时，在桌子内找不到对应的玩家,userId =" + userId + ",tableId=" + table.getTableId());
			}
			//校验是否在解散阶段
			if (table.isDissolutionStage())
			{
				throw new GameException(GameException.GAME_ERROR_IN_DISSOLUTION_STAGE_OTHER, "玩家抢庄时，房间处于解散阶段，userId=" + userId);
			}
			
			if (table.getUserSeat(userId).isRobBanker())
			{
				throw new GameException(GameException.GAME_ERROR_REPEAT_ROB_BANKER, "玩家抢庄时，重复抢庄，userId=" + userId);
			}
			
			TaurusSeat seat = table.getUserSeat(userId);
			seat.robBanker();
			if (robBankerNum != 0)
			{
				seat.getPlayer().RobBanker();
				if (BankerMode.ValueOf(bankerMode) == BankerMode.BANKER_MODE_BRIGHT_ROB)//明牌抢庄
				{
					seat.getPlayer().setRobBankerNum(robBankerNum);
				}
				else
				{
					seat.getPlayer().RobBanker();
				}
			}
			
			//@need通知其他玩家抢庄情况
			OutputMessage om = new OutputMessage(true);
			om.putString(userId);
			om.putByte(bankerMode);
			om.putByte(robBankerNum);
			room.sendMessage(PROTOCOL_Ser_Rob_Banker, om);
			
		}
		catch (GameException e)
		{
			sendError(response, PROTOCOL_Cli_Rob_Banker, e.getId());
			logger.error(e.getMessage(), e.getCause());
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage(),e);
		}
		finally 
		{
			if (sem != null)
			{
				sem.release();
			}
		}
	}
	
	/** 拉取上局的局号索引 */
	public void getLastRoundIndex(SocketRequest request, SocketResponse response)
	{  
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{   
			String userId = msg.getUTF();
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in getLastRoundIndex, userId is " + userId);
			getExistUser(userId);//校验用户是否存在
			
			TaurusRoom room = getUserRoom(userId);
			if (room == null)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST, "玩家拉取上局索引时，找不到房间,userId =" + userId);
			}
			
			TaurusTable table = getTable(String.valueOf(room.getRoomId()));
			if (table == null)
			{
				throw new GameException(GameException.TABLE_NOT_EXIST, "玩家拉取上局索引时，找不到桌子,userId =" + userId);
			}
			
			String roundIndex = table.getLastRoundIndex();
			
			OutputMessage om = new OutputMessage(true);
			om.putString(roundIndex);
			session.sendMessage(PROTOCOL_Cli_Last_Round_Index, om);
		}
		catch (GameException e)
		{
			sendError(response, PROTOCOL_Cli_Last_Round_Index, e.getId());
			logger.error(e.getMessage(), e.getCause());
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage(),e);
		}
		finally 
		{
			if (sem != null)
			{
				sem.release();
			}
		}
	}
	
	public void autoAction(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			byte isAutoAction = msg.getByte();
			sem = getSemp(userId);
			sem.acquire();
			TaurusTable table = getUserTable(userId);
			if (table == null)
			{
				throw new GameException(GameException.TABLE_NOT_EXIST, "玩家托管时，找不到桌子,userId =" + userId);
			}
			
			TaurusRoom room = getRoom(table.getTableId());
			if (room == null)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST, "玩家托管时，找不到房间,userId =" + userId);
			}
			if (!table.userIsExist(userId))
			{
				throw new GameException(GameException.GAME_ERROR_USER_NOT_EXIST, "玩家托管时，在桌子内找不到对应的玩家,userId =" + userId + ",tableId=" + table.getTableId());
			}
			//校验是否在解散阶段
			if (table.isDissolutionStage())
			{
				throw new GameException(GameException.GAME_ERROR_IN_DISSOLUTION_STAGE_OTHER, "玩家托管时，房间处于解散阶段，userId=" + userId);
			}
			
			table.getUserSeat(userId).setAutoAction(isAutoAction==1);
			
			OutputMessage other_om = new OutputMessage(true);
			other_om.putByte(isAutoAction);
			session.sendMessage(PROTOCOL_Cli_Auto_Action, other_om);
		}
		catch (GameException e)
		{
			sendError(response, PROTOCOL_Cli_Auto_Action, e.getId());
			logger.error(e.getMessage(), e.getCause());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(),e);
		}
		finally 
		{
			if (sem != null)
			{
				sem.release();
			}
		}
	}
	
	/**固定庄家模式时，庄家满三局点击下庄，此时也需要解散房间*/
	public void fixedBankerCloseGame(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			sem = getSemp(userId);
			sem.acquire();
			TaurusRoom room = getUserRoom(userId);
			if (room == null)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST, "玩家下庄[固定庄家]时，找不到房间,userId =" + userId);
			}
			
			TaurusTable table = getUserTable(userId);
			if (table == null)
			{
				throw new GameException(GameException.TABLE_NOT_EXIST, "玩家下庄[固定庄家]时，找不到桌子,userId =" + userId);
			}
			
			if (!table.userIsExist(userId))
			{
				throw new GameException(GameException.GAME_ERROR_USER_NOT_EXIST, "玩家下庄[固定庄家]时，在桌子内找不到对应的玩家,userId =" + userId + ",tableId=" + table.getTableId());
			}
			
			if (room.getConfig().getBankerMode() != BankerMode.BANKER_MODE_FIXED)
			{
				throw new GameException(GameException.GAME_ERROR_BANKER_MODE_NOT_FIXED, "玩家下庄[固定庄家]时，房间模式不是固定庄家模式,userId =" + userId + ",tableId=" + table.getTableId() + ",bankerMode" + room.getConfig().getBankerMode());
			}
			
			if (table.getCurRound() <= 3)
			{
				throw new GameException(GameException.GAME_ERROR_BANKER_ROND_NOT_ENOUGH, "玩家下庄[固定庄家]时，牌局未满三局,userId =" + userId + ",tableId=" + table.getTableId());
			}
			
			HandCard handCard = table.getUserSeat(userId).getCards();
			if (handCard.getCards()[0] != 0)
			{
				throw new GameException(GameException.GAME_DOWNBANKER_ERROR_GAME_START, "玩家下庄[固定庄家]时,牌局已开始,userId =" + userId + ",tableId=" + table.getTableId());
			}
			
			table.downBanker();
//			session.sendMessage(PROTOCOL_Ser_FIXED_Banker_Close_Game, new OutputMessage(true));
		}
		catch (GameException e)
		{
			sendError(response, PROTOCOL_Cli_Auto_Action, e.getId());
			logger.error(e.getMessage(), e.getCause());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(),e);
		}
		finally 
		{
			if (sem != null)
			{
				sem.release();
			}
		}
	}
	
	
}
