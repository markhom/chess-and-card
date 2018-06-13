package com.linyun.club.taurus.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

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
import com.linyun.common.entity.ClubMember;
import com.linyun.common.entity.ClubRoomLog;
import com.linyun.common.entity.GamePlayerLog;
import com.linyun.common.entity.GameRoundLog;
import com.linyun.common.entity.PrivateRoom;
import com.linyun.common.entity.TaurusLog;
import com.linyun.common.entity.TaurusRoundLog;
import com.linyun.common.entity.User;
import com.linyun.common.taurus.eum.BankerChooseBaseScore;
import com.linyun.common.taurus.eum.BankerMode;
import com.linyun.common.taurus.eum.BaseScoreType;
import com.linyun.common.taurus.eum.DissolutionStatus;
import com.linyun.common.taurus.eum.GameStatus;
import com.linyun.common.taurus.eum.PlayerInjection;
import com.linyun.common.taurus.eum.UpBankerScore;
import com.linyun.middle.common.taurus.bean.TaurusRoomConfig;
import com.linyun.middle.common.taurus.card.HandCard;
import com.linyun.middle.common.taurus.club.BaseClubServer;
import com.linyun.middle.common.taurus.club.ClubConfig;
import com.linyun.middle.common.taurus.club.TaurusClub;
import com.linyun.middle.common.taurus.club.TaurusClubMember;
import com.linyun.middle.common.taurus.engine.GameEngine;
import com.linyun.middle.common.taurus.engine.factory.EngineFactory;
import com.linyun.middle.common.taurus.manager.RoomManager;
import com.linyun.middle.common.taurus.manager.TableManager;
import com.linyun.middle.common.taurus.player.TaurusPlayer;
import com.linyun.middle.common.taurus.room.TaurusRoom;
import com.linyun.middle.common.taurus.table.TaurusSeat;
import com.linyun.middle.common.taurus.table.TaurusTable;
import com.linyun.middle.common.taurus.task.GameTimer;
import com.linyun.middle.common.taurus.utils.MessageUtils;

/**
 * 俱乐部游戏牌局接口类
 * */
public class ClubGameServer extends BaseClubServer
{
	private static Logger logger = LoggerFactory.getLogger(ClubGameServer.class);
	
	public static final Object lock_createClubRoom = new Object();
	public static final Object lock_enterTable = new Object();
	public static final Object lock_enterRoom = new Object();
	
	public static final int PLAYER_NUM = 6;
	/**---------------------------- 以下协议为牌局内协议---客户端发送给服务器的 -------------------------------------*/
	
	public static final short PROTOCOL_Cli_GetDetailRecord =6005;
	public static final short PROTOCOL_Cli_Room_Enter = 7001;// 加入房间 
	public static final short PROTOCOL_Cli_Room_Exit = 7002;//退出房间
	public static final short PROTOCOL_Cli_Table_SitDown = 7003;//进入桌子，坐下
	public static final short PROTOCOL_Cli_Table_Exit = 7004;//退出桌子，离开
	public static final short PROTOCOL_Cli_Room_Dissolution_Apply = 7005;//申请解散，某个玩家申请解散房间
	public static final short PROTOCOL_Cli_Room_Dissolution_Choice = 7006;//解散选择，其他玩家对申请解散房间做出选择
	public static final short PROTOCOL_Cli_Room_Result_Info = 7007;//游戏结果，房间解散的时候的总的游戏结果
	public static final short PROTOCOL_Cli_Start = 7008;//开始游戏，只有房主点击开始游戏
	public static final short PROTOCOL_Cli_Ready = 7009;//准备,在一局游戏完成后、下一局游戏开始之前可进行的操作
	public static final short PROTOCOL_Cli_Banker_Choose_BaseCoin = 7010;//庄家选择押注底分情况，在创建房间时，底分模式选择了"庄家选择"时触发
	public static final short PROTOCOL_Cli_Bet_Coin = 7011;//押注，玩家一局的下注分数
	public static final short PROTOCOL_Cli_Open_Cards = 7012;//亮牌，客户端选择确认亮牌
	public static final short PROTOCOL_Cli_Rob_Banker = 7013;//玩家抢庄，在庄家模式为自由抢庄和明牌抢庄的时候使用
	public static final short PROTOCOL_Cli_Owner_Dissolution = 7014;//房主解散房间，由客户端发起
	public static final short PROTOCOL_Cli_Auto_Action = 7015;//玩家进行托管操作
	public static final short PROTOCOL_Cli_Last_Round_Index = 7016;//玩家拉取上局局号
	public static final short PROTOCOL_Cli_FIXED_Banker_Close_Game = 7017;//固定庄家下庄协议
	public static final short PROTOCOL_Cli_Create_Club_Room = 7018; //创建俱乐部房间
	public static final short PROTOCOL_Cli_Get_Club_Amount = 7019 ;//拉取俱乐部成员额度详情
	public static final short Protocol_Cli_Club_Hall_Reconnect = 7091 ;//俱乐部大厅重连
	
	/** ----------------------------新版H5新增协议-------------------------*/
	public static final short PROTOCOL_Cli_Get_BuyScore_Info = 7020;//获取买入积分牌数据
	public static final short PROTOCOL_Cli_BuyScore_SitDown = 7021;//买入积分牌，并坐下
	public static final short PROTOCOL_Ser_Others_SitDown = 7022;//其他玩家进入桌子，坐下
	public static final short PROTOCOL_Ser_Game_Log_Send = 7023;//每局游戏结束推送战绩给前端
	public static final short PROTOCOL_Ser_ReserveSeat = 7024;//保座离桌推送
	public static final short PROTOCOL_Cli_Gaming_BuyScore =7025;//玩家游戏中买入积分
	public static final short PROTOCOL_Cli_Cancel_BuyScore =7027;//取消买入积分
	
	/** ---------------------------- 以下协议为牌局内协议---服务器发送给客户端------------------------*/
	public static final short PROTOCOL_Ser_Table_SitDown = 8001; //进入桌子，坐下
	public static final short PROTOCOL_Ser_Table_Exit = 8002;//退出桌子，离开 (保座离桌特殊处理等同 退出桌子)
	public static final short PROTOCOL_Ser_Room_Dissolution_Apply = 8003;//申请解散，某个玩家申请解散房间
	public static final short PROTOCOL_Ser_Room_Dissolution_Choice = 8004;//解散选择，其他玩家对申请解散房间做出选择
	public static final short PROTOCOL_Ser_Room_Dissolution_Result = 8005;// 解散结果，对一个解散申请最后的处理结果
	public static final short PROTOCOL_Ser_Game_Start = 8006;//游戏开始，房主开始游戏
	public static final short PROTOCOL_Ser_Game_Status_Changed = 8007;//游戏状态，当游戏状态发生变化的时候发送客户端
	public static final short PROTOCOL_Ser_Ready = 8008;//准备,在一局游戏完成后、下一局游戏开始之前可进行的操作
	public static final short PROTOCOL_Ser_Rob_Banker = 8009;//玩家抢庄，在庄家模式为自由抢庄和明牌抢庄的时候使用
	public static final short PROTOCOL_Ser_Banker_Info = 8010;//庄家确认，在庄家确定之后通知客户端庄家信息
	public static final short PROTOCOL_Ser_Banker_Choose_BaseCoin = 8011;//庄家选择押注底分情况，在创建房间时，底分模式选择了"庄家选择"时触发
	public static final short PROTOCOL_Ser_Bet_Coin = 8012;//押注，玩家一局的下注分数
	public static final short PROTOCOL_Ser_Deal_Cards = 8013;//发牌，将最多6个位置上的玩家的牌信息分别发送给玩家
	public static final short PROTOCOL_Ser_Open_Cards = 8014;//亮牌，客户端选择确认亮牌
	public static final short PROTOCOL_Ser_Compare_Cards_Result = 8015;//比牌，进行玩家的牌型计算，将比牌结果<也就是游戏结果>发送给客户端,一局的比牌结果
	public static final short PROTOCOL_Ser_Owner_Dissolution_Notice = 8016;//房主解散房间，通知房间内的其他人
	public static final short PROTOCOL_Ser_Timeout_Dissolution_Notice = 8017;//房间超时被解散，通知房间内的所有人
	public static final short PROTOCOL_Ser_Deal_Cards_Watch = 8018;//观战玩家收到发牌协议
	public static final short PROTOCOL_Ser_Rob_Banker_Deal_Cards = 8021;//明牌抢庄发牌，将最多6个位置上的玩家的牌信息分别发送给玩家
	public static final short PROTOCOL_Ser_ROB_Banker_Deal_Cards_Watch = 8022;//观战玩家收到明牌抢庄发牌协议
	public static final short PROTOCOL_Ser_FIXED_Banker_Close_Game = 8023;//固定庄家下庄通知客户端
	public static final short PROTOCOL_Ser_FIXED_Banker_No_Enough_Coin_Close_Game = 8024;//固定庄家庄家分数不足通下庄通知客户端
	/**----------------------------------------end------------------------------*/
	/************************************************************************************************************/
	public static final short PROTOCOL_Cli_reconnect = 9001;//游戏内的重连协议
	public static final short PROTOCOL_Cli_reconnect_out = 9002;//游戏外重连

	public static final short PROTOCOL_Ser_Disconnect = 9011;//玩家掉线通知客户端
	public static final short PROTOCOL_Ser_reconnect = 9012;//玩家掉线重连回来通知客户端
	public static final short PROTOCOL_Ser_Outconnect = 9013;//通知玩家在其他设备上登录/**玩家被冻结协议*/
	public static final short PROTOCOL_Ser_Frozen = 9015 ;//玩家被冻结协议
    /************************************************************************************************************/
	public ClubGameServer()
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
				logger.info("玩家" + userId + "俱乐部游戏<掉线>");
				if (userId == null)
				{
					return;
				}
				sem = getSemp(userId);
				sem.acquire();
				
				//清理俱乐部相关
				TaurusClub club = getUserClub(userId);
				if (club != null)
				{
					club.removeUserSession(userId);
					club.subOneOnlineCount();
					TaurusClubMember member = club.getMember(Integer.parseInt(userId));
					if(member != null)
					{
						member.disConnect();
					}
					
					// ---------------------------------房间-------------------------
					TaurusRoom room = getUserRoom(userId);
					if (room != null)
					{//进入了俱乐部，也进入了房间
						// 固定清除session相关
						room.removeSession(userId);
						
						TaurusTable table = getUserTable(userId);
						if (table == null)
						{// 未加入桌子的玩家
							logger.info("玩家" + userId + "加入俱乐部房间，未加入桌子，在房间内掉线，" + room.getRoomId());
							unbindUserRoom(userId);
							unbindUserClub(userId);
						}
						else
						{//加入桌子
							logger.info("玩家" + userId + "加入俱乐部房间，加入桌子，在房间内掉线,"+room.getRoomId());
							TaurusSeat seat = table.getUserSeat(userId);
							seat.setAutoAction(true);
							seat.setOnline(false);

							OutputMessage om = new OutputMessage(true);
							om.putString(userId);
							room.sendMessage(PROTOCOL_Ser_Disconnect, om);
						}
					}
					else
					{
						logger.info("玩家" + userId + "未加入俱乐部房间，在俱乐部房间列表页面掉线，clubId is" + club.getClubId());
						unbindUserClub(userId);
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
	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * room begin * * *  * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  * * * * * * * * * * * * * * * * * * * *
	 * */
	
	
	/** 游戏内断线重连 */
	public void reconnect(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession newSession = request.getSession();
		
		Semaphore sem = null; 
		try
		{
			String userId = msg.getUTF();
			int iUserId = Integer.parseInt(userId);
			sem = getSemp(userId);
			sem.acquire();
			
			logger.info("user " + userId + " reconnect");
			TaurusClub taurusClub = getUserClub(userId);
			if(taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST,"玩家" + userId + "断线重连时(俱乐部)，找不到俱乐部");
			}
			TaurusRoom room = getUserRoom(userId);
			if (room == null)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST, "玩家" + userId + "断线重连时(俱乐部)，找不到房间");
			}
			TaurusTable table = getUserTable(userId);
			if (table == null)
			{
				throw new GameException(GameException.TABLE_NOT_EXIST, "玩家" + userId + "断线重连时(俱乐部)，找不到桌子");
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
						taurusClub.removeUserSession(userId);
					}
				}
				
				taurusClub.addUserSession(Integer.valueOf(userId), newSession);//暂时没有用到，为俱乐部内广播消息预留
				TaurusClubMember member = taurusClub.getMember(iUserId);
				if(member != null)
				{
					member.online();
				}
				taurusClub.addOneOnlineCount();
				
				room.addSession(userId, newSession);
				addUser(userId, newSession);
				TaurusSeat seat = null;
				if((seat=table.getUserSeat(userId))!=null)
				{
					seat.setAutoAction(false);
					seat.setOnline(true);
				}
				

				OutputMessage om = MessageUtils.getReconnectMessage(room, table);
				om.putInt(room.getConfig().getJoinGameScoreLimit());
				om.putInt(member.getCurrentScore());
				om.putInt(taurusClub.getDiamondPercent());
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
			int iUserId = Integer.parseInt(userId);
			sem = getSemp(userId);
			sem.acquire();
			logger.info("用户" + userId + " 游戏外重连");
			TaurusClub taurusClub = getUserClub(userId);
			if(taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST,"玩家" + userId + "游戏外断线重连时(俱乐部)，找不到俱乐部");
			}
			TaurusRoom room = getUserRoom(userId);
			if (room == null)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST, "玩家" + userId + "游戏外断线重连时(俱乐部)，找不到房间");
			}
			TaurusTable table = getUserTable(userId);
			if (table == null)
			{
				throw new GameException(GameException.TABLE_NOT_EXIST, "玩家" + userId + "游戏外断线重连时(俱乐部)，找不到房间");
			}

			synchronized (room)
			{
				/** 处理 session， 先清理后加进来 */
				GameSession oldSession = getUserSession(userId);
				if (oldSession != null)
				{
					synchronized (oldSession)
					{
						oldSession.sendMessage(PROTOCOL_Ser_Outconnect, new OutputMessage(true));
						room.removeSession(userId);
						// baseServer
						removeUser(oldSession);
						//club
						taurusClub.removeUserSession(userId);
					}
				}
				
				//俱乐部处理
				taurusClub.addUserSession(iUserId, newSession);
				TaurusClubMember member = taurusClub.getMember(iUserId);
				if(member != null)
				{
					member.online();
				}
				taurusClub.addOneOnlineCount();
				
				room.addSession(userId, newSession);
				addUser(userId, newSession);
				TaurusSeat seat = null;
				if((seat=table.getUserSeat(userId))!=null)
				{
					seat.setAutoAction(false);
					seat.setOnline(true);
				}

				GamePlayerLog playerLog = gamePlayerLogAction().getPlayerLogById(taurusClub.getClubId(), room.getRoomId(), iUserId);
				/** 游戏外重连 */
				OutputMessage om = MessageUtils.getOutReconnectMessage(room, table);
				om.putInt(room.getConfig().getJoinGameScoreLimit());
				om.putInt(member.getCurrentScore());
				om.putInt(taurusClub.getDiamondPercent());
				om.putInt(playerLog == null ? 0 : playerLog.getBuyScore());
				om.putInt(room.getConfig().getGameTime());
				
				//判断玩家游戏开始后能不能坐下
				boolean noEnter = room.getConfig().getAdvancedOptions().isNoEnter();
				List<Integer> allPlayerIds = gamePlayerLogAction().getAllPlayerIds(taurusClub.getClubId(), room.getRoomId());
				
				om.putBoolean(room.isGameStart() && noEnter && !allPlayerIds.contains(iUserId) ? false : true);
				newSession.sendMessage(PROTOCOL_Cli_reconnect_out, om);
				
				OutputMessage other_om = new OutputMessage(true);
				other_om.putString(userId);
				room.sendMessage(PROTOCOL_Ser_reconnect, other_om, newSession);
				logger.info("用户" + userId + "游戏外重连" + room.getRoomId() + "成功");
			}
		}
		catch (GameException e)
		{
			sendError(response, PROTOCOL_Cli_reconnect_out, e.getId());
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
     
	//游戏内俱乐部大厅重连
	public void ClubHallReconnect(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession newSession = request.getSession();
		Semaphore sem = null ;
		try
		{
			String userId = msg.getUTF();
			int iUserId = Integer.parseInt(userId);
			int clubId = msg.getInt();
			sem = getSemp(userId);
			sem.acquire();
			
			GameSession oldSession = getUserSession(userId);
			if(oldSession != null)
			{
				removeUser(oldSession);
			}
			addUser(userId, newSession);
			//在俱乐部大厅，没有进入俱乐部里面
			if(clubId == 0)
			{
				logger.info("玩家：userId is "+userId+"在俱乐部大厅重连success！");
			}
			//玩家在俱乐部房间列表中掉线
			else
			{
				TaurusClub taurusClub = getClub(clubId);
				if (taurusClub == null)
				{
					throw new GameException(GameException.CLUB_IS_NOT_EXIST,"玩家：userId="+userId+"断线重连回来时，俱乐部clubId="+clubId+"不存在！");
				}
				
				taurusClub.addUserSession(iUserId, newSession);
				taurusClub.addOneOnlineCount();
				bindUserClub(userId, taurusClub);
				TaurusClubMember member = taurusClub.getMember(Integer.parseInt(userId));
				if(member != null)
				{
					member.online();
				}
				logger.info("玩家：userId is "+userId+"在俱乐部clubId is "+clubId+"的房间列表页面重连success！");
			}
			OutputMessage om = new OutputMessage(true);
			newSession.sendMessage(Protocol_Cli_Club_Hall_Reconnect, om);
		}
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Club_Hall_Reconnect, e.getId());
			logger.error(e.getMessage(), e);
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
	
	
	//拉取俱乐部成员额度详情
	public void getClubMemberAmountDetail(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			int iUserId = Integer.parseInt(userId);
			int clubId = msg.getInt();
			logger.info("in getClubMemberAmountDetail, userId is"+userId+",clubId is "+clubId); 
			//校验
			getExistUser(userId);
			TaurusClub taurusClub = getClub(clubId);
			if(taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST, "俱乐部玩家在拉取额度时，俱乐部不存在:clubId is"+clubId);
			}
			if(!taurusClub.ContainsUser(iUserId))
			{
				throw new GameException(GameException.CLUB_USER_IS_NOT_MEMBER, "俱乐部玩家在拉取额度时，不是俱乐部成员:userId is"+userId);
			}
			
			ClubMember clubMember = clubMemberAction().getClubMemberByClubId(clubId, iUserId);
			
			int currentScore = clubMember.getCurrentScore();
			int scoreLimit = clubMember.getScoreLimit();
			
			OutputMessage om = new OutputMessage(true);
			om.putInt(currentScore);
			om.putInt(scoreLimit);
			session.sendMessage(PROTOCOL_Cli_Get_Club_Amount, om);
			logger.info(" getClubMemberAmountDetail success, userId is"+userId+",clubId is "+clubId); 
		} 
		catch (GameException e)
		{
			sendError(response, PROTOCOL_Cli_Get_Club_Amount, e.getId());
			logger.error(e.getMessage(), e);
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
	
	//创建俱乐部房间
	public void createClubRoom(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{    
			 String userId = msg.getUTF(); //创建房间的玩家的Id
			 int iUserId = Integer.parseInt(userId);
			 int gameType = msg.getInt();//游戏类型    eg:牛牛上庄,轮流上庄
			 int coin = msg.getInt();  //底分 改成 小盲/大盲   最小带入底分为大盲的500倍
			 int gameTime = msg.getInt(); //游戏时长
			 int ruleType = msg.getInt(); //翻倍规则    默认选2   牛牛*4
			 int allFace = msg.getInt() ; //五花牛  0没选  1 已选  5倍
			 int bomb = msg.getInt();//炸弹牛   6倍
			 int allSmall = msg.getInt();//五小牛   8倍
			 int playerInjection = msg.getInt();//闲家推注  0--没选   1--选  默认是0
			 int noEnter = msg.getInt();//游戏开始后禁止加入   0--没选   1--选  默认是0
			 int noShuffle = msg.getInt();//禁止搓牌     0--没选   1--选  默认是0
			 //固定庄家的上庄分数和明牌抢庄的最大抢庄
			 int maxRobRanker = msg.getInt();//最大抢庄：（明牌抢庄模式特有）  1-1倍，2-2倍，3-3倍，4-4倍（默认值）
			 int upBankerScore = msg.getInt();
			 int clubId = msg.getInt(); // 创建房间时所在的俱乐部的id
			 
			 logger.info("in createClubRoom, userId is"+userId+",clubId is "+clubId); 
			 sem = getSemp(userId);
			 sem.acquire();
			 /**权限校验*/
			 getExistUser(userId); //用户是否存在
			 TaurusClub taurusClub = getClub(clubId);
			 if(taurusClub == null)
			 {
				 throw new GameException(GameException.CLUB_IS_NOT_EXIST, "俱乐部玩家在创建房间时，俱乐部不存在:clubId is"+clubId);
			 }
			 if(!taurusClub.ContainsUser(iUserId))
			 {
				 throw new GameException(GameException.CLUB_USER_IS_NOT_MEMBER, "创建俱乐部房间时，不是俱乐部成员禁止创建:userId is"+userId);
			 }
			 int expandRate = taurusClub.getExpandRate();
			 int scoreRate = taurusClub.getScoreRate();
			 if(!((coin % expandRate == 0 && coin>=1*expandRate && coin<=10*expandRate)||(coin % 5==0 && coin<=100*expandRate && coin >=10*expandRate)))
			 {
				 throw new GameException(GameException.CLUB_CREATEROOM_BASESCORE_ERROR,"创建俱乐部房间时传入的底分有误！");
			 }
			 int scoreLimit = coin * 2 * scoreRate;
			 int roomNum = 0 ;
			 synchronized (lock_createClubRoom)
			 {   
				
				 //校验俱乐部开房数量是否达到上线
				 checkClubRoomNum(taurusClub,iUserId); 
				 
				//随机生成一个房间号，并判断该房间是否存在，如果存在则生成另外一个，不存在的话可以使用
				 roomNum = new Random().nextInt(900000) + 100000;
				 while((roomAction().getPrivateRoom(roomNum)) != null || 
						 clubRoomLogAction().getOnlyRoomNumInfourDays().size() != 0)
				 {
					   roomNum = new Random().nextInt(900000) + 100000;
				 }
				 
				 //将创建房间的相关信息写入数据库
				  PrivateRoom pr = new PrivateRoom();
				  pr.setRoomNum(roomNum); //获取生成的房间号
				  pr.setUpBankerMode((byte)gameType);
				  pr.setGameTime(gameTime);
				  if(gameType != BankerMode.BANKER_MODE_ALL_COMPARE.value) // 不是通比牛牛模式时，底分选项1/2  2/4  4/8  
				  {
					  pr.setBaseScore(coin);
					  pr.setPlayerInjection(conversionPlayerInjection(playerInjection));
				  }
				  else
				  {
					  pr.setAllCompareBaseScore(coin); //通比牛牛  底分  1  2  4 同时通比牛牛没有闲家推注
				  }
				  pr.setJoinGameScoreLimit(scoreLimit);
				  pr.setTimesMode((byte)ruleType);
				  pr.setAllFace((byte)allFace);
				  pr.setBomb((byte)bomb);
				  pr.setAllSmall((byte)allSmall);
				  pr.setNoEnter((byte)noEnter);
				  pr.setNoShuffle((byte)noShuffle);
				  pr.setRoomOwnerId(Integer.valueOf(userId));
				  pr.setSeatNum(6);//座位数
				  
				  //明牌抢庄模式--最大抢庄
				  if(gameType == BankerMode.BANKER_MODE_BRIGHT_ROB.value)
				  {
					  pr.setMostRobBanker((byte)maxRobRanker);
				  }
				  //固定庄家 --上庄分数
				  if(gameType == BankerMode.BANKER_MODE_FIXED.value)
				  {
					  pr.setUpBankerScore(UpBankerScore.ValueOf(upBankerScore).value);
				  }
				  pr.setClubId(clubId);
				  roomAction().createPrivateRoom(pr);
				  
				  //初始化房间
				  TaurusRoom game_Room = null ;
				  game_Room = RoomManager.getRoom();
				  if(game_Room == null)
				  {
					  game_Room = new TaurusRoom(pr);
					  logger.info("初始化后房间号roomId is "+game_Room.getRoomId()+",roomOwnerId is "+game_Room.getRoomOwnerId());
				  }
				  else
				  {
					  game_Room.Init(pr);
					  logger.info("资源池初始化后房间号roomId is "+game_Room.getRoomId()+",roomOwnerId is "+game_Room.getRoomOwnerId());
				  }
				  taurusClub.addRoom(game_Room);
				  //BaseServer
				  addRoom(String.valueOf(roomNum), game_Room);
				  clubRoomAction().bindRoomClub(roomNum, clubId);
			 }
			 
			  OutputMessage om = new OutputMessage(true);
			  om.putString(String.valueOf(roomNum));
			  session.sendMessage(PROTOCOL_Cli_Create_Club_Room, om);
			  logger.info("createClubRoom success, userId is"+userId+",clubId is "+clubId+",roomNum is "+roomNum); 
		}
		catch (GameException e)
		{
			sendError(response, PROTOCOL_Cli_Create_Club_Room, e.getId());
			logger.error(e.getMessage(), e);
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
	
	//俱乐部开房数量是否达到上线
	public void checkClubRoomNum(TaurusClub taurusClub,int userId)
	{
		//公共俱乐部开房数暂时没有限制，私人俱乐部有开房限制
		if(taurusClub.getClubType() == 0)
		{   
			int clubId = taurusClub.getClubId();
			int priClubMaxTableNum = ClubConfig.INSTANCE.getConfig().getPriClubMaxTableNum();//允许的最大房间数
			int clubRoomNum = taurusClub.getAllRoomCount();//俱乐部已开房间数
			if(clubRoomNum >= priClubMaxTableNum)
			{
				throw new GameException(GameException.CLUB_ROOMS_REACH_LIMIT,
						 "玩家：userId is"+userId+"在俱乐部:clubId is "+clubId+"创建房间时，俱乐部房间数达到上限");
			}
		}
	}

	//转换前端传过来的闲家推注的值
	 public byte conversionPlayerInjection(int value)
	 {
		  switch (value)
		  {
		  case 0:
			 return PlayerInjection.PLAYER_INJECTION_NONE.value ;
		  case 1:
		     return PlayerInjection.PLAYER_INJECTION_5.value ;
		  case 2:
			 return PlayerInjection.PLAYER_INJECTION_10.value ;
		  case 3:
			 return PlayerInjection.PLAYER_INJECTION_20.value ;
		 default:
			 return PlayerInjection.PLAYER_INJECTION_NONE.value ;
		 }
	 } 
	 
	 //获取买入记分牌数据
	 public void getBuyScoreInfo(SocketRequest request, SocketResponse response)
	 {
		 InputMessage im = request.getInputMessage();
		 String userId = im.getUTF();
		 int clubId = im.getInt();
		 GameSession session = request.getSession();
		 try
		 {
			 int iUserId = Integer.parseInt(userId);
			 /**权限校验*/
			 getExistUser(userId); //用户是否存在
			 TaurusClub taurusClub = getClub(clubId);
			 if(taurusClub == null)
			 {
				 throw new GameException(GameException.CLUB_IS_NOT_EXIST, "俱乐部玩家在拉取积分牌数据时，俱乐部不存在:clubId is"+clubId);
			 }
			 if(!taurusClub.ContainsUser(iUserId))
			 {
				 throw new GameException(GameException.CLUB_USER_IS_NOT_MEMBER, "俱乐部玩家在拉取积分牌数据时，不是俱乐部成员:userId is"+userId);
			 }
			 TaurusRoom userRoom = getUserRoom(userId);
			 
			 TaurusClubMember member = taurusClub.getMember(iUserId);
			 GamePlayerLog playerLog = gamePlayerLogAction().getPlayerLogById(clubId, userRoom.getRoomId(), iUserId);
			 OutputMessage om = new OutputMessage(true);
			 om.putInt(member.getCurrentScore());
			 om.putInt(playerLog == null ? 0 : playerLog.getBuyScore());//历史买入积分总和
			 session.sendMessage(PROTOCOL_Cli_Get_BuyScore_Info, om);
			 
		 }catch(GameException e)
		 {
			 sendError(response, PROTOCOL_Cli_Get_BuyScore_Info, e.getId());
			 logger.error(e.getMessage(), e);
		 }catch(Exception e)
		 {
			 logger.error(e.getMessage(),e);
		 }
			 
	 }
	 
	 //俱乐部成员买入积分并坐下
	 public void buyScoreAndSitDown(SocketRequest request, SocketResponse response)
	 {
		 InputMessage im = request.getInputMessage();
		 String userId = im.getUTF();
		 int clubId = im.getInt();
		 String tableId = im.getUTF();
		 int score = im.getInt();
		 byte seatId = im.getByte();
		 
		 Semaphore sem = null;

		 try
		 {
			 int iUserId = Integer.parseInt(userId);
			 int iTableId= Integer.parseInt(tableId);
			 sem = getSemp(userId);
			 sem.acquire();
			 GameSession session = request.getSession();
				
			 /**权限校验*/
			 getExistUser(userId); //用户是否存在
			 TaurusClub taurusClub = getClub(clubId);
			 if(taurusClub == null)
			 {
				 throw new GameException(GameException.CLUB_IS_NOT_EXIST, "俱乐部玩家在买入积分牌并坐下时，俱乐部不存在:clubId is"+clubId);
			 }
			 if(!taurusClub.ContainsUser(iUserId))
			 { 
				 throw new GameException(GameException.CLUB_USER_IS_NOT_MEMBER, "俱乐部玩家在买入积分牌并坐下时，不是俱乐部成员:userId is"+userId);
			 }
			 TaurusRoom game_room = getUserRoom(userId);
			 if (game_room == null)
		     {
				 throw new GameException(GameException.ROOM_NOT_EXIST, "俱乐部玩家在买入积分牌并坐下时，房间不存在，userId="+userId + ",roomId="+iTableId);
			 }
			
			 //加入桌子游戏的最低带入底分
			 int joinGameScoreLimit = game_room.getConfig().getJoinGameScoreLimit();
			 TaurusClubMember member = taurusClub.getMember(iUserId);
			 //该俱乐部玩家的买入积分限额
			 int currentScore = member.getCurrentScore();
			 
			 if(score < joinGameScoreLimit)
			 {
				 throw new GameException(GameException.CLUB_BUYSCORE_BELOW_LIMIT,"俱乐部成员买入积分低于房间最低底分,userId="+userId);
			 }
			 if(score > currentScore)
			 {
				 throw new GameException(GameException.CLUB_BUYSCORE_ABOVE_CURRENTSCORE ,"俱乐部成员买入积分高于其现有总积分,userId="+userId);
			 }
				
			  User user = userAction().getExistUser(userId);
				//---------------------------桌子-------------------------------------
				TaurusTable user_table = getUserTable(userId);
				if ((user_table!=null) && (!user_table.getTableId().equals(tableId)))
				{
					throw new GameException(GameException.ROOM_UNFINISHED_GAME, "userId:" + userId + "在桌子" + user_table.getTableId() + "有未完成的牌局");
				}
				//钻石采用加一法取整
				int temp = taurusClub.getDiamondPercent()*score;
				int diamond = temp%100 == 0 ? temp/100 : (temp/100)+1;
				
				User creator = userAction().getExistUser(String.valueOf(taurusClub.getCreatorId()));
				if(creator.getDiamond()< diamond)
				{
					throw new GameException(GameException.CLUB_CREATOR_DIAMOND_IS_NOT_ENOUGH, 
							"玩家：userId is"+userId+"在买入底分时，该俱乐部群主"+creator.getUserId()+"钻石不足");
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
						List<Integer> allPlayerLog = gamePlayerLogAction().getAllPlayerIds(clubId, iTableId);
						
						if (game_room.isGameStart() && (game_room.getConfig().getAdvancedOptions().isNoEnter()) && !allPlayerLog.contains(iUserId))
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
						logger.info("桌子" + iTableId + "不存在，创建桌子");
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
					TaurusPlayer player = new TaurusPlayer(tableId, userId,score);
				    boolean addPlayer = game_table.addPlayer(player, user, seatId, game_room);
				    
				    if(!addPlayer)
				    {
				    	throw new GameException(GameException.GAME_ERROR_SEAT_CAN_NOT_SITDOWN, "玩家在加入桌子时，对应位置坐不下，userId="+userId);
				    }else
				    {
				    	logger.info("========= 玩家"+userId+",进入桌子"+tableId+"坐下，座位号为"+seatId);
				    }
				}
			    
				//玩家带入底分，消耗现有积分
				clubMemberAction().updateClubMemberCurrentScore(clubId, iUserId, currentScore-score);
				//刷新缓存
				member.setCurrentScore(currentScore-score);
				//扣除群主的钻石
				
				userAction().deductDiamond(userId, diamond, taurusClub.getClub(), iTableId);
				
				if(game_room.isGameStart())
				{
					GamePlayerLog playerLogById = gamePlayerLogAction().getPlayerLogById(clubId, iTableId, iUserId);
					if(playerLogById == null)
					{
						GamePlayerLog playerLog = new GamePlayerLog();
						playerLog.setClubId(clubId);
						playerLog.setClubName(taurusClub.getClubName());
						playerLog.setRoomNum(iTableId);
						playerLog.setPlayerId(iUserId);
						playerLog.setBuyScore(score);
						playerLog.setNickName(user.getNickName());
						playerLog.setHeadImgUrl(user.getHeadImgUrl());
						gamePlayerLogAction().addGamePlayerLog(playerLog);
					}else
					{
						gamePlayerLogAction().updatePlayerBuyScoreHistory(score, clubId, iTableId, iUserId);
					}
				}
				
				boolean isJoinGame = game_room.getGameStatus()==GameStatus.GAME_STATUS_INIT || game_room.getGameStatus()==GameStatus.GAME_STATUS_TABLE_READY || game_room.getGameStatus() == GameStatus.GAME_STATUS_PAUSE ? true : false;
				synchronized (game_table)
				{   
					bindUserTable(userId, game_table);
					OutputMessage self_om = MessageUtils.getSelfEnterTableMessage(game_room, game_table, seatId);
					self_om.putBoolean(isJoinGame);
					self_om.putInt(score);
					session.sendMessage(PROTOCOL_Cli_BuyScore_SitDown, self_om);
					
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
					other_om.putBoolean(isJoinGame);
					other_om.putInt(score);
					game_room.sendMessage(PROTOCOL_Ser_Others_SitDown, other_om, session);
					//更新数据库中的房间中的已经坐下的玩家人数
					roomAction().addOnePlayer(iTableId);
				}
			 
		 }catch(GameException e)
		 {
			 sendError(response,PROTOCOL_Cli_BuyScore_SitDown,e.getId());
			 logger.error(e.getMessage(),e);
			 
		 }catch(Exception e)
		 {
			 logger.error(e.getMessage(),e);
		 }finally
		 {
			 if (sem != null)
				{
					sem.release();
				}
		 }
	 }
	 /** 玩家游戏过程中积分不足重新买入积分*/
	public void playerGamingBuyScore(SocketRequest request, SocketResponse response)
	{
		InputMessage im = request.getInputMessage();
		 Semaphore sem = null;
		try
		{
			String uid = im.getUTF();
			int clubId = im.getInt();
			String roomNum = im.getUTF();
			int score = im.getInt();
			
			int userId = Integer.parseInt(uid);
			int iTableId= Integer.parseInt(roomNum);
			
			GameSession session = request.getSession();
			
			 /**权限校验*/
			 getExistUser(uid); //用户是否存在
			 
			 sem = getSemp(uid);
			 sem.acquire();
			 TaurusClub taurusClub = getClub(clubId);
			 if(taurusClub == null)
			 {
				 throw new GameException(GameException.CLUB_IS_NOT_EXIST, "俱乐部玩家在游戏中买入积分时，俱乐部不存在:clubId is"+clubId);
			 }
			 if(!taurusClub.ContainsUser(userId))
			 { 
				 throw new GameException(GameException.CLUB_USER_IS_NOT_MEMBER, "俱乐部玩家在游戏中买入积分时，不是俱乐部成员:userId is"+userId);
			 }
			 TaurusRoom game_room = getUserRoom(uid);
			 if (game_room == null)
		     {
				 throw new GameException(GameException.ROOM_NOT_EXIST, "俱乐部玩家在游戏中买入积分时，房间不存在，userId="+userId + ",roomId="+iTableId);
			 }
			 TaurusTable game_table = getUserTable(uid);
			 if(game_table == null)
			 {
				 throw new GameException(GameException.TABLE_NOT_EXIST,"俱乐部玩家在游戏中买入积分时，桌子不存在，userId="+userId+",tableId="+iTableId);
			 }
			 //加入桌子游戏的最低带入底分
			 int joinGameScoreLimit = game_room.getConfig().getJoinGameScoreLimit();
			 TaurusClubMember member = taurusClub.getMember(userId);
			 //该俱乐部玩家的买入积分限额
			 int currentScore = member.getCurrentScore();
			 
			 if(score < joinGameScoreLimit)
			 {
				 throw new GameException(GameException.CLUB_BUYSCORE_BELOW_LIMIT,"俱乐部成员买入积分低于房间最低底分,userId="+userId);
			 }
			 if(score > currentScore)
			 {
				 throw new GameException(GameException.CLUB_BUYSCORE_ABOVE_CURRENTSCORE ,"俱乐部成员买入积分高于其现有总积分,userId="+userId);
			 }
				
			//钻石采用加一法取整
			int temp = taurusClub.getDiamondPercent()*score;
			int diamond = temp%100 == 0 ? temp/100 : (temp/100)+1;
			
			User creator = userAction().getExistUser(String.valueOf(taurusClub.getCreatorId()));
			if(creator.getDiamond()< diamond)
			{
				throw new GameException(GameException.CLUB_CREATOR_DIAMOND_IS_NOT_ENOUGH, 
							"玩家：userId is"+userId+"在买入底分时，该俱乐部群主"+creator.getUserId()+"钻石不足");
			}
			//玩家带入底分，消耗现有积分
			clubMemberAction().updateClubMemberCurrentScore(clubId, userId, currentScore-score);
			//刷新缓存
			member.setCurrentScore(currentScore-score);
			//扣除群主的钻石
			
			userAction().deductDiamond(uid, diamond, taurusClub.getClub(), iTableId);
			//更新玩家在俱乐部房间的历史买入底分
			gamePlayerLogAction().updatePlayerBuyScoreHistory(score, clubId, iTableId, userId);
			//刷新玩家现有积分
		    TaurusSeat seat = game_table.getUserSeat(uid);
		    TaurusPlayer player = seat.getPlayer();
		    player.setScoreTotal(player.getScoreTotal()+score);
		    //清理保座状态
		    seat.setKeepSeatStage(false);
		    seat.setKeepSeatTimer(0);
		    
		    boolean isJoinGame =  game_room.getGameStatus()==GameStatus.GAME_STATUS_PAUSE || game_room.getGameStatus()==GameStatus.GAME_STATUS_TABLE_READY ? true : false;
		    seat.setJoinGame(isJoinGame);
		    OutputMessage om = new OutputMessage(true);
		    om.putString(uid);
		    om.putInt(player.getScoreTotal());
		    om.putBoolean(isJoinGame);
		    game_room.sendMessage(PROTOCOL_Cli_Gaming_BuyScore, om);
			
		}catch(GameException e)
		{
			sendError(response,PROTOCOL_Cli_Gaming_BuyScore,e.getId());
			logger.error(e.getMessage(),e);
		}catch(Exception e)
		{
			logger.error(e.getMessage(),e);
		}
		finally
		{
			if(sem !=null)
			{
				sem.release();
			}
		}
		
	}
	
	/**玩家在游戏中积分不足但是取消买入积分*/
	public void cancelBuyScore(SocketRequest request, SocketResponse response)
	{
		InputMessage im = request.getInputMessage();
		String userId = im.getUTF();
		String roomNum = im.getUTF(); 
		
		try
		{
			 GameSession session = request.getSession();
			 getExistUser(userId); //验证权限
			 TaurusRoom game_room = getUserRoom(userId);
			 if (game_room == null)
		     {
				 throw new GameException(GameException.ROOM_NOT_EXIST, "俱乐部玩家在取消买入积分时，房间不存在，userId="+userId + ",roomId="+roomNum);
			 }
			 TaurusTable game_table = getTable(roomNum);
			 if(game_table == null)
			 {
				 throw new GameException(GameException.TABLE_NOT_EXIST,"俱乐部玩家在取消买入积分时，桌子不存在，userId="+userId + ",roomId="+roomNum);
			 }
			 int userLocation = game_table.getUserLocation(userId);
			 
			 TaurusClub taurusClub = getClub(game_room.getClubId());
			 if(taurusClub == null)
			 {
				 throw new GameException(GameException.CLUB_IS_NOT_EXIST, "俱乐部玩家在买入积分牌并坐下时，俱乐部不存在:clubId is"+game_room.getClubId());
			 }
			 if(!taurusClub.ContainsUser(Integer.valueOf(userId)))
			 { 
				 throw new GameException(GameException.CLUB_USER_IS_NOT_MEMBER, "俱乐部玩家在买入积分牌并坐下时，不是俱乐部成员:userId is"+userId);
			 }
			 //取消买入后踢玩家下桌，然后积分返还俱乐部成员积分池中
			 TaurusClubMember member = taurusClub.getMember(Integer.valueOf(userId));
			 TaurusSeat seat = game_table.getSeat(userLocation);
			 int scoreTotal = seat.getPlayer().getScoreTotal();
			 if(scoreTotal>0)
			 {
				 clubMemberAction().updateClubMemberCurrentScore(game_room.getClubId(),Integer.valueOf(userId), member.getCurrentScore()+scoreTotal);
				 member.setCurrentScore(member.getCurrentScore()+scoreTotal);
			 }
			 session.sendMessage(PROTOCOL_Cli_Cancel_BuyScore, new OutputMessage(true));
			 OutputMessage om = new OutputMessage(true);
			 om.putString(userId);
			 om.putByte((byte)userLocation);
			 game_room.sendMessage(PROTOCOL_Ser_Table_Exit, om);
			 
			 exitExistTable(userId, game_table);
			
		}catch(GameException e)
		{
			sendError(response,PROTOCOL_Cli_Cancel_BuyScore,e.getId());
			logger.error(e.getMessage(),e);
			
		}catch(Exception e)
		{
			logger.error(e.getMessage(),e);
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
			TaurusClub taurusClub = getUserClub(userId);
			if(taurusClub == null)
			{
				 throw new GameException(GameException.CLUB_IS_NOT_EXIST, "俱乐部玩家在进入房间时，俱乐部不存在");
			}
			TaurusRoom tempRoom = getUserRoom(userId);
			if ((tempRoom != null) && (tempRoom.getRoomId() != iRoomNum))
			{
				other_roomId = getUserRoom(userId).getRoomId();
				throw new GameException(GameException.ROOM_UNFINISHED_GAME,
						"userId:" + userId + "在房间" + other_roomId + "有未完成的牌局");
			}
			
			PrivateRoom db_room = roomAction().getPrivateRoom(iRoomNum);
			if (db_room == null || db_room.getClubId() == 0)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST,
						"玩家加入房间时，房间不存在，userId=" + userId + "roomNum=" + roomNum);
			}

			TaurusRoom game_room = taurusClub.getRoom(iRoomNum);
			if (game_room == null)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST, "进入房间时，找不到对应的房间,userId=" + userId + ",RoomNum=" + roomNum);
			}
			
			if (game_room != null && game_room.ContainUserId(userId))
			{
				//清理玩家旧的session相关
				game_room.removeSession(userId);
				unbindUserRoom(userId);
			}
         
			synchronized (lock_enterRoom)
			{
				game_room.addSession(userId, session);
				bindUserRoom(userId, game_room);
				/** 发送客户端需要的数据 */
				TaurusTable table = getTable(roomNum);
				OutputMessage om = MessageUtils.getEnterRoomMessage(game_room.getConfig(), roomNum, game_room, table,game_room.getRoomOwnerId());
				om.putInt(game_room.getConfig().getJoinGameScoreLimit());
				om.putInt(taurusClub.getDiamondPercent());
				om.putInt(game_room.getConfig().getGameTime());
				
				//判断玩家游戏开始后能不能坐下
				boolean noEnter = game_room.getConfig().getAdvancedOptions().isNoEnter();
				List<Integer> allPlayerIds = gamePlayerLogAction().getAllPlayerIds(taurusClub.getClubId(), game_room.getRoomId());
				
				om.putBoolean(game_room.isGameStart() && noEnter && !allPlayerIds.contains(Integer.parseInt(userId)) ? false : true);
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
			TaurusClub taurusClub = getUserClub(userId);
			if(taurusClub == null)
			{
				 throw new GameException(GameException.CLUB_IS_NOT_EXIST, "退出俱乐部房间时，俱乐部不存在:userId is"+userId);
			}
			TaurusRoom game_room = taurusClub.getRoom(Integer.parseInt(roomNum));
			if ((getUserTable(userId) != null)&&(game_room != null) && (game_room.isGameStart()) && game_room.getGameStatus() != GameStatus.GAME_STATUS_PAUSE)
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
					/*if (game_room.getSessionList().isEmpty() && getTable(String.valueOf(game_room.getRoomId()))==null)
					{// 房间内已经没有人了，将房间销毁
						removeRoom(roomNum);
						game_room.clear();
						RoomManager.addRoom(game_room);
					}*/
				}

				if (getUserTable(userId) != null)
				{
					synchronized (getUserTable(userId))
					{
						TaurusTable table = getExistTable(roomNum);
						int locationId = table.getUserLocation(userId);
						
						 //游戏未开始玩家买入坐下后，退出房间，积分返还俱乐部成员积分池中
						 TaurusClubMember member = taurusClub.getMember(Integer.valueOf(userId));
						 TaurusSeat seat = table.getSeat(locationId);
						 int scoreTotal = seat.getPlayer().getScoreTotal();
						 if(scoreTotal>0)
						 {
							 clubMemberAction().updateClubMemberCurrentScore(game_room.getClubId(),Integer.valueOf(userId), member.getCurrentScore()+scoreTotal);
							 member.setCurrentScore(member.getCurrentScore()+scoreTotal);
							 
							 if(seat.getPlayer().isNeedReturnDiamond())
							 {
								 int temp = taurusClub.getDiamondPercent()*scoreTotal;
								 int diamond = temp%100 == 0 ? temp/100 : (temp/100)+1;
									
								 userAction().returnRoomOwnerDiamond(String.valueOf(userId), diamond, taurusClub.getClub(), Integer.valueOf(roomNum)); 
							 }
						 }
						exitExistTable(userId, table);
						OutputMessage om = new OutputMessage(true);
						om.putString(userId);
						om.putByte((byte) locationId);
						game_room.sendMessage(PROTOCOL_Ser_Table_Exit, om, session);
					}
				}
			}
			logger.info(" exit room success, userId is " + userId + "roomNum is " + roomNum);
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
	/*public void enterTable(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			String tableId = msg.getUTF(); // 作为房间号码
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
			    game_table.addPlayerForNewH5(game_room,player, user);
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
	}*/

	/** 离开桌子 */
	private void exitExistTable(String userId, TaurusTable table) throws GameException
	{
		roomAction().subOnePlayer(Integer.valueOf(table.getTableId()));
		
		table.removePlayer(userId);
		unbindUserTable(userId);
		/*if (table.getRealPlayer() == 0)
		{
			//table.destory();
			//桌子上已经没有人了，销毁桌子
			removeTable(table);
			table.clear();
			TableManager.addTaurusTable(table);
		}*/
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
			int iRoomNum = Integer.parseInt(roomNum);
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in roomOwnerDissolution , userId is " + userId + ",roomNum is " + roomNum );
			
			getExistUser(userId);//校验用户是否存在
			TaurusClub taurusClub = getUserClub(userId);
			if(taurusClub == null)
			{
				 throw new GameException(GameException.CLUB_IS_NOT_EXIST, "俱乐部房间房主解散房间时，俱乐部不存在:userId is"+userId);
			}
			TaurusRoom room = taurusClub.getRoom(iRoomNum);
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
						//房主解散房间，返回坐下玩家的积分
						TaurusPlayer player = seat.getPlayer();
						int playerId = Integer.valueOf(player.getPlayerId());
						int scoreTotal = player.getScoreTotal();
						int temp = taurusClub.getDiamondPercent()*scoreTotal;
						int diamond = temp%100 == 0 ? temp/100 : (temp/100)+1;
						TaurusClubMember member = taurusClub.getMember(playerId);
						clubMemberAction().updateClubMemberCurrentScore(taurusClub.getClubId(), playerId, member.getCurrentScore()+ scoreTotal);
						member.setCurrentScore(member.getCurrentScore()+ scoreTotal);
						if(diamond > 0)
						{
						   userAction().returnRoomOwnerDiamond(player.getPlayerId(), diamond, taurusClub.getClub(), iRoomNum);
						}
						
						unbindUserTable(player.getPlayerId());
					}					
					//table.destory();
					removeTable(table);
					table.clear();
					TableManager.addTaurusTable(table);
				}
				
				//删除数据库中的房间记录
				roomAction().deletePrivateRoom(Integer.valueOf(roomNum));
				
				//解除房间俱乐部绑定
				clubRoomAction().unBindRoomClub(Integer.valueOf(roomNum));
				
				//清除玩家的进入房间记录
				taurusClub.removeRoom(iRoomNum);
				//BaseServer
				removeRoom(roomNum);
				
				/*List<GameSession> sessions = room.getSessionList();
				for (int i=0; i<sessions.size(); ++i)
				{
					GameSession session_destory = sessions.get(i);
					removeUser(session_destory);
				}*/
				//通知所有人房间已解散
				OutputMessage om = new OutputMessage(true);
				//这里发送客户端需要的数据到客户端
				room.sendMessage(PROTOCOL_Ser_Owner_Dissolution_Notice, om);
				Set<String> userIdList = room.getUserIdList();
				for (String userId1: userIdList)
				{
					unbindUserRoom(userId1);
					room.removeSession(userId1);
				}
				
				//释放房间资源
				room.clear();
				RoomManager.addRoom(room);
				
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
			TaurusClub taurusClub = getUserClub(userId);
			if(taurusClub == null)
			{
				 throw new GameException(GameException.CLUB_IS_NOT_EXIST, "俱乐部玩家申请解散房间时，俱乐部不存在:userId is "+userId);
			}
			TaurusRoom room = taurusClub.getRoom(Integer.parseInt(table.getTableId()));
			if (room == null)
			{
				throw new GameException(GameException.START_GAME_ERROR_ROOM_NOT_EXIST, "玩家申请解散房间失败，房间不存在,userId=" + userId + ",RoomNum=" + table.getTableId());
			}
			PrivateRoom privateRoom = roomAction().getPrivateRoom(Integer.parseInt(table.getTableId()));
			if(privateRoom.getRoomStatus() == 1)
			{
				throw new GameException(GameException.GAME_ERROR_CLUB_DISSOLUTION_GAME_IS_START,"玩家申请解散房间失败,俱乐部游戏开始后禁止解散,userId=" + userId + ",RoomNum=" + table.getTableId());
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
			TaurusClub taurusClub = getUserClub(userId);
			if(taurusClub == null)
			{
				 throw new GameException(GameException.CLUB_IS_NOT_EXIST, "玩家对解散房间的申请的选择失败，俱乐部不存在:userId=" + userId);
			}
			TaurusRoom room = taurusClub.getRoom(Integer.parseInt(table.getTableId()));
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
			TaurusClub taurusClub = getUserClub(userId);
			if(taurusClub == null)
			{
				 throw new GameException(GameException.CLUB_IS_NOT_EXIST, "开始游戏时，俱乐部不存在:userId is "+userId);
			}
			TaurusRoom room = taurusClub.getRoom(roomNum);
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
			//开始游戏后插入房间配置记录
			TaurusRoomConfig config = room.getConfig();
			ClubRoomLog clubRoomLog = new ClubRoomLog();
			clubRoomLog.setAllCompareBaseScore(config.getAllCompareBaseScore().value);
			clubRoomLog.setBankerMode(config.getBankerMode().value);
			clubRoomLog.setBaseScore(config.getClubRoomBaseScore());
			clubRoomLog.setClubId(taurusClub.getClubId());
			clubRoomLog.setGameTime(config.getGameTime());
			clubRoomLog.setPlayedRound(1);
			clubRoomLog.setRoomNum(roomNum);
			clubRoomLog.setRoomOwnerId(room.getRoomOwnerId());
			clubRoomLog.setScoreLimit(config.getJoinGameScoreLimit());
			clubRoomLogAction().addGameRoomLog(clubRoomLog);
			
			session.sendMessage(PROTOCOL_Cli_Start, new OutputMessage(true));
			
			List<GamePlayerLog> playerLogs = new ArrayList<GamePlayerLog>();
			for(int i=0;i<TaurusTable.TABLE_SEAT_NUM; ++i)
			{
				TaurusSeat seat = table.getSeat(i);
				if (seat.isCanSitDown())
				{
					continue;
				}
				TaurusPlayer player = seat.getPlayer();
				User user = seat.getUser();
				GamePlayerLog playerLog = new GamePlayerLog();
				playerLog.setClubId(taurusClub.getClubId());
				playerLog.setClubName(taurusClub.getClubName());
				playerLog.setRoomNum(roomNum);
				playerLog.setPlayerId(Integer.valueOf(player.getPlayerId()));
				playerLog.setBuyScore(player.getScoreTotal());
				playerLog.setNickName(user.getNickName());
				playerLog.setHeadImgUrl(user.getHeadImgUrl());
				playerLogs.add(playerLog);
			}
			gamePlayerLogAction().insertPlayerLogBatch(playerLogs);
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
			TaurusClub taurusClub = getUserClub(userId);
			if(taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST, "玩家准备时，俱乐部不存在,userId =" + userId);
			}
			TaurusRoom room = taurusClub.getRoom(Integer.parseInt(table.getTableId()));
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
			TaurusClub taurusClub = getUserClub(userId);
			if(taurusClub == null)
			{
				 throw new GameException(GameException.CLUB_IS_NOT_EXIST, "庄家选择押注底分时，俱乐部不存在:userId =" + userId);
			}
			TaurusRoom room = taurusClub.getRoom(Integer.parseInt(table.getTableId()));
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
			TaurusClub taurusClub = getUserClub(userId);
			if(taurusClub == null)
			{
				 throw new GameException(GameException.CLUB_IS_NOT_EXIST, "玩家押注时，俱乐部不存在:userId =" + userId);
			}
			TaurusRoom room = taurusClub.getRoom(Integer.parseInt(table.getTableId()));
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
			if ((!checkBet(room.getConfig().getClubRoomBaseScore(), bet_coin)) && (bet_coin!=seat.getCurRoundInjectionScore()))
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
	
	private boolean checkBet(int baseScore, int betCoin)
	{
		return betCoin == baseScore || betCoin == 2*baseScore ;
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
			TaurusClub taurusClub = getUserClub(userId);
			if(taurusClub == null)
			{
				 throw new GameException(GameException.CLUB_IS_NOT_EXIST, "玩家亮牌时，俱乐部不存在:userId =" + userId);
			}
			TaurusRoom room = taurusClub.getRoom(Integer.parseInt(table.getTableId()));
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
			TaurusClub taurusClub = getUserClub(userId);
			if(taurusClub == null)
			{
				 throw new GameException(GameException.CLUB_IS_NOT_EXIST, "玩家抢庄时，俱乐部不存在:userId =" + userId);
			}
			TaurusRoom room = taurusClub.getRoom(Integer.parseInt(table.getTableId()));
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
	
	/**
	 * 拉取详细手牌记录详情(记录里面的二级详情)
	 */
	public void getDetailRecord(SocketRequest request, SocketResponse response) 
	{
		try {
			InputMessage im = request.getInputMessage();
			String userId = im.getUTF();
			String roundIndex = im.getUTF();
			String sessionId = im.getUTF();
			int index2 = im.getInt();

			logger.info("玩家userId=：" + userId + "拉取手牌的战绩详情！索引roomIndex=" + roundIndex);
			userAction().getExistUser(userId);// 检测玩家是否存在

			OutputMessage om = new OutputMessage(true);
			String[] index = roundIndex.split(",");
			int clubId = Integer.valueOf(index[0]);
			int roomNum = Integer.valueOf(index[1]);
			int round = Integer.valueOf(index[2]);
			
			GameRoundLog userHandCardLog = gameRoundLogAction().getRoundDetails(clubId, roomNum, round);
			
			if (userHandCardLog == null) 
			{
				throw new GameException(GameException.GET_HAND_CARD_LOG_IS_NULL,
						"玩家userId:" + userId + "拉取二级详情时，拉取的userHandCardLog对象为空");
			}

			om.putString(userHandCardLog.getBankerId());// 庄家的id

			// 通过房间号得到userGameLog对象，在得到上庄模式的类型
			ClubRoomLog userGameLog = clubRoomLogAction().getClubRoomLogByRoomId(userHandCardLog.getRoomNum());
			if (userGameLog == null)
			{
				throw new GameException(GameException.USERGAEMLOG_Not_Exist,
						"玩家userId=" + userId + "通过房间号：" + userHandCardLog.getRoomNum() + "拉取的对象userGameLog不存在！");
			}
			// 6个玩家的依次手牌信息
			for (int i = 0; i < PLAYER_NUM; i++) 
			{

				if (userHandCardLog.getGetPlayerIdx(i+1) != null && !userHandCardLog.getGetPlayerIdx(i+1).isEmpty()) 
				{
					om.putString(userHandCardLog.getGetPlayerIdx(i+1));
					User u = userAction().getExistUser(userHandCardLog.getGetPlayerIdx(i+1));
					om.putString(u == null ? "" : u.getNickName());
					if (userGameLog.getBankerMode() == BankerMode.BANKER_MODE_FREE.value)// 自由抢庄模式
					{
						om.putByte(userHandCardLog.getIsRobBankerx(i+1));// 第一个位置的玩家是否抢庄 0--没有抢庄// 1---表示有抢(自由抢庄特有)
						
					} else 
					{ // 不是自由抢庄模式 值恒 0
						om.putByte((byte) 0);
					}
					if (userGameLog.getBankerMode() == BankerMode.BANKER_MODE_BRIGHT_ROB.value) // 明牌抢庄模式
					{
						om.putByte(userHandCardLog.getRobBankerNumx(i+1));// 位置1玩家的抢庄倍率 0--没抢 1：x2
																		  // 2:x4 3:x8(明牌抢庄特有)
					} else 
					{// 非明牌抢庄模式 值恒 0
						om.putByte((byte) 0);
					}
					om.putInt(userHandCardLog.getGetScoreTotalx(i+1));
					om.putInt(userHandCardLog.getBaseScorex(i+1)); // 位置1的玩家的押注底分
					om.putInt(userHandCardLog.getGetScorex(i+1)); // 每个玩家当前局的得分
					om.putInt(userHandCardLog.getCardTypex(i+1)); // 位置1玩家的牌的类型
					om.putString(userHandCardLog.getCardsx(i+1)); //手牌数组
					
				}else
				{
					om.putString("");
					om.putString("");
					om.putByte((byte)0);
					om.putByte((byte)0);
					om.putInt(0);
					om.putInt(0); // 位置1的玩家的押注底分
					om.putInt(0); // 每个玩家当前局的得分
					om.putInt(0); // 位置1玩家的牌的类型
					om.putString(""); //手牌数组
				}

			}
			
			logger.info("玩家userId=" + userId + "拉取手牌记录成功,该局的索引是" + roundIndex);
			om.putInt(index2);
			response.sendMessage(PROTOCOL_Cli_GetDetailRecord, om);

		} catch (GameException e) 
		{
			sendError(response, PROTOCOL_Cli_GetDetailRecord, e.getId());
			
		} catch (Exception e)
		{
			logger.error(e.getMessage(), e);
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
			
			String roundIndex = room.getClubId()+","+room.getRoomId()+","+(table.getCurRound()-1);
			
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
			TaurusClub taurusClub = getUserClub(userId);
			if(taurusClub == null)
			{
				 throw new GameException(GameException.CLUB_IS_NOT_EXIST, "玩家托管时，俱乐部不存在:userId =" + userId);
			}
			TaurusRoom room = taurusClub.getRoom(Integer.parseInt(table.getTableId()));
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
	
	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * room end * * *  * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  * * * * * * * * * * * * * * * * * * * *
	 * */
	
}
