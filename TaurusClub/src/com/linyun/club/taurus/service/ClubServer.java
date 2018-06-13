package com.linyun.club.taurus.service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.linyun.bottom.util.InputMessage;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.club.taurus.timer.RoomTimeOutTask;
import com.linyun.club.taurus.utils.ClubIdUtil;
import com.linyun.club.taurus.utils.TimeUtil;
import com.linyun.common.entity.Club;
import com.linyun.common.entity.ClubMember;
import com.linyun.common.entity.ClubMessage;
import com.linyun.common.entity.ClubRoomLog;
import com.linyun.common.entity.GamePlayerLog;
import com.linyun.common.entity.PrivateRoom;
import com.linyun.common.entity.User;
import com.linyun.common.taurus.club.eum.ClubPosition;
import com.linyun.middle.common.taurus.club.BaseClubServer;
import com.linyun.middle.common.taurus.club.ClubConfig;
import com.linyun.middle.common.taurus.club.ClubConfig.Config;
import com.linyun.middle.common.taurus.club.TaurusClub;
import com.linyun.middle.common.taurus.club.TaurusClubMember;
import com.linyun.middle.common.taurus.room.TaurusRoom;
import com.linyun.middle.common.taurus.service.BaseServer;

/**
 * 俱乐部接口类
 * */
public class ClubServer extends BaseClubServer
{
	private static Logger logger = LoggerFactory.getLogger(ClubServer.class);
	
	/************************************************************俱乐部相关协议***************************************************************/
	public static final short Protocol_Cli_Enter_Lobby_Club = 7041;//进入俱乐部大厅
	public static final short Protocol_Cli_Leave_Lobby_Club = 7042;//离开俱乐部大厅
	public static final short Protocol_Cli_Enter_Club = 7043;//进入俱乐部
	public static final short Protocol_Cli_Leave_Club = 7044;//离开俱乐部
	public static final short Protocol_Cli_Get_Club_All_Room = 7045;//获取俱乐部的已开房间列表信息
	public static final short Protocol_Cli_Get_Club_Data = 7046;//拉取俱乐部信息
	public static final short Protocol_Cli_Get_All_Clubs = 7047;//拉取个人所有的俱乐部
	public static final short Protocol_Cli_Action_Create_Club = 7051;//代理创建俱乐部
	public static final short Protocol_Cli_Action_Del_Club = 7052;//俱乐部创建者删除俱乐部 
	public static final short Protocol_Cli_Action_Apply_Join_Club = 7053;//非俱乐部创建者申请加入俱乐部
	public static final short Protocol_Cli_Search_Club = 7054;//根据输入的俱乐部id搜索俱乐部
	public static final short Protocol_Cli_Action_Exit_Club = 7055;//退出俱乐部
	public static final short Protocol_Cli_Get_User_List = 7057;//根据俱乐部id获取俱乐部成员列表
	public static final short Protocol_Cli_Serch_User = 7058; //俱乐部创建者搜索其他玩家
	public static final short Protocol_Cli_Action_Invite_Join_Club = 7059;//俱乐部创建者邀请其他玩家加入俱乐部
	public static final short Protocol_Cli_Action_Handle_Apply = 7071;//俱乐部创建者对申请加入俱乐部玩家做出的答复 <通过or拒绝>
	public static final short Protocol_Cli_Action_Kick_Member = 7072;//俱乐部创建者踢出群成员
	public static final short Protocol_Cli_Action_Set_Club_ConfigInfo = 7073;//俱乐部创建者设置俱乐部配置信息
	public static final short Protocol_Cli_Get_Club_User_Msg = 7081;//拉取玩家所有的消息
	public static final short Protocol_Cli_Read_Club_User_Msg = 7082;//玩家读取某一条消息
	public static final short Protocol_Cli_Check_User_New_Msg = 7083 ;//检查玩家是否有未读的新消息
	
	public static final short Protocol_Cli_Get_Club_Game_Record=7092;//获取俱乐部战绩
	public static final short Protocol_Cli_Record_Detail = 7093;//获取俱乐部战绩详情
	
	public static final short Protocol_Ser_Action_Invite_Join_Club = 8059;//俱乐部邀请玩家（创建者邀请） 该协议发送给被邀请者(如果在线)
	public static final short Protocol_Ser_Action_Handle_Apply = 8071;//创建者-玩家申请加入俱乐部（玩家） 同意之后需要通知申请者
	public static final short Protocol_Ser_Action_Kick_Member = 8072;//创建者-踢出玩家（玩家） 该协议发送给被提出者(如果在线)
	public static final short Protocol_Ser_New_Msg = 8081; // 通知客户端有新消息 消息会有类型 1-代表此消息是一个申请加入俱乐部的消息，需要特殊处理  默认为0	
	
	public static final short PROTOCOL_Ser_Disconnect = 9011;//玩家掉线通知客户端
	/***************************************************************************************************************************/
	
	public static final int CLUB_SCOREPOOL_INITIAL_VALUE = 1000000000;//俱乐部创建后积分池初始化为十亿
	public static final int CLUB_DIAMONDPERCENT_INITIAL_VALUE = 10 ; //俱乐部创建时设置扣钻百分比初始值为10%
	public static final int CLUB_EXPANDRATE_INITIAL_VALUE=1;  //俱乐部小盲的拉伸倍率的初始值
	//public static final int CLUB_SCORERATE_INITIAL_VALUE=500; //俱乐部最低买入底分倍率初始值
	
	/**
	 *  在服务器启动的时候  加载所有数据库的俱乐部信息(包括俱乐部成员信息)到内存中
	 *  在创建或者删除俱乐部 --- 增加、移除俱乐部信息
 	 *  批准加入或者踢出成员  --- 增加、移除成员信息
	 * */
	public void init()
	{
		List<Club> clubs = clubAction().getAllClub();
		TaurusClub taurusClub = null;
		TaurusClubMember privateClubMember = null;
		for (Club club: clubs)
		{
			taurusClub = new TaurusClub(club);
			List<ClubMember> members = clubMemberAction().getAllMember(club.getClubId());
			for (ClubMember member:members)
			{
				privateClubMember = new TaurusClubMember(member);
				taurusClub.addMember(privateClubMember);
			}
			if (club.getClubType() == 1)//公共俱乐部
			{
				publicClubIdList.add(club.getClubId());
			}
			bindClub(taurusClub);
			
			/*List<PrivateRoom> clubRoomList = roomAction().getClubRoomList(club.getClubId());
			if(!clubRoomList.isEmpty())
			{   
				TaurusRoom room = null ;
				for (PrivateRoom db_room : clubRoomList)
				{
					room = new TaurusRoom(db_room);
					taurusClub.addRoom(room);
					BaseServer.addRoom(String.valueOf(room.getRoomId()), room);
				}
			}*/
		}
		roomAction().deleteAllRooms(); //每次启动服务器时清空所有房间
		
		RoomTimeOutTask.getInstance().start(); //俱乐部超时房间处理
	}
	
	public ClubServer()
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
						//clearSession(session);
					}
				} 
				else if (obj instanceof String)
				{
				}
			}
		});
	}
	

	/** * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * Club begin * * *  * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  * * * * * * * * * * * * * * * * * * * * */
	//进入俱乐部大厅
	public void enterLobbyClub(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			String sessionId = msg.getUTF();
			filterSessionId(userId, sessionId);
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in enterLobbyClub, userId is " + userId);
			//进入俱乐部大厅第一条协议时拉取俱乐部信息，在这个时候将玩家的session加入到基础的session里面去
			addUser(userId, session);
			//获取公共俱乐部信息
			OutputMessage om = getAllPublicMessage();
			int iUserId = Integer.valueOf(userId);
			//获取用户相关的私人俱乐部信息
			List<String> allPriClubId = clubMemberAction().getAllClubId(iUserId);
			TaurusClub priClub = null;
			om.putByte((byte)(allPriClubId.size()));
			for (String clubId:allPriClubId)
			{
				priClub = getClub(Integer.parseInt(clubId));
				if (priClub.getClubType() == 0)
				{
					getClubMessage(priClub, om);
				}
			}
			session.sendMessage(Protocol_Cli_Enter_Lobby_Club, om);
			logger.info("userId is "+userId+" enter clubLobby success!");
		} 
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Enter_Lobby_Club, e.getId());
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
	
	//获取玩家的俱乐部游戏战绩
	public void getClubGameRecord(SocketRequest request, SocketResponse response)
	{
		InputMessage im = request.getInputMessage();
		GameSession session = request.getSession();
		
		try
		{
			String userId = im.getUTF();
			getExistUser(userId);// 校验用户是否存在
			List<GamePlayerLog> gameRecord = gamePlayerLogAction().getGameRecord(Integer.parseInt(userId));
			
			OutputMessage om = new OutputMessage(true);
			om.putInt(gameRecord.size());
			for(GamePlayerLog g : gameRecord)
			{
				om.putString(TimeUtil.formatDate(g.getClubRoomLog().getCreateTime()));
				om.putString(TimeUtil.formatDate(g.getClubRoomLog().getUpdateTime()));
				om.putInt(g.getClubId());
				om.putString(g.getClubName());
				om.putInt(g.getResult());
				om.putString(String.valueOf(g.getRoomNum()));
			}
			
			session.sendMessage(Protocol_Cli_Get_Club_Game_Record, om);
			
		}catch(GameException e)
		{
			sendError(response, Protocol_Cli_Get_Club_Game_Record, e.getId());
		}catch(Exception e)
		{
			logger.error(e.getMessage(),e);
		}
	}
	
	//获取玩家游戏记录详情
	public void getGameRecordDetail(SocketRequest request, SocketResponse response)
	{
		InputMessage im = request.getInputMessage();
		GameSession session = request.getSession();
		try
		{
			int clubId = im.getInt();
			String roomNum = im.getUTF();
			int iRoomNum = Integer.parseInt(roomNum);
			
			ClubRoomLog detail = clubRoomLogAction().getClubRoomLogByRoomId(iRoomNum);
			List<GamePlayerLog> allPlayerLog = gamePlayerLogAction().getAllPlayerLog(clubId, iRoomNum);
			OutputMessage om = new OutputMessage(true);
			if(detail != null)
			{
				om.putString(roomNum);
				om.putInt(detail.getGameTime());
				om.putInt(detail.getBaseScore());
				om.putInt(detail.getBankerMode());
				om.putInt(allPlayerLog.size());
				for(GamePlayerLog g : allPlayerLog)
				{
					om.putString(g.getNickName());
					om.putInt(g.getBuyScore());
					om.putInt(g.getResult());
				}
			}
			session.sendMessage(Protocol_Cli_Record_Detail, om);
			
		}catch(GameException e)
		{
			sendError(response, Protocol_Cli_Record_Detail, e.getId());
		}catch(Exception e)
		{
			logger.error(e.getMessage(),e);
		}
	}
	//离开俱乐部大厅
	public void leaveLobbyClub(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			getExistUser(userId);// 校验用户是否存在
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in leaveLobbyClub, userId is " + userId);
			//进入俱乐部大厅第一条协议时拉取俱乐部信息，在这个时候将玩家的session加入到基础的session里面去
			removeUser(session);
			OutputMessage om = new OutputMessage(true);
			session.sendMessage(Protocol_Cli_Leave_Lobby_Club, om);
			logger.info("userId is "+userId+" leaveLobbyClub success!");
		} 
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Leave_Lobby_Club, e.getId());
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
	//进入俱乐部
	public void enterClub(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			int clubId = msg.getInt();
			getExistUser(userId);// 校验用户是否存在
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in enterClub, userId is " + userId+"clubId is "+clubId);
			
			Integer iUserId = Integer.valueOf(userId);
			TaurusClub taurusClub = getClub(clubId);
			if (taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST,"玩家：userId="+userId+"进入俱乐部时，俱乐部clubId="+clubId+"不存在！");
			}	
			//进入的是私人俱乐部，必须是俱乐部成员   
			if(taurusClub.getClubType() == 0 && !taurusClub.ContainsUser(iUserId))
			{
				throw new GameException(GameException.CLUB_USER_ALREADY_JOIN_CLUB,"玩家：userId ="+userId+"进入俱乐部时，不是俱乐部成员，不能进入");
			}
			// 玩家首次进入公共俱乐部（公共俱乐部可以直接进入）
			if(taurusClub.getClubType() == 1 && !taurusClub.ContainsUser(iUserId))
			{  
				//成员相关
				ClubMember clubMember = new ClubMember(clubId,iUserId);
				int pubClubUserCostLimit = ClubConfig.INSTANCE.getConfig().getPubClubUserCostLimit();
				clubMember.setDiamondLimit(pubClubUserCostLimit);
				TaurusClubMember tcMember = new TaurusClubMember(clubMember);
				//消息相关
				String applyContent = "您已经成功加入"+taurusClub.getClubName()+"("+clubId+")俱乐部";
				ClubMessage applyMsg = new ClubMessage(clubId, iUserId, applyContent);
				clubCommonAction().JoinPublicClub(clubId, iUserId, clubMember, applyMsg);
				synchronized (taurusClub)
				{
					taurusClub.addUserSession(iUserId, session);
					bindUserClub(userId, taurusClub);
					taurusClub.addOneOnlineCount();//在线人数+1
					taurusClub.addOneTotalCount();//总人数+1
					taurusClub.addMember(tcMember);
					tcMember.online();//在线
				}
			}
			//玩家已经是俱乐部成员
			else
			{
				synchronized (taurusClub)
				{   
					TaurusClubMember tcMember = taurusClub.getMember(iUserId);
					taurusClub.addUserSession(iUserId, session);
					bindUserClub(userId, taurusClub);
					taurusClub.addOneOnlineCount();//在线人数+1
					tcMember.online();
				}
			}
			OutputMessage om = new OutputMessage(true);
			om.putInt(clubId);
			om.putInt(taurusClub.getExpandRate());
			om.putInt(taurusClub.getScoreRate());
			session.sendMessage(Protocol_Cli_Enter_Club, om);
			logger.info("userId is"+userId+",clubId is"+clubId+" enter club success！");
		} 
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Enter_Club, e.getId());
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
	
	//离开俱乐部
	public void leaveClub(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			int iUserId = Integer.parseInt(userId);
			int clubId = msg.getInt();
			getExistUser(userId);// 校验用户是否存在
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in leaveClub, userId is " + userId + ",clubId is " + clubId);
			TaurusClub taurusClub = getClub(clubId);
			if (taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST,"玩家：userId="+userId+"离开俱乐部时，俱乐部clubId="+clubId+"不存在！");
			}			
			synchronized (taurusClub)
			{   
				TaurusClubMember tcMember = taurusClub.getMember(iUserId);
				taurusClub.removeUserSession(userId);
				unbindUserClub(userId);
				taurusClub.subOneOnlineCount();//在线人数-1
				tcMember.disConnect();//不在线
			}
			OutputMessage om = new OutputMessage(true);
			om.putInt(clubId);
			session.sendMessage(Protocol_Cli_Leave_Club, om);
			logger.info("userId is "+userId+",clubId is "+clubId+" leaveClub success"); 
		} 
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Leave_Club, e.getId());
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
	
	//拉取玩家个人所有的俱乐部信息
	public void getAllClubs(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null ;
		try
		{
			String userId = msg.getUTF();
			getExistUser(userId);// 校验用户是否存在
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in enter getAllClubs,userId is "+userId);
			//获取公共俱乐部信息
			OutputMessage om = getAllPublicMessage();
			//获取用户相关的私人俱乐部信息
			TaurusClub priClub = null;
			Set<String> clubIdMap = RedisResource.getAllClubs(userId);
			byte priClubCount = (byte)(clubIdMap.size());
			om.putByte(priClubCount);
			Iterator<String> iterator = clubIdMap.iterator();
			while (iterator.hasNext())
			{
				priClub = getClub(Integer.parseInt(iterator.next()));
				if (priClub.getClubType() == 0)
				{
					getClubMessage(priClub, om);
				}
			}
			
			session.sendMessage(Protocol_Cli_Get_All_Clubs, om);
			logger.info("userId is "+userId+"getAllClubs success!");
		}
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Get_All_Clubs, e.getId());
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
	
	//获取俱乐部房间列表
	public void getClubAllRoom(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			int clubId = msg.getInt();
			getExistUser(userId);// 校验用户是否存在
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in getClubAllRoom, userId is " + userId + ",clubId="+clubId);
			TaurusClub taurusClub = getClub(clubId);
			if (taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST,"玩家：userId="+userId+"离开俱乐部时，俱乐部clubId="+clubId+"不存在！");
			}	
			
			OutputMessage om = taurusClub.getAllRoomInfo();
			session.sendMessage(Protocol_Cli_Get_Club_All_Room, om);
			logger.info("userId is "+userId+",clubId is "+clubId+" getClubAllRoom success");
		} 
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Get_Club_All_Room, e.getId());
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
	
	//创建俱乐部
	public void actionCreateClub(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{   
			String userId = msg.getUTF();
			String clubName = msg.getUTF();  //所创建的俱乐部的名称
			String clubIntroduction = msg.getUTF(); //俱乐部的介绍
			String clubIconUrl = msg.getUTF(); //所选的俱乐部的图标
			String clubCity = msg.getUTF();//俱乐部所属城市
			
			logger.info("in create private club ,userId is "+userId);
			User user = getExistUserBySql(userId);// 校验用户是否存在
			//是不是代理判断待定
			
			String regex = "^[\u4e00-\u9fa5a-zA-Z0-9]{2,7}$";
			Pattern p = Pattern.compile(regex);
			Matcher matcher = p.matcher(clubName);
			boolean flag = matcher.matches();
			if(!flag)
			{
				throw new GameException(GameException.NICK_NAME_IS_NOT_STANDARD,"玩家：userId = "+userId+"创建俱乐部,俱乐部昵称不合乎规范！");
			}
			
			if(clubIntroduction.trim().length() > 50) //俱乐部字数不超过50字
			{
				throw new GameException(GameException.CLUB_INTRODUCE_TOO_LONG,"玩家：userId = "+userId+"创建俱乐部,俱乐部介绍字数过多！");
			}
			//已经拥有和正在申请的俱乐部是否达到上限，达到则不能创建俱乐部
			reachClubCountLimit(user);
			//玩家只能创建一个俱乐部
			if(clubAction().getClubByCreatorId(Integer.parseInt(userId)) != null)
			{
				throw new GameException(GameException.USER_ALREADY_CREATE_CLUB,"玩家:userId = "+userId+"已经创建过俱乐部,不可再创建");
			}
			sem = getSemp(userId);
			sem.acquire();
			
			//生成俱乐部的Id
			int clubId = ClubIdUtil.generatClubId();
			Config config = ClubConfig.INSTANCE.getConfig();
			//俱乐部人数的上限
			int clubPeopleMaxNum = config.getPriClubPeopleMaxNum();
			//俱乐部允许的最大开桌数
			int clubMaxTableNum = config.getPriClubMaxTableNum();
			
			//生成一条私人俱乐部信息
			Club club = new Club();
			club.setClubId(clubId);
			club.setClubName(clubName); 
			club.setClubType((byte)0);//0--私人俱乐部  1--公共俱乐部
			club.setIconUrl(clubIconUrl);
			club.setCreatorId(Integer.parseInt(userId));
			club.setCity(clubCity);
			club.setPeopleCount(1);//群主本人已加入
			club.setClubIntroduce(clubIntroduction);
			club.setScorePool(CLUB_SCOREPOOL_INITIAL_VALUE);
			club.setDiamondPercent(CLUB_DIAMONDPERCENT_INITIAL_VALUE);
			club.setExpandRate(CLUB_EXPANDRATE_INITIAL_VALUE);
			club.setScoreRate(config.getCreateClubScoreRateInitialValue());
			//生成一条消息
			String creatorContent = "您已成功创建" + clubName + "("+clubId+")俱乐部";
			ClubMessage msgCreator = new ClubMessage(clubId, user.getUserId(), creatorContent);
			
			//将群主加入到俱乐部成员表中
			ClubMember clubMember = new ClubMember(club.getClubId(), user.getUserId());
			clubMember.setDiamondLimit(user.getDiamond());//群主拥有的钻石，成员钻石由群主设置
			clubMember.setPosition(ClubPosition.POSITION_CREATOR.value);
			
			clubCommonAction().CreatorCreateClub(club,msgCreator,clubMember);
			
			//玩家创建俱乐部后,将玩家的身份设置成为代理
			userAction().setProxy(Integer.parseInt(userId));
			
			//群主创建俱乐部
			TaurusClubMember taurusClubMember = new TaurusClubMember(clubMember);
			
			//绑定与俱乐部相关数据
			TaurusClub taurusClub = new TaurusClub(club);
			//taurusClub.addUserSession(Integer.parseInt(userId), session);
			taurusClub.addMember(taurusClubMember);
			bindClub(taurusClub);
			
			OutputMessage om = new OutputMessage(true);
			om.putInt(clubId);
			om.putString(clubName);
			om.putString(clubIntroduction);
			om.putInt(clubPeopleMaxNum);
			om.putString(clubIconUrl);
			om.putString(userId);
			om.putString(user.getNickName());
			om.putString(user.getHeadImgUrl());
			om.putString(clubCity);//俱乐部所属地址
			om.putInt(clubMaxTableNum);
			om.putInt(taurusClub.getOnlineCount()); //俱乐部在线人数
			om.putInt(club.getPeopleCount()); //俱乐部总成员
			session.sendMessage(Protocol_Cli_Action_Create_Club, om);
			logger.info("userId is "+userId+",clubId is "+clubId+" create private club success");
		} 
		catch (GameException e)
		{   
			sendError(response, Protocol_Cli_Action_Create_Club, e.getId());
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
		
	//群主删除俱乐部，只有在俱乐部只有群主一个人的时候才可以删除
	public void actionDelClub(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			int clubId = msg.getInt();
			logger.info("in delete club, userId is " + userId + "clubId is " + clubId);
			sem = getSemp(userId);
			sem.acquire();
			getExistUser(userId);// 校验用户是否存在
			//检测俱乐部是否存在
			TaurusClub taurusClub = getClub(clubId);
			if(taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST,"玩家：userId="+userId+"删除俱乐部时，俱乐部clubId="+clubId+"不存在！");
			}
			//如果玩家不是该俱乐部的群主，不可进行删除操作
			int iUserId = Integer.valueOf(userId);
			if(!taurusClub.IsCreator(iUserId))
			{
				throw new GameException(GameException.CLUB_USER_IS_NOT_CLUB_CREATOR,"玩家：userId="+userId+"删除俱乐部时，玩家不是群主！");
			}
			
			//俱乐部除了群主是否还有其他玩家
			List<ClubMember> allMembers = clubMemberAction().getAllMember(clubId);
			if(allMembers.size() > 1)
			{
				throw new GameException(GameException.CLUB_HAS_OTHER_MEMBERS,"玩家：userId="+userId+"删除俱乐部时，还有其他成员！");
			}
			
			String creatorContent = "您已成功删除"+taurusClub.getClubName()+"("+clubId+")俱乐部";
			ClubMessage msgCreator = new ClubMessage(clubId, iUserId, creatorContent);
			clubCommonAction().CreatorDeleteClub(iUserId, clubId, msgCreator);
			//俱乐部群主删除俱乐部，取消其代理
			userAction().cancelProxy(iUserId);
			//对于该俱乐部中没有处理的申请消息
			List<ClubMessage> msgList = clubMessageAction().selectAllApplyMsgInDelClub(clubId);
			if(msgList.size() != 0)
			{
				GameSession applySession = null ;
				for (ClubMessage clubMessage : msgList)
				{
					int applyUserId = clubMessage.getApplyId();
					String applyContent = "您申请的俱乐部"+taurusClub.getClubName()+"("+clubId+")已经解散" ;
					ClubMessage msgApply = new ClubMessage(clubId, applyUserId, applyContent);
					clubMessageAction().addOneClubMessage(msgApply);
					applySession = getUserSession(String.valueOf(applyUserId));
					//玩家是否在线
					if(applySession != null)
					{
						applySession.sendMessage(Protocol_Ser_New_Msg, new OutputMessage(true));  
					}
				}
			}
			
			
			synchronized (taurusClub)
			{
				taurusClub.removeMember(iUserId);
				taurusClub.removeUserSession(userId);
				unbindUserClub(userId);
				removeClub(clubId);
			}
			
			OutputMessage om = new OutputMessage(true);
			om.putInt(clubId);
			session.sendMessage(Protocol_Cli_Action_Del_Club, om);
			
			//如果创建者在线，通知创建者有新消息
			session.sendMessage(Protocol_Ser_New_Msg, new OutputMessage(true));
			logger.info("userId is "+userId+" clubId is "+clubId+" delete private club success");
		} 
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Action_Del_Club, e.getId());
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
	
	//判断玩家申请和拥有的俱乐部是否达到上限
	public void  reachClubCountLimit(User user)
	{
		int clubTotalCount = user.getApplyClubCount() + user.getClubCount(); //申请和加入的数量
		int clubLimit = ClubConfig.INSTANCE.getConfig().getUserCanJoinPriClubNum();//允许限制的数量
		if(clubTotalCount >= clubLimit)
		{
			throw new GameException(GameException.CLUB_COUNTS_REACH_LIMIT,"玩家：userId = "+user.getUserId()+"创建，被邀请，申请加入俱乐部，拥有的俱乐部达到上限");
		}
	}
	
	//私人俱乐部的人数达到上限
	public void reachPriClubPeopleNumLimit(int clubId)
	{ 
		 List<ClubMember> memList = clubMemberAction().getAllMember(clubId);
		 if(!memList.isEmpty() && memList.size() >= ClubConfig.INSTANCE.getConfig().getPriClubPeopleMaxNum())
		 {
			 throw new GameException(GameException.CLUB_PEOPLE_REACH_LIMIT,"俱乐部:clubId = "+clubId+"人数达到上限,不能加入");
		 }
	}
	
	
	//申请加入俱乐部
	public void actionApplyJoinClub(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			int clubId = msg.getInt();
			logger.info("in apply for join club, userId is " + userId + "clubId is " + clubId);
			sem = getSemp(userId);
			sem.acquire();
			
			User user = getExistUser(userId);//检测用户是否存在
			TaurusClub taurusClub = getClub(clubId);
			if(taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST,"玩家：userId is "+userId+"申请加入俱乐部 ,俱乐部clubId ="+clubId+"不存在！");
			}
			int iUserId = Integer.valueOf(userId);
			if(taurusClub.IsCreator(iUserId))
			{
				throw new GameException(GameException.CLUB_USER_ALREADY_JOIN_CLUB,"玩家：userId ="+userId+"申请加入俱乐部时，是俱乐部的创建者，clubId is"+clubId);
			}
			//玩家是否是俱乐部成员
			if(taurusClub.ContainsUser(iUserId))
			{
				throw new GameException(GameException.CLUB_USER_ALREADY_JOIN_CLUB,"玩家：userId ="+userId+"申请加入俱乐部时，已经是俱乐部"+clubId+"成员");
			}
			//玩家已经向该俱乐部提交了申请，三天内等待群主答复，如果没有答复或者拒绝，玩家可以继续申请
			if(clubMessageAction().isApplyJoinClub(clubId,iUserId) != null)
			{
				throw new GameException(GameException.USER_ALREADY_APPLY_JOIN_CLUB,"玩家：userId ="+userId+"申请加入俱乐部:clubId"+clubId+"时，不可重复申请加入");
			}
			//玩家已经申请的俱乐部和拥有的是否达到上限
			reachClubCountLimit(user);
			//检测俱乐部人数是否达到上限
			reachPriClubPeopleNumLimit(clubId);
			if (taurusClub.getClubType() == 1)
			{
				throw new GameException(GameException.PUB_CLUB_DOESNOT_NEED_APPLY_JOIN,"玩家：userId ="+userId+
						"申请加入俱乐部:clubId"+clubId+"时，"+"俱乐部是公共，不需要申请可以直接加入");

			}
			
			String creatorContent = user.getNickName()+"("+user.getUserId()+")"+"申请加入"+taurusClub.getClubName()+"("+clubId+")俱乐部";
			ClubMessage msgCreator = new ClubMessage(clubId, taurusClub.getCreatorId(), iUserId, creatorContent);
			clubCommonAction().UserApplyJoinClub(iUserId, clubId, msgCreator);
			GameSession creatorSession = getUserSession(String.valueOf(taurusClub.getCreatorId()));
			if (creatorSession != null)
			{
				//如果创建者在线，通知创建者有新消息
				OutputMessage emptyOm = new OutputMessage(true);
				creatorSession.sendMessage(Protocol_Ser_New_Msg, emptyOm);
			}
						
			//俱乐部创建者获得玩家申请消息通知，如果在线则推送，否则创建者下次登录拉取
			OutputMessage om = new OutputMessage(true);
			session.sendMessage(Protocol_Cli_Action_Apply_Join_Club, om);
			logger.info("userId is "+userId+",clubId is "+clubId+" apply join club success");
		} 
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Action_Apply_Join_Club, e.getId());
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
	
	//拉取俱乐部详情
	public void getClubDetail(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			int clubId = msg.getInt();
			sem = getSemp(userId);
			sem.acquire();
			getExistUser(userId);// 校验用户是否存在
			logger.info("in getClubDetail, userId=" + userId + ",clubId=" + clubId);
			TaurusClub taurusClub = getClub(clubId);
			if (taurusClub == null)
			{
                throw new GameException(GameException.CLUB_IS_NOT_EXIST,"玩家:userId= "+userId+"拉取俱乐部详情时，俱乐部clubId ="+clubId+"不存在！");
			}
			//玩家是否是俱乐部成员
			int iUserId = Integer.parseInt(userId);
			
			if(!taurusClub.ContainsUser(iUserId))
			{
				throw new GameException(GameException.CLUB_USER_ALREADY_JOIN_CLUB,"玩家：userId ="+userId+"拉取俱乐部详情时，不是俱乐部成员，不能拉取");
			}
			
			OutputMessage om = new OutputMessage(true);
			synchronized (taurusClub)
			{
				om.putInt(clubId);
				om.putString(taurusClub.getClubName()); 
				om.putString(taurusClub.getClubIntroduce());
				om.putInt(ClubConfig.INSTANCE.getConfig().getPriClubPeopleMaxNum());
				om.putString(taurusClub.getIconUrl());
				if(taurusClub.getClubType() == 0)
				{
					String creatorId = String.valueOf(taurusClub.getCreatorId());
					om.putString(creatorId);
					User creatorUser = getExistUser(creatorId);
					om.putString(creatorUser.getNickName());
					om.putString(creatorUser.getHeadImgUrl());
				}
				else
				{
					om.putString("");
					om.putString("");
					om.putString("");
				}
				om.putString(taurusClub.getClubCity());
				om.putInt(taurusClub.getAllRoomCount()); //当前的开桌数
				om.putInt(taurusClub.getOnlineCount()); //在线人数
				om.putInt(taurusClub.getTotalCount()); //总成员数
			}
			session.sendMessage(Protocol_Cli_Get_Club_Data, om);
			logger.info("userId is "+userId+",clubId is "+clubId+" getClubDetail success"); 
		} 
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Get_Club_Data, e.getId());
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
	
	//搜索俱乐部
	public void searchClub(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			int clubId = msg.getInt();
			sem = getSemp(userId);
			sem.acquire();
			getExistUser(userId);// 校验用户是否存在
			logger.info("in searchClub, userId=" + userId + ",clubId=" + clubId);
			
			TaurusClub taurusClub = getClub(clubId);
			if (taurusClub == null)
			{
                throw new GameException(GameException.CLUB_IS_NOT_EXIST,"玩家:userId= "+userId+"查找俱乐部时，俱乐部clubId ="+clubId+"不存在！");
			}
			
			int iUserId = Integer.valueOf(userId);
			OutputMessage om = new OutputMessage(true);
			synchronized (taurusClub)
			{
				om.putInt(taurusClub.getClubId());
				om.putString(taurusClub.getClubName());
				om.putString(taurusClub.getClubIntroduce());
				om.putInt(ClubConfig.INSTANCE.getConfig().getPriClubPeopleMaxNum());
				om.putString(taurusClub.getIconUrl());
				if(taurusClub.getClubType() == 0) //私人俱乐部发送创建者消息，公共俱乐部没有创建者
				{
					int creatorId = taurusClub.getCreatorId();
					User creator = getExistUser(String.valueOf(creatorId));
					om.putString(String.valueOf(creator.getUserId()));
					om.putString(creator.getNickName());
					om.putString(creator.getHeadImgUrl());
				}
				else
				{
					om.putString("");
					om.putString("");
					om.putString("");
				}
				om.putString(taurusClub.getClubCity());
				om.putInt(taurusClub.getAllRoomCount());
				om.putInt(taurusClub.getOnlineCount());
				om.putInt(taurusClub.getTotalCount());
				om.putBoolean(taurusClub.ContainsUser(iUserId));
				om.putBoolean(taurusClub.getClubType() == 1); 
			}
			session.sendMessage(Protocol_Cli_Search_Club, om);
			logger.info("userId is "+userId+",clubId is "+clubId+" searchClub success"); 
		} 
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Search_Club, e.getId());
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
	
	//退出俱乐部
	public void actionExitClub(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			int clubId = msg.getInt();
			sem = getSemp(userId);
			sem.acquire();
			
			logger.info("in actionExitClub, userId=" + userId+",clubId="+clubId);
			User user = getExistUser(userId);// 校验用户是否存在
			int iUserId = Integer.valueOf(userId);
			//俱乐部是否存在
			TaurusClub taurusClub = getClub(clubId);
			if (taurusClub == null)
			{
                throw new GameException(GameException.CLUB_IS_NOT_EXIST,"玩家:userId= "+userId+"退出俱乐部时，俱乐部clubId ="+clubId+"不存在！");
			}
			if (!taurusClub.ContainsUser(iUserId))
			{
				throw new GameException(GameException.CLUB_USER_IS_NOT_MEMBER, "玩家:userId="+userId+"退出俱乐部时，不是俱乐部成员,不能退出");
			}
			//群主无法退出俱乐部
			if(taurusClub.IsCreator(iUserId))
			{
				throw new GameException(GameException.CLUB_CREATOR_CAN_NOT_EXIT,"玩家：userId="+userId+"退出俱乐部时，玩家是群主不可退出俱乐部！");
			}
			
			/*
			 * 1.玩家离开俱乐部生成的消息，通知本人
			 * 2.通知俱乐部创建者
			 */
			String memberContent = "您已退出"+taurusClub.getClubName()+"("+clubId+")俱乐部";//1
			ClubMessage msgMember = new ClubMessage(clubId, iUserId, memberContent);//1
			String creatorContent = user.getNickName()+"("+user.getUserId()+")"+"已经退出"+taurusClub.getClubName()+"("+clubId+")俱乐部";//2
			ClubMessage msgCreator = new ClubMessage(clubId, taurusClub.getCreatorId(), creatorContent);//2
			clubCommonAction().MemberExitClub(user.getUserId(), clubId, msgMember, msgCreator);
			
			//俱乐部相关操作
			synchronized (taurusClub)
			{
				TaurusClubMember member = taurusClub.getMember(iUserId);
				taurusClub.removeMember(iUserId);//移除群成员
				taurusClub.subOneTotalCount();//俱乐部总人数减一
				if (member!=null && member.isOnline())
				{
					taurusClub.removeUserSession(userId);//移除玩家session
					taurusClub.subOneOnlineCount();//在线人数减一
				}
			}
			OutputMessage om = new OutputMessage(true);
			om.putInt(clubId);
			session.sendMessage(Protocol_Cli_Action_Exit_Club, om);
			//通知退出者有新消息
			OutputMessage emptyOm = new OutputMessage(true);
			session.sendMessage(Protocol_Ser_New_Msg, emptyOm);
			
			GameSession creatorSession = getUserSession(String.valueOf(taurusClub.getCreatorId()));
			if (creatorSession != null)
			{
				//如果创建者在线，通知创建者有新消息
				creatorSession.sendMessage(Protocol_Ser_New_Msg, emptyOm);
			}
			logger.info("userId is "+userId+",clubId is "+clubId+"exitClub success!");
		} 
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Action_Exit_Club, e.getId());
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
	

	//获取俱乐部玩家列表信息
	public void getClubUserList(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			int clubId = msg.getInt();
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in getClubUserList, userId=" + userId + ",clubId=" + clubId);
			getExistUser(userId);// 校验用户是否存在
			TaurusClub taurusClub = getClub(clubId);
			if (taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST, "拉取俱乐部成员信息时，找不到对应的俱乐部信息");
			}
			int iUserId = Integer.valueOf(userId);
			if (!taurusClub.ContainsUser(iUserId))
			{
				throw new GameException(GameException.CLUB_USER_IS_NOT_MEMBER, "拉取俱乐部成员信息时，不是俱乐部成员，禁止拉取");
			}
			
			List<TaurusClubMember> members = taurusClub.getAllMember();
			OutputMessage om = new OutputMessage(true);
			synchronized (taurusClub)
			{
				int size = members.size(); 
				om.putInt(size);
				String tempUserId = null;
				User tempUser = null;
				TaurusClubMember member = null;
				for (int i=0; i<size; ++i)
				{
					member = members.get(i);
					tempUserId = String.valueOf(members.get(i).getUserId());
					tempUser = getExistUser(tempUserId);
					om.putString(tempUserId);
					om.putString(tempUser.getNickName());
					om.putString(tempUser.getHeadImgUrl());
					om.putByte(member.getPosition());
					om.putInt(member.getScoreLimit());
					om.putInt(member.getCurrentScore());
				}
			}
			session.sendMessage(Protocol_Cli_Get_User_List, om);
			logger.info("userId is "+userId+",clubId is "+clubId+" getClubUserList success");
		} 
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Get_User_List, e.getId());
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

	
	//俱乐部创建者 搜索用户 为邀请用户加入做准备
	public void searchUser(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			String searchUserId = msg.getUTF();
			int clubId = msg.getInt();
			logger.info("in searchUser, userId="+userId+",searchUserId="+searchUserId+",clubId="+clubId);
			sem = getSemp(userId);
			sem.acquire();
			getExistUser(userId);// 校验用户是否存在
			User searchUser = getExistUser(searchUserId);// 校验用户是否存在
			TaurusClub taurusClub = getClub(clubId);
			if (taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST, "俱乐部创建者搜索玩家时，找不到对应的俱乐部信息");
			}
			int iUserId = Integer.valueOf(userId);
			if (!taurusClub.IsCreator(iUserId))
			{
				throw new GameException(GameException.CLUB_USER_IS_NOT_CLUB_CREATOR, "俱乐部创建者搜索玩家时，不是俱乐部创建者，不能进行搜索操作");
			}
			
			OutputMessage om = new OutputMessage(true);
			om.putString(String.valueOf(searchUser.getUserId()));
			om.putString(searchUser.getNickName());
			om.putString(searchUser.getHeadImgUrl());
			om.putBoolean(taurusClub.ContainsUser(Integer.valueOf(searchUserId)));
			session.sendMessage(Protocol_Cli_Serch_User, om);
			logger.info("userId is "+userId+",clubId is "+clubId+"searchUser success");
		} 
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Serch_User, e.getId());
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
	
	//俱乐部管理者邀请玩家加入俱乐部
	public void actionInviteJoinClub(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			String inviteId = msg.getUTF();
			
			sem = getSemp(userId);
			sem.acquire();
			int clubId = msg.getInt();
			int iUserId = Integer.valueOf(userId);
			int iInviteId = Integer.valueOf(inviteId);
			logger.info("in actionInviteJoinClub, userId="+userId+"，inviteId="+inviteId+",clubId="+clubId);
			User user = getExistUser(userId);// 校验用户是否存在
			User inviteUser = getExistUser(inviteId);// 校验用户是否存在
			//权限校验
			TaurusClub taurusClub = getClub(clubId);
			if (taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST, "俱乐部创建者邀请玩家加入时，找不到对应的俱乐部信息");
			}
			if (!taurusClub.IsCreator(iUserId))
			{
				throw new GameException(GameException.CLUB_USER_IS_NOT_CLUB_CREATOR, "俱乐部创建者邀请玩家加入时，不是俱乐部创建者，不能做出审批操作");
			}
			if (taurusClub.ContainsUser(iInviteId))
			{
				throw new GameException(GameException.CLUB_USER_ALREADY_JOIN_CLUB, "俱乐部创建者邀请玩家加入时，该玩家已经加入该俱乐部，不能重复加入");
			}
			//检测俱乐部人数是否达到上限
			reachPriClubPeopleNumLimit(clubId);
			//被邀请者的俱乐部达到上限，不能被邀请
			reachClubCountLimit(inviteUser);
			//----------------------校验通过，下面进行流程处理----------------
			ClubMember member = new ClubMember(clubId, iInviteId);
			
			String inviteContent = user.getNickName()+"("+userId+")已成功邀请加入"+taurusClub.getClubName()+"("+clubId+")俱乐部";
			ClubMessage msgInvite = new ClubMessage(clubId, iInviteId, inviteContent);
			String creatorContent = "您已成功邀请" + inviteUser.getNickName()+"("+inviteId+")加入"+taurusClub.getClubName()+"("+clubId+")俱乐部";
			ClubMessage msgCreator = new ClubMessage(clubId, iUserId, creatorContent);
			
			clubCommonAction().CreatorInviteUserJoinClub(iUserId, iInviteId, clubId, msgInvite, msgCreator, member);
			synchronized (taurusClub)
			{
				TaurusClubMember taurusMember = new TaurusClubMember(member);
				taurusClub.addMember(taurusMember);//增加群成员
				taurusClub.addOneTotalCount();//俱乐部总人数加一
				
			}
			OutputMessage om = new OutputMessage(true);
			om.putInt(clubId);
			om.putString(inviteId);
			om.putString(inviteUser.getNickName());
			om.putString(inviteUser.getHeadImgUrl());
			om.putByte(ClubPosition.POSITION_MEMBER.value);
			om.putInt(member.getDiamondLimit());
			om.putInt(member.getCostDiamond());
			session.sendMessage(Protocol_Cli_Action_Invite_Join_Club, om);
			
			//推送有新消息给俱乐部创建者 
			OutputMessage emptyOm = new OutputMessage(true);
			session.sendMessage(Protocol_Ser_New_Msg, emptyOm);
			
			GameSession inviteSession = getUserSession(inviteId);
			if (inviteSession != null)
			{//如果被邀请加入俱乐部的玩家在线，则推送有新消息给该玩家
				inviteSession.sendMessage(Protocol_Ser_New_Msg, emptyOm);
				//将俱乐部信息发送给玩家
				OutputMessage inviteOm = new OutputMessage(true);
				inviteOm.putInt(taurusClub.getClubId());
				inviteOm.putString(taurusClub.getClubName());
				inviteOm.putString(taurusClub.getClubIntroduce());
				inviteOm.putInt(ClubConfig.INSTANCE.getConfig().getPriClubPeopleMaxNum());
				inviteOm.putString(taurusClub.getIconUrl());
				inviteOm.putString(String.valueOf(taurusClub.getCreatorId()));
				inviteOm.putString(user.getNickName());
				inviteOm.putString(user.getHeadImgUrl());
				inviteOm.putString(taurusClub.getClubCity());
				inviteOm.putInt(taurusClub.getAllRoomCount());
				inviteOm.putInt(taurusClub.getOnlineCount());
				inviteOm.putInt(taurusClub.getTotalCount());
				inviteSession.sendMessage(Protocol_Ser_Action_Invite_Join_Club, inviteOm);
			}
			logger.info("userId is "+userId+", inviteId is "+inviteId+",clubId is "+clubId+" inviteUserJoinClub success"); 
		} 
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Action_Invite_Join_Club, e.getId());
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
	
	
	
	//俱乐部创建者对 加入俱乐部申请作出回应   通过or拒绝
	public void actionHandleApply(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			int iUserId = Integer.valueOf(userId);
			int msgId = msg.getInt();
			String applyId = msg.getUTF();
			int clubId = msg.getInt();
			boolean isAgree = msg.getBoolean();
			sem = getSemp(userId);
			sem.acquire();
			//用户存在性校验
			User user = getExistUser(userId);
			User applyUser = getExistUser(applyId);
			logger.info("in actionHandleApply,userId=" + userId + ",msgId=" + msgId + ",applyId=" + applyId +",clubId=" + clubId + ",isAgree=" + isAgree);
			
			//权限校验
			TaurusClub taurusClub = getClub(clubId);
			if (taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST, "俱乐部创建者审批加入申请时，找不到对应的俱乐部信息");
			}
			if (!taurusClub.IsCreator(iUserId))
			{
				throw new GameException(GameException.CLUB_USER_IS_NOT_CLUB_CREATOR, "俱乐部创建者审批加入申请时，不是俱乐部创建者，不能做出审批操作");
			}
			
			int iApplyId = Integer.valueOf(applyId);
			if (taurusClub.ContainsUser(iApplyId))
			{
				throw new GameException(GameException.CLUB_USER_ALREADY_JOIN_CLUB, "俱乐部创建者审批加入申请时，申请加入俱乐部玩家已经加入该俱乐部，不能再次审批");
			}
			//----------------------校验通过，下面进行流程处理----------------
			ClubMember member = new ClubMember(clubId, iApplyId);
			
			ClubMessage msgApply = null, msgCreator = null;
			if (isAgree)
			{   
				/*
				 * 1.每次群主同意玩家的申请时，判断俱乐部人数是否达到上限
				 * 2.如果人数达到上限，群主对已有的申请同意时，群主弹框提示人数已满，该条申请消息保留
				 * (群主可能会删除不活跃用户后再同意申请)，不用生成消息告诉申请者和群主，该次操作不用生成操作记录
				 */
				reachPriClubPeopleNumLimit(clubId);
				String applyContent = user.getNickName()+"("+userId+")已经通过您加入"+taurusClub.getClubName()+"("+clubId+")俱乐部的申请";
				msgApply = new ClubMessage(clubId, iApplyId, applyContent);
				String creatorContent = "您已通过" + applyUser.getNickName()+"("+applyId+")加入"+taurusClub.getClubName()+"("+clubId+")俱乐部的申请";
				msgCreator = new ClubMessage(clubId, iUserId, creatorContent);
				clubCommonAction().CreatorAgreeApplyJoinClub(msgId,iUserId, clubId, iApplyId, msgApply, msgCreator, member);
			}
			else
			{
				String applyContent = user.getNickName()+"("+userId+")已经拒绝您加入"+taurusClub.getClubName()+"("+clubId+")俱乐部的申请";
				msgApply = new ClubMessage(clubId, iApplyId, applyContent);
				String creatorContent = "您已拒绝" + applyUser.getNickName()+"("+applyId+")加入"+taurusClub.getClubName()+"("+clubId+")俱乐部的申请";
				msgCreator = new ClubMessage(clubId, iUserId, creatorContent);
				clubCommonAction().CreatorRefuseApplyJoinClub(msgId,iUserId, clubId, iApplyId, msgApply, msgCreator);
			}
			
			synchronized (taurusClub)
			{
				if (isAgree)
				{
					TaurusClubMember taurusMember = new TaurusClubMember(member);
					taurusClub.addMember(taurusMember);//增加群成员
					taurusClub.addOneTotalCount();//俱乐部总人数加一
				}
				else
				{//拒绝了申请，俱乐部相关不变动
				}
			}
			
			OutputMessage emptyOm = new OutputMessage(true);
			session.sendMessage(Protocol_Cli_Action_Handle_Apply, emptyOm);
			
			//推送有新消息给俱乐部创建者 
			session.sendMessage(Protocol_Ser_New_Msg, emptyOm);
			
			GameSession applySession = getUserSession(applyId);
			if (applySession != null)
			{//如果申请加入俱乐部的玩家在线，则推送有新消息给该玩家
				applySession.sendMessage(Protocol_Ser_New_Msg, emptyOm);
				OutputMessage applyOm = new OutputMessage(true);
				applyOm.putBoolean(isAgree);
				if (isAgree)
				{
					applyOm.putInt(taurusClub.getClubId());
					applyOm.putString(taurusClub.getClubName());
					applyOm.putString(taurusClub.getClubIntroduce());
					applyOm.putInt(ClubConfig.INSTANCE.getConfig().getPriClubPeopleMaxNum());
					applyOm.putString(taurusClub.getIconUrl());
					applyOm.putString(String.valueOf(taurusClub.getCreatorId()));
					applyOm.putString(user.getNickName());
					applyOm.putString(user.getHeadImgUrl());
					applyOm.putString(taurusClub.getClubCity());
					applyOm.putInt(taurusClub.getAllRoomCount());
					applyOm.putInt(taurusClub.getOnlineCount());
					applyOm.putInt(taurusClub.getTotalCount());
				}
				applySession.sendMessage(Protocol_Ser_Action_Handle_Apply, applyOm);
			}
			logger.info("actionHandleApply success ,userId=" + userId + ",msgId=" + msgId + ",applyId=" + applyId +",clubId=" + clubId + ",isAgree=" + isAgree);
		} 
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Action_Handle_Apply, e.getId());
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
	
	//俱乐部管理者踢出俱乐部成员
	public void actionKickMember(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String creatorId = msg.getUTF();
			int iCreatorId = Integer.valueOf(creatorId);
			String kickedId = msg.getUTF();
			int iKickedId = Integer.valueOf(kickedId);
			int clubId = msg.getInt();
			//校验完成，进行踢出操作
			sem = getSemp(creatorId);
			sem.acquire();
			logger.info("in actionKickMember, creatorId="+creatorId+",kickedId="+kickedId+",clubId="+clubId);
			User creator = getExistUser(creatorId);// 校验用户是否存在
			User kickedUser = getExistUser(kickedId);// 校验用户是否存在
			
			TaurusClub taurusClub = getClub(clubId);
			if (taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST, "俱乐部创建者踢出成员时，找不到对应的俱乐部信息");
			}
			if (!taurusClub.IsCreator(iCreatorId))
			{
				throw new GameException(GameException.CLUB_USER_IS_NOT_CLUB_CREATOR, "俱乐部创建者踢出成员时，不是俱乐部创建者，不能踢出其他成员");
			}
			if (!taurusClub.ContainsUser(iKickedId))
			{
				throw new GameException(GameException.CLUB_USER_IS_NOT_MEMBER, "俱乐部创建者踢出成员时，被踢出者不是俱乐部成员，不能被踢出");
			}
			//被踢出玩家在该俱乐部已经创建房间或者在已经进入牌局，则不能被踢出
			if(taurusClub.isRoomOwner(iKickedId))
			{
				throw new GameException(GameException.CLUB_MEMBER_ALREADY_CREATE_ROOM,"俱乐部群主踢出成员时，该成员已创建房间不可踢出,kickedId is "+kickedId);
			}
			if(taurusClub.bindCurrentClubTable(kickedId))
			{
				throw new GameException(GameException.USER_SIT_CURRENT_CLUB_TABLE,"俱乐部群主踢出成员时，该成员已经在俱乐部房间中坐下，不可踢出，kickedId"+kickedId);
			}
			
			String kickContent = "您已被"+creator.getNickName()+"("+creatorId+")踢出"+taurusClub.getClubName()+"("+clubId+")俱乐部";
			ClubMessage msgKicked = new ClubMessage(clubId, iKickedId, kickContent);
			String creatorContent = "您在"+taurusClub.getClubName()+"("+clubId+")俱乐部已成功踢出"+kickedUser.getNickName()+"("+kickedId+")";
			ClubMessage msgCreator = new ClubMessage(clubId, iCreatorId, creatorContent);
			
			clubCommonAction().CreatorKickClubMember(iCreatorId, clubId, iKickedId, msgKicked, msgCreator);
			synchronized (taurusClub)
			{
				TaurusClubMember member = taurusClub.getMember(iKickedId);
				taurusClub.removeMember(iKickedId);//移除群成员
				taurusClub.subOneTotalCount();//俱乐部总人数减一
				if (member!=null && member.isOnline())
				{
					taurusClub.subOneOnlineCount();//在线人数减一
				}
				//如果玩家正好处在当前俱乐部内部，则需要清理和俱乐部的绑定关系
				TaurusClub club = getUserClub(kickedId);
				if(club != null && club.getClubId() == clubId)
				{   
					//在当前俱乐部被踢出则收到被踢出通知，如果在不在当前俱乐部内收到新消息即可
					GameSession session2 = taurusClub.getUserSession(iKickedId);
					if(session2 != null)
					{
						OutputMessage kickOm = new OutputMessage(true);
						kickOm.putInt(clubId);
						session2.sendMessage(Protocol_Ser_Action_Kick_Member, kickOm);
					}
					taurusClub.removeUserSession(kickedId);//移除玩家session
					unbindUserClub(kickedId);
				}
				//进入了当前俱乐部的房间，没有坐下
				TaurusRoom room = getUserRoom(kickedId);
				if(room != null && taurusClub.getRoom(room.getRoomId()) != null)
				{   
					room.removeSession(kickedId);
					unbindUserRoom(kickedId);
				}
			}
			OutputMessage om = new OutputMessage(true);
			om.putString(kickedId);
			om.putInt(clubId);
			session.sendMessage(Protocol_Cli_Action_Kick_Member, om);
			//推送有新消息给操作者-俱乐部创建者
			OutputMessage emptyOm = new OutputMessage(true);
			session.sendMessage(Protocol_Ser_New_Msg, emptyOm);
			
			GameSession kickSession = getUserSession(kickedId);
			if (kickSession != null)
			{//如果被踢出玩家在线，则推送有新消息给被踢出者
				kickSession.sendMessage(Protocol_Ser_New_Msg, emptyOm);
			
			}
			logger.info(" actionKickMember success, kickedId="+creatorId+",kickedId="+kickedId+",clubId="+clubId);
		} 
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Action_Kick_Member, e.getId());
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
	
	//俱乐部管理者设置俱乐部配置信息
	public void actionSetClubConfigInfo(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String creatorId = msg.getUTF();
			int clubId = msg.getInt();
			String clubName = msg.getUTF();
			String clubIntroduce = msg.getUTF();
			String clubIconUrl = msg.getUTF();
			getExistUser(creatorId);// 校验用户是否存在
			logger.info("in actionSetClubConfigInfo, userId="+creatorId+",clubId="+clubId+",clubName="+clubName+",clubIntroduce="+clubIntroduce+",clubIConUrl="+clubIconUrl);
			
			String regex = "^[\u4e00-\u9fa5a-zA-Z0-9]{2,7}$";
			Pattern p = Pattern.compile(regex);
			Matcher matcher = p.matcher(clubName);
			boolean flag = matcher.matches();
			if(!flag)
			{
				throw new GameException(GameException.NICK_NAME_IS_NOT_STANDARD,"玩家：userId = "+creatorId+"设置俱乐部信息,俱乐部昵称不合乎规范！");
			}
			if(clubIntroduce.trim().length() > 50) //俱乐部字数不超过50字
			{
				throw new GameException(GameException.CLUB_INTRODUCE_TOO_LONG,"玩家：userId = "+creatorId+"设置俱乐部信息,俱乐部介绍字数过多！");
			}
			
			TaurusClub taurusClub = getClub(clubId);
			if (taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST, "玩家设置俱乐部信息时，找不到对应的俱乐部信息");
			}
			if (!taurusClub.IsCreator(Integer.valueOf(creatorId)))
			{
				throw new GameException(GameException.CLUB_USER_IS_NOT_CLUB_CREATOR, "玩家设置俱乐部信息时，不是俱乐部创建者，不能设置");
			}
			//校验通过 ，下面进行具体设置
			sem = getSemp(creatorId);
			sem.acquire();
			clubCommonAction().CreatorSetClubConfigInfo(clubId, clubName, clubIntroduce, clubIconUrl);
			synchronized (taurusClub)
			{
				taurusClub.setClubName(clubName);
				taurusClub.setClubIntroduce(clubIntroduce);
				taurusClub.setIconUrl(clubIconUrl);
			}
			OutputMessage om = new OutputMessage(true);
			om.putInt(clubId);
			om.putString(clubName);
			om.putString(clubIntroduce);
			om.putString(clubIconUrl);
			session.sendMessage(Protocol_Cli_Action_Set_Club_ConfigInfo,om);
			logger.info("actionSetClubConfigInfo  success, userId="+creatorId+",clubId="+clubId+",clubName="+clubName+",clubIntroduce="+clubIntroduce+",clubIConUrl="+clubIconUrl);
		} 
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Action_Set_Club_ConfigInfo, e.getId());
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
	
	//获取俱乐部玩家消息
	public void getClubUserMsg(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			getExistUser(userId);// 校验用户是否存在
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in getClubUserMsg, userId is " + userId);
			List<ClubMessage> list = clubMessageAction().getAllMessage(Integer.valueOf(userId));
			OutputMessage om = new OutputMessage(true);
			om.putInt(list.size());
			for (ClubMessage clubMsg:list)
			{
				om.putInt(clubMsg.getId());
				om.putString(clubMsg.getContent());
				om.putBoolean(clubMsg.getIsRead()==1);
				om.putString(String.valueOf(clubMsg.getCreateTime().getTime()));
				om.putByte(clubMsg.getType());
				if (clubMsg.getType() == 1)
				{
					String applyId = String.valueOf(clubMsg.getApplyId());
					User user = userAction().getExistUser(applyId);
					om.putString(applyId);
					om.putString(user.getNickName());
					om.putInt(clubMsg.getClubId());
				}
			}
			session.sendMessage(Protocol_Cli_Get_Club_User_Msg, om);
			logger.info("getClubUserMsg success, userId is " + userId);
		} 
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Get_Club_User_Msg, e.getId());
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
	//读取俱乐部消息
	public void readClubUserMsg(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			int msgId = msg.getInt();
			getExistUser(userId);// 校验用户是否存在
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in readClubUserMsg, userId is " + userId);
			ClubMessage clubMsg = clubMessageAction().getMessage(msgId);
			if(clubMsg == null)
			{
				throw new GameException(GameException.CLUB_MESSAGE_NOT_EXIST,"玩家：userId is "+userId+"在读取消息msgId is "+msgId+"时，消息不存在");
			}
			int iUserId = Integer.valueOf(userId);
			if (clubMsg.getUserId() == iUserId)
			{
				clubMessageAction().readMessage(msgId);
			}
			session.sendMessage(Protocol_Cli_Read_Club_User_Msg, new OutputMessage(true));
			logger.info("readClubUserMsg success, userId is " + userId);
		} 
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Read_Club_User_Msg, e.getId());
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
	
	public void checkUserUnreadMsg(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		GameSession session = request.getSession();
		Semaphore sem = null;
		try
		{
			String userId = msg.getUTF();
			int iUserId = Integer.parseInt(userId);
			getExistUser(userId);// 校验用户是否存在
			sem = getSemp(userId);
			sem.acquire();
			logger.info("in checkUserUnreadMsg, userId is " + userId);
			List<ClubMessage> msgList = clubMessageAction().getUserUnreadMsg(iUserId);
			OutputMessage om = new OutputMessage(true);
			if(msgList.isEmpty())
			{
				om.putBoolean(false);
			}
			else
			{
				om.putBoolean(true);
			}
			session.sendMessage(Protocol_Cli_Check_User_New_Msg, om);
			logger.info("checkUserUnreadMsg success, userId is " + userId);
		} 
		catch (GameException e)
		{
			sendError(response, Protocol_Cli_Check_User_New_Msg, e.getId());
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
	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * Club end * * *  * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  * * * * * * * * * * * * * * * * * * * *
	 * */
}
