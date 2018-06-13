package com.linyun.game.taurus.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.linyun.bottom.cached.RedisResource;
import com.linyun.bottom.common.exception.GameException;
import com.linyun.bottom.handler.SocketRequest;
import com.linyun.bottom.handler.SocketResponse;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.util.InputMessage;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.common.entity.CustomService;
import com.linyun.common.entity.Marquee;
import com.linyun.common.entity.Notice;
import com.linyun.common.entity.PrivateRoom;
import com.linyun.common.entity.TaurusLog;
import com.linyun.common.entity.TaurusRoundLog;
import com.linyun.common.entity.User;
import com.linyun.common.taurus.eum.BankerMode;
import com.linyun.common.taurus.eum.PlayerInjection;
import com.linyun.common.taurus.eum.RoomPayMode;
import com.linyun.common.taurus.eum.UpBankerScore;
import com.linyun.middle.common.taurus.service.BaseServer;
import com.linyun.middle.common.taurus.utils.DiamondUtils;

/**
 * @Author walker
 * @Since 2018年5月18日
 **/

public class LobbyServer extends BaseServer {
	private static final Logger logger = LoggerFactory.getLogger(LobbyServer.class);

	private static final String SERVER_VERSION = "0.3.1_20180518_1104";
	private static final int PLAYER_NUM = 6; // 每局游戏中玩家的最多人数

	/******* 整合大厅新增协议 **********/

	/** 绑定邀请码 */
	public static final short PROTOCOL_Cli_BindInviteCode = 6002;
	/** 拉取总体战绩(第一级) */
	public static final short PROTOCOL_Cli_GetTotalRecord = 6003;
	/** 拉取局数战绩(第二级) */
	public static final short PROTOCOL_Cli_GetRoundRecord = 6004;
	/** 拉取战绩详情 */
	public static final short PROTOCOL_Cli_GetDetailRecord = 6005;
	/** 拉取消息公告 */
	public static final short PROTOCOL_Cli_GetMsg = 6006;
	/** 拉取跑马灯信息 */
	public static final short PROTOCOL_Cli_GetBroadCast = 6007;
	/** 获取房间列表 */
	public static final short PROTOCOL_Cli_GetRoomList = 6008;
	/** 创建房间 **/
	public static final short PROTOCOL_Cli_CreateRoom = 6009;
	/** 加入房间 **/
	public static final short PROTOCOL_Cli_JoinRoom = 6010;
	/** 刷新用户信息 **/
	public static final short PROTOCOL_Cli_RefreshUserInfo = 6011;
	/** 获取联系方式信息 */
	public static final short PROTOCOL_Cli_GetContactInfo = 6012;
	/** 获取服务器版本信息 */
	public static final short PROTOCOL_Cli_GetServerVersion = 6013;
	/** 收到房间解散消息后 **/
	public static final short PROTOCOL_Cli_ReceiveDissolutionMsg = 6014;
	/** 验证登录信息，看重连期间是否已登录，是则不重连 **/
	public static final short PROTOCOL_Cli_VerifyLoginInfo = 6015;

	/*********************************************************************************************/
	/************************************** 整合大厅 ************************************************/
	/*********************************************************************************************/

	// 游戏内断线重连，如果此时该账号有从其他设备上登录，则重连终止
	public void verifyLoginInfo(SocketRequest request, SocketResponse response)
	{
		try {
			InputMessage im = request.getInputMessage();
			String userId = im.getUTF();
			String sessionId = im.getUTF();

			filterSessionId(userId, sessionId);
			OutputMessage om = new OutputMessage(true);
			response.sendMessage(PROTOCOL_Cli_VerifyLoginInfo, om);
			
		} catch (GameException e) 
		{
			sendError(response, PROTOCOL_Cli_VerifyLoginInfo, e.getId());
			logger.error(e.getMessage(),e);
		}
	}

	public void getSerVersion(SocketRequest request, SocketResponse response) 
	{
		try {
			OutputMessage om = new OutputMessage(true);
			om.putString(SERVER_VERSION);
			response.sendMessage(PROTOCOL_Cli_GetServerVersion, om);
		} catch (GameException e) 
		{
			sendError(response, PROTOCOL_Cli_GetServerVersion, e.getId());
		
		} catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	/*
	 * 单点登录，同一个userId进行两次以上的登录，以最后一次登录的用户为准，剔除前面的用户
	 */
	public void filterSessionId(String userId, String sessionId) throws GameException {
		/*
		 * 1.从缓存中或者数据库中拉取玩家，此时的玩家的sessionId是用这个userId最后一次登录生成的sessionId
		 * 2.如有用户发起操作，其中所带的sessionId与从缓存中拿出来的最新的sessionId不等，则该设备上的玩家剔除
		 */
		User user = userAction().getExistUser(userId);
		if (!sessionId.equals(user.getSessionId())) {
			throw new GameException(GameException.USER_LOGIN_BY_OTHERWAY, "玩家userId=" + userId + "已在其他设备上登录！");
		}
	}


	/**
	 * 
	 * 绑定邀请码
	 */
	public void bindInviteCode(SocketRequest request, SocketResponse response) 
	{
		try {
			InputMessage im = request.getInputMessage();
			String userId = im.getUTF();
			String inviteCode = im.getUTF();
			String sessionId = im.getUTF();

			filterSessionId(userId, sessionId);
			logger.info("userId:" + userId + "bindInviteCode");
			User u = userAction().getExistUser(userId);// 检测是否存在
			if (u.getInviteCode() != 0) 
			{
				throw new GameException(GameException.REFCODE_IS_ALREADY_EXIST,
						"绑定邀请码时，玩家userId=" + userId + "的邀请码已经存在，不可重复绑定！");
			}
			// 绑定邀请码时，邀请码不能为空字符串
			if (inviteCode.trim().isEmpty()) 
			{
				throw new GameException(GameException.INVITECODE_IS_NULL, "绑定邀请码时，玩家：" + userId + "没有填写邀请码！");
			}
			// 判断填写的邀请码是否存在
			Set<Integer> inviteCodeSet = commonAction().getInviteCodeList();
			if (inviteCodeSet == null || inviteCodeSet.isEmpty()) 
			{
				throw new GameException(GameException.INVITECODE_LIST_LENGTH_IS_ZERO,
						"绑定邀请码时，玩家:" + userId + "拉取的邀请码集合为null!");
			}

			String pattern = "\\d{5}";
			boolean isMatch = Pattern.matches(pattern, inviteCode);
			if (!isMatch) 
			{
				throw new GameException(GameException.INVITECODE_REGULAR_ERROR,
						"绑定邀请码时，玩家:" + userId + "输入的邀请码不符合规则!(只能输入数字)");
			}

			// 玩家填写的邀请码，在邀请码的配置信息找不到
			if (!inviteCodeSet.contains(Integer.valueOf(inviteCode))) 
			{
				throw new GameException(GameException.BIND_INVITECODE_IS_NOT_EXIST, "绑定邀请码时，玩家:" + userId + "填写的邀请码有误");
			}

			if (u.getUserId() == Integer.valueOf(inviteCode)) 
			{
				throw new GameException(GameException.refCode_Is_Myself_InvitationCode,
						"绑定邀请码时，玩家:" + userId + "填写的邀请码不能是自己");
			}

			// 更新玩家的邀请码到数据库,用户绑定邀请码成功
			userAction().bindUserInviteCode(userId, Integer.valueOf(inviteCode));

			logger.info("玩家:userId=" + userId + "绑定邀请码" + inviteCode + "成功!");
			
			response.sendMessage(PROTOCOL_Cli_BindInviteCode, new OutputMessage(true));
			
		} catch (GameException e) 
		{
			sendError(response, PROTOCOL_Cli_BindInviteCode, e.getId());
			
		} catch (Exception e) 
		{
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * 拉取最近战绩(玩家近3天参与过的游戏的记录)
	 */
	public void getTotalRecord(SocketRequest request, SocketResponse response) throws GameException 
	{
		try {
			InputMessage im = request.getInputMessage();
			String userId = im.getUTF();
			String sessionId = im.getUTF();

			filterSessionId(userId, sessionId);
			logger.info("玩家：" + userId + "拉取最近三天的所有战绩！");
			// 以玩家的userId作为游戏记录t_game_log里面的6个玩家之一，来进行记录查询
			userAction().getExistUser(userId);// 检测玩家是否存在
			List<TaurusLog> userGameRecardList = taurusLogAction().getUserGameRecard(userId);
			
			// 玩家有游戏记录
			OutputMessage om =new OutputMessage(true);
			om.putInt(userGameRecardList.size());
			for(TaurusLog userGameLog : userGameRecardList)
			{
				om.putString(String.valueOf(userGameLog.getRoomNum()));
				om.putInt(userGameLog.getRoundTotal());
				if (BankerMode.ValueOf(userGameLog.getBankerMode()) == BankerMode.BANKER_MODE_ALL_COMPARE) // 如果是通比牛牛模式  单独的底分设置
                {
					om.putInt(userGameLog.getAllCompareBaseScore());// 发给前端 1 2 3 对应 1 2 4,通比牛牛没有庄家选择
                }else
                {
                	om.putInt(userGameLog.getBaseScore()); // 1， 2， 3 分别对应底分 1/2 ，2/4， 4/8 ， 4--庄家选择
                }
				om.putInt(userGameLog.getBankerMode()); //上庄模式
				String time = String.valueOf(userGameLog.getUpdateTime().getTime());// 游戏结束时间 时间戳
				om.putString(time);
				om.putString(String.valueOf(userGameLog.getRoomOwnerId()));
				
				for(int i=0;i<PLAYER_NUM; ++i)
				{
					String playerId = userGameLog.getPlayerIdByIndex(i+1);
					if(playerId != null && !playerId.isEmpty() && playerId.equals(userId))
					{
						om.putString(playerId);
						om.putString(userGameLog.getNickNameByIndex(i+1));
						om.putString(userGameLog.getFaceUrlByIndex(i+1));
						om.putInt(userGameLog.getScoreByIndex(i+1));
					}
				}
				
				for(int i=0;i<PLAYER_NUM; ++i)
				{
					String playerId = userGameLog.getPlayerIdByIndex(i+1);
					if(playerId != null && !playerId.isEmpty())
					{
						if(!playerId.equals(userId))
						{
							om.putString(playerId);
							om.putString(userGameLog.getNickNameByIndex(i+1));
							om.putString(userGameLog.getFaceUrlByIndex(i+1));
							om.putInt(userGameLog.getScoreByIndex(i+1));
						}else
						{
							
						}
						
					}else
					{
						om.putString("");
						om.putString("");
						om.putString("");
						om.putInt(0);
					}
				}
				
				om.putInt(userGameLog.getClubId());
				
			}
			
			response.sendMessage(PROTOCOL_Cli_GetTotalRecord, om);
			
			logger.info("玩家userId=" + userId + "获取总战绩成功!");

		} catch (GameException e) 
		{
			sendError(response, PROTOCOL_Cli_GetTotalRecord, e.getId());
			
		} catch (Exception e) 
		{
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 拉取战绩局数详情(一级详情)
	 */
	public void getRoundRecord(SocketRequest request, SocketResponse response) 
	{
		try {
			InputMessage im = request.getInputMessage();
			String userId = im.getUTF();
			String roomId = im.getUTF();
			String sessionId = im.getUTF();
			int index = im.getInt();

			filterSessionId(userId, sessionId);
			logger.info("玩家userId=：" + userId + "拉取局数的战绩详情！");
			userAction().getExistUser(userId); // 检测玩家是否存在
			/*
			 * 1.拉去10 or 20 局数的详情列表
			 * 2.创建房间时，以随机生成的roomNum为索引，在战绩总表中查询（3天内），以该房间号生成的记录是否存在，如果存在则重新生成房间号
			 */
			List<TaurusRoundLog> handCardList = taurusRoundLogAction().getUserEveryRoundHandCardLog(Integer.valueOf(roomId));
			if (handCardList.size() == 0) 
			{
				throw new GameException(GameException.GET_ROUND_LIST_IS_NULL,
						"玩家userId=:" + userId + "拉取一级详情的时候，拉取的集合为空");
			}
            OutputMessage om = new OutputMessage(true);
            om.putInt(handCardList.size());
            for(TaurusRoundLog roundLog : handCardList)
            {
            	om.putInt(roundLog.getRound());
            	om.putString(roundLog.getRoundIndex());
            	
            	for(int i=0; i< PLAYER_NUM ; ++i)
            	{
            		String playerId = roundLog.getGetPlayerIdx(i+1);
            		if(playerId != null && !playerId.isEmpty())
            		{
            			om.putString(playerId);
            			om.putInt(roundLog.getGetScorex(i+1));
            		}else
            		{
            			om.putString("");
            			om.putInt(0);
            		}
            	}
            	
            }
		   om.putInt(index);
           response.sendMessage(PROTOCOL_Cli_GetRoundRecord, om);
           
		} catch (GameException e) 
		{
			sendError(response, PROTOCOL_Cli_GetRoundRecord, e.getId());
			
		} catch (Exception e)
		{
			logger.error(e.getMessage(), e);
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
			int index = im.getInt();

			filterSessionId(userId, sessionId);
			logger.info("玩家userId=：" + userId + "拉取手牌的战绩详情！索引roomIndex=" + roundIndex);
			userAction().getExistUser(userId);// 检测玩家是否存在

			OutputMessage om = new OutputMessage(true);
			
			TaurusRoundLog userHandCardLog = taurusRoundLogAction().getHandCardByRoomIndex(roundIndex);
			if (userHandCardLog == null) 
			{
				throw new GameException(GameException.GET_HAND_CARD_LOG_IS_NULL,
						"玩家userId:" + userId + "拉取二级详情时，拉取的userHandCardLog对象为空");
			}

			om.putString(userHandCardLog.getBankerId());// 庄家的id

			// 通过房间号得到userGameLog对象，在得到上庄模式的类型
			TaurusLog userGameLog = taurusLogAction().getUserGameLogByRoomNum(userHandCardLog.getRoomNum());
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
					om.putString(u==null ? "":u.getNickName());
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
					om.putInt(userHandCardLog.getBaseScorex(i+1)); // 位置1的玩家的押注底分;
					om.putInt(userHandCardLog.getGetScorex(i+1)); // 每个玩家当前局的得分;
					om.putInt(userHandCardLog.getCardTypex(i+1)); // 位置1玩家的牌的类型
					om.putString(userHandCardLog.getCardsx(i+1)); //手牌数组
					
				}else
				{
					om.putString("");
					om.putString("");
					om.putByte((byte)0);
					om.putByte((byte)0);
					om.putInt(0);
					om.putInt(0); // 位置1的玩家的押注底分;
					om.putInt(0); // 每个玩家当前局的得分;
					om.putInt(0); // 位置1玩家的牌的类型
					om.putString(""); //手牌数组
				}

			}
			om.putInt(index);
			logger.info("玩家userId=" + userId + "拉取手牌记录成功,该局的索引是" + roundIndex);
			response.sendMessage(PROTOCOL_Cli_GetDetailRecord, om);

		} catch (GameException e) 
		{
			sendError(response, PROTOCOL_Cli_GetDetailRecord, e.getId());
			
		} catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 拉取消息公告
	 */
	public void getMsg(SocketRequest request, SocketResponse response) 
	{
		try {
			InputMessage im = request.getInputMessage();
			String userId = im.getUTF();
			String sessionId = im.getUTF();
			logger.info("userId:" + userId + "拉取公告");
			// 拉取公告
			filterSessionId(userId, sessionId);
			userAction().getExistUser(userId); // 玩家是否存在
			Notice notice = commonAction().getNoticeContent();
			if (notice == null) 
			{
				throw new GameException(GameException.NOTICE_NO_EXITST, "玩家:" + userId + "拉取公告时，公告不存在！");
			}
			OutputMessage om = new OutputMessage(true);
			om.putString(notice.getContent());
 
			response.sendMessage(PROTOCOL_Cli_GetMsg, om);
		} catch (GameException e) 
		{
			sendError(response, PROTOCOL_Cli_GetMsg, e.getId());
		} catch (Exception e) 
		{
			logger.error(e.getMessage(), e);
		}

	}

	/**
	 * 拉取跑马灯信息
	 */
	public void getBroadcast(SocketRequest request, SocketResponse response)
	{
		try {
			InputMessage im = request.getInputMessage();
			String userId = im.getUTF();
			String sessionId = im.getUTF();
			
			filterSessionId(userId, sessionId);
			userAction().getExistUser(userId);// 玩家是否存在
			List<Marquee> marqueeList = commonAction().getMarqueeList();
            
			OutputMessage om = new OutputMessage(true);
			om.putInt(marqueeList.size());
			// 遍历跑马灯集合
			for(Marquee m : marqueeList)
			{
				om.putString(m.getContent());
			}
			response.sendMessage(PROTOCOL_Cli_GetBroadCast, om);
			
		} catch (GameException e) 
		{
			sendError(response, PROTOCOL_Cli_GetBroadCast, e.getId());
			
		} catch (Exception e) 
		{
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 获取房间列表
	 */
	public void getRoomList(SocketRequest request, SocketResponse response) 
	{
		try {
			InputMessage im = request.getInputMessage();
			String userId = im.getUTF();
			String sessionId = im.getUTF();
			
			logger.info("玩家：userId=" + userId + "拉取房间列表的详情");
			filterSessionId(userId, sessionId);
			// 拉取玩家加入的房间号的集合
			userAction().getExistUser(userId);// 检测玩家是否存在
			Map<String, String> roomNumTimeMap = RedisResource.getDataFromRedis(userId);
			// 根据时间戳的值的大小进行排序
			List<Entry<String, String>> roomNumTimeList = new ArrayList<Entry<String, String>>(roomNumTimeMap.entrySet());

			Collections.sort(roomNumTimeList, new Comparator<Entry<String, String>>() {
					@Override
					public int compare(Entry<String, String> o1, Entry<String, String> o2)
					{
					  return o2.getValue().compareTo(o1.getValue());
					}
				});
			
			OutputMessage om = new OutputMessage(true);
			om.putInt(roomNumTimeList.size());
			for(Entry<String,String> entry : roomNumTimeList)
			{
				PrivateRoom privateRoom = roomAction().getPrivateRoom(entry.getKey());
				if (privateRoom == null) 
				{
					continue;
				}
				om.putString(String.valueOf(privateRoom.getRoomNum()));
				om.putInt(privateRoom.getUpBankerMode());
				if (privateRoom.getUpBankerMode() == BankerMode.BANKER_MODE_ALL_COMPARE.value) 
				{
					om.putInt(privateRoom.getAllCompareBaseScore());
				} else {
					om.putInt(privateRoom.getBaseScore()); // 底分
				}
				if (privateRoom.getUpBankerMode() == BankerMode.BANKER_MODE_ROTATE.value)
				{
					om.putInt(privateRoom.getRoundNum());
				} else 
				{
					om.putInt(privateRoom.getRoundNum());// 局数 10 or 20
					
				}
				om.putInt(privateRoom.getPayMode());// 支付方式
				om.putInt(privateRoom.getSitDownNum()); // 6个椅子上坐下的人数
				
			}
			
			response.sendMessage(PROTOCOL_Cli_GetRoomList, om);

		} catch (GameException e) 
		{
			sendError(response, PROTOCOL_Cli_GetRoomList, e.getId());
		} catch (Exception e) 
		{
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 加入房间
	 */
	public void joinRoom(SocketRequest request, SocketResponse response)
	{
		try {
			InputMessage im = request.getInputMessage();
			String userId = im.getUTF();
			String roomNum = im.getUTF();
			String sessionId = im.getUTF();
			
			filterSessionId(userId, sessionId);
			logger.info("userId:" + userId + "joinRoom");
			userAction().getExistUser(userId);
			PrivateRoom privateRoom = roomAction().getPrivateRoom(roomNum);
			if (privateRoom == null || privateRoom.getClubId() != 0) 
			{
				throw new GameException(GameException.PRIVATE_ROOM_IS_NOT_EXIST, "加入房间时，玩家：" + userId + "加入的房间不存在！");
			}

			// 玩家不是首次加入此房间，更新玩家加入此房间的时间
			RedisResource.joinRoomSetData(userId, roomNum);

			response.sendMessage(PROTOCOL_Cli_JoinRoom, new OutputMessage(true));

		} catch (GameException e) 
		{
			sendError(response, PROTOCOL_Cli_JoinRoom, e.getId());
			
		} catch (Exception e) 
		{
			logger.error(e.getMessage(), e);
		}

	}

	/**
	 * 刷新用户信息
	 */
	public void refreshUserInfo(SocketRequest request, SocketResponse response) throws GameException
	{
		try {
			InputMessage im = request.getInputMessage();
			String userId = im.getUTF();
			String sessionId = im.getUTF();
			
			filterSessionId(userId, sessionId);
			logger.info("uid:" + userId + "request refresh userInfo start");
			User user = userAction().getExistUser(userId);
			// 更新玩家的钻石信息
			OutputMessage om = new OutputMessage(true);
			om.putInt(user.getDiamond());
			response.sendMessage(PROTOCOL_Cli_RefreshUserInfo, om);
			logger.info("uid:" + userId + "request refresh userInfo end");
		} catch (GameException e) 
		{
			sendError(response, PROTOCOL_Cli_RefreshUserInfo, e.getId());
			
		} catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 创建房间 1.前期创建的房间主要是牛牛上庄和轮流上庄 2.定义牛牛上庄的参数是1 轮流上庄的参数是2
	 */
	public void createRoom(SocketRequest request, SocketResponse response) throws GameException 
	{

		try {
			InputMessage im = request.getInputMessage();
			String userId = im.getUTF();// 玩家的微信Id 如果选择一人支付则玩家的Id也是房主Id
			int gameType = im.getInt();// 游戏类型 牛牛上庄or轮流上庄
			int coin = im.getInt(); // 底分 1--->1/2 2---> 2/4 3---> 4/8
			int round = im.getInt(); // 局数 传的实际局数
			int costType = im.getInt(); // 0 -- 房主支付 1--AA支付
			int ruleType = im.getInt(); // 翻倍规则 默认选2 牛牛*4
			int allFace = im.getInt(); // 五花牛 0没选 1 已选 5倍
			int bomb = im.getInt();// 炸弹牛 6倍
			int allSmall = im.getInt();// 五小牛 8倍
			int playerInjection = im.getInt();// 闲家推注 0--没选 1--选 默认是0
			int noEnter = im.getInt();// 游戏开始后禁止加入 0--没选 1--选 默认是0
			int noShuffle = im.getInt();// 禁止搓牌 0--没选 1--选 默认是0
			// 固定庄家的上庄分数和明牌抢庄的最大抢庄
			int maxRobRanker = im.getInt();// 最大抢庄：（明牌抢庄模式特有） 1-1倍，2-2倍，3-3倍，4-4倍（默认值）
			int upBankerScore = im.getInt();// 上庄分数：（固定庄家模式特有） 0-无（默认值），1-50分，2-100分，3-150分，4-200分
			String sessionId = im.getUTF();

			logger.info("userId:" + userId + "create room");
			
			filterSessionId(userId, sessionId);
			User u = userAction().getExistUser(userId);
			// 房主支付，则扣去玩家的钻石，AA支付在游戏内坐下时支付
			if (costType == RoomPayMode.PAY_MODE_ONE.value)
			{
				int payDiamond = DiamondUtils.getPayDiamond(BankerMode.ValueOf(gameType), RoomPayMode.ValueOf(costType),
						round);
				if (!checkDiamond(u, payDiamond)) // 钻石不够
				{
					throw new GameException(GameException.USER_DIAMOND_NOT_ENOUGH, "选择房主支付时，玩家:" + userId + "的钻石不足");
				} else // 扣除钻石
				{
					userAction().deductDiamond(userId, payDiamond);
				}
			}

			// 随机生成一个房间号，并判断该房间是否存在，如果存在则生成另外一个，不存在的话可以使用
			int roomNum = new Random().nextInt(900000) + 100000;
			while ((roomAction().getPrivateRoom(roomNum)) != null || taurusLogAction().getUserOnlyRoomNumInFourDays(roomNum).size() != 0) 
			{
				roomNum = new Random().nextInt(900000) + 100000;
			}

			// 将创建房间的相关信息写入数据库
			PrivateRoom pr = new PrivateRoom();
			pr.setRoomNum(roomNum); // 获取生成的房间号
			pr.setUpBankerMode((byte) gameType);
			// 轮庄模式时，局数待定，先设置为0
			if (gameType == BankerMode.BANKER_MODE_ROTATE.value) {
				pr.setRoundNum((byte) 0);
			} else {
				pr.setRoundNum((byte) round);// 非轮庄牛牛模式局数是客户端传过来的局数
			}
			if (gameType != BankerMode.BANKER_MODE_ALL_COMPARE.value) // 不是通比牛牛模式时，底分选项1/2 2/4 4/8
			{
				pr.setBaseScore((byte) coin);
				pr.setPlayerInjection(conversionPlayerInjection(playerInjection));
			} else {
				pr.setAllCompareBaseScore((byte) coin); // 通比牛牛 底分 1 2 4 同时通比牛牛没有闲家推注
			}
			pr.setPayMode((byte) costType);
			pr.setTimesMode((byte) ruleType);
			pr.setAllFace((byte) allFace);
			pr.setBomb((byte) bomb);
			pr.setAllSmall((byte) allSmall);
			pr.setNoEnter((byte) noEnter);
			pr.setNoShuffle((byte) noShuffle);
			pr.setRoomOwnerId(Integer.valueOf(userId));
			pr.setSeatNum(6);// 座位数
			// 明牌抢庄模式--最大抢庄
			if (gameType == BankerMode.BANKER_MODE_BRIGHT_ROB.value) {
				pr.setMostRobBanker((byte) maxRobRanker);
			}
			// 固定庄家 --上庄分数
			if (gameType == BankerMode.BANKER_MODE_FIXED.value) {
				pr.setUpBankerScore(UpBankerScore.ValueOf(upBankerScore).value);
			}
			roomAction().createPrivateRoom(pr);

			// 创建房间时，将 key--roomNUM ,value--set<String> String-->userIdList
			RedisResource.createRoomSetData(userId, String.valueOf(roomNum));

			OutputMessage om = new OutputMessage(true);
			om.putString(String.valueOf(roomNum));
			response.sendMessage(PROTOCOL_Cli_CreateRoom, om);
			logger.info("创建房间时，玩家:userId=" + userId + "创建房间成功，房间号是roomNum=" + roomNum);

		} catch (GameException e)
		{
			sendError(response, PROTOCOL_Cli_CreateRoom, e.getId());
			
		} catch (Exception e) 
		{
			logger.error(e.getMessage(), e);
		}
	}

	/*
	 * 玩家查看代理商qq群。微信公众号信息协议
	 */
	public void getContactInfo(SocketRequest request, SocketResponse response)
	{
		try {
			InputMessage im = request.getInputMessage();
			String userId = im.getUTF();
			String sessionId = im.getUTF();
			
			filterSessionId(userId, sessionId);
			logger.info("userId:" + userId + "get contact info");
			// 检测玩家是否存在
			userAction().getExistUser(userId);
			// 拉取代理商的qq群和微信公众号
			CustomService service = commonAction().getCustomServiceInfo();
			if (service == null)
			{
				throw new GameException(GameException.CUSTOMER_SERVICE_IS_NOT_EXIST,
						"玩家userId：" + userId + "联系客服时，拉取的对象为null");
			}
			OutputMessage om = new OutputMessage(true);
			om.putString(service.getQqGroup());
			om.putString(service.getWxPublicAccount());
			om.putString( service.getQqService());
			response.sendMessage(PROTOCOL_Cli_GetContactInfo, om);
			
		} catch (GameException e) 
		{
			sendError(response, PROTOCOL_Cli_GetContactInfo, e.getId());
			
		} catch (Exception e) 
		{
			logger.error(e.getMessage(), e);
		}

	}

	public void recvDissolutionMsg(SocketRequest request, SocketResponse response)
	{
		try {
			InputMessage im = request.getInputMessage();
			String userId = im.getUTF();
			logger.info("start recvDissolutionMsg userId = " + userId);
			userAction().updateUserTableNum(userId, "");
			response.sendMessage(PROTOCOL_Cli_ReceiveDissolutionMsg, new OutputMessage(true));
			logger.info("end recvDissolutionMsg userId = " + userId);
		} catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

	}

	/*
	 * 房主支付时玩家的钻石不够创建房间
	 */
	public boolean checkDiamond(User user, int payDiamond) 
	{
		if (user.getDiamond() < payDiamond) {
			return false;
		}
		return true;
	}

	// 转换前端传过来的闲家推注的值
	private byte conversionPlayerInjection(int value) {
		switch (value) {
		case 0:
			return PlayerInjection.PLAYER_INJECTION_NONE.value;
		case 1:
			return PlayerInjection.PLAYER_INJECTION_5.value;
		case 2:
			return PlayerInjection.PLAYER_INJECTION_10.value;
		case 3:
			return PlayerInjection.PLAYER_INJECTION_20.value;
		default:
			return PlayerInjection.PLAYER_INJECTION_NONE.value;
		}
	}

}
