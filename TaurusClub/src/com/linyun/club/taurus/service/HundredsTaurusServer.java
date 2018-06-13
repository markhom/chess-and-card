package com.linyun.club.taurus.service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.linyun.bottom.common.exception.GameException;
import com.linyun.bottom.container.GameSession;
import com.linyun.bottom.handler.SocketRequest;
import com.linyun.bottom.handler.SocketResponse;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.util.InputMessage;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.club.taurus.engine.GameEngine;
import com.linyun.club.taurus.engine.HundredsTaurusEngine;
import com.linyun.club.taurus.manager.FieldConfigManager;
import com.linyun.club.taurus.manager.GameRoomManager;
import com.linyun.club.taurus.manager.GameTableManager;
import com.linyun.common.entity.BullWaybill;
import com.linyun.common.entity.FieldConfig;
import com.linyun.common.entity.User;
import com.linyun.middle.common.taurus.club.BaseClubServer;
import com.linyun.middle.common.taurus.player.HundredsTaurusPlayer;
import com.linyun.middle.common.taurus.room.HundredsTaurusRoom;
import com.linyun.middle.common.taurus.table.HundredsTaurusTable;

/**
*  @Author walker
*  @Since 2018年5月24日
**/

public class HundredsTaurusServer extends BaseClubServer
{
	private static Logger logger = LoggerFactory.getLogger(HundredsTaurusServer.class);
	
	
	public static final short PROTOCOL_FIELD_INFO =7084; //拉取场次信息
	public static final short PROTOCOL_ENTER_ROOM =8100;    //加入房间
	public static final short PROTOCOL_REFRESH_SEATS_INFO =8101; //每局结算后刷新座次信息
	public static final short PROTOCOL_EXIT_ROOM = 8102; //玩家退出房间
	public static final short PROTOCOL_GAME_RESULT =8103;// 获取游戏结果
	public static final short PROTOCOL_GAME_STATUS_CHANGE = 8104;//游戏状态切换
	public static final short PROTOCOL_WAYBILL =8105; //拉取路单
	public static final short PROTOCOL_PLAYER_LIST_INFO =8106;// 获取玩家列表信息
	public static final short PROTOCOL_BET_COUNTDOWN = 8108;//下注倒计时
	public static final short PROTOCOL_LASTROUND_REPLAY = 8109; //上局回顾
	public static final short PROTOCOL_SELF_BET = 8110;  //自己下注
	public static final short PROTOCOL_OTHERS_BET = 8111; //其他玩家下注
	public static final short PROTOCOL_RECONNECT = 8112;//游戏内断线重连
	
	
	
	public void getFieldInfo(SocketRequest request,SocketResponse response)
	{
		try
		{
			InputMessage im = request.getInputMessage();
			String userId = im.getUTF();
			List<FieldConfig> configs = FieldConfigManager.getInstance().configs;
			OutputMessage om = new OutputMessage(true);
			om.putInt(3);
			for(FieldConfig config : configs)
			{
				om.putByte((byte)config.getTypeId());
				om.putString(config.getFieldName());
				
				HundredsTaurusEngine engine = getEngine(config.getTypeId());
				om.putInt(engine.curPlayers);
				om.putBoolean(config.getIsOpen()==FieldConfig.FIELD_OPEN);
				om.putInt(config.getEntryLimit());
				om.putInt(config.getUpBankerLimit());
				
			}
			
			response.sendMessage(PROTOCOL_FIELD_INFO, om);
			
		}catch(Exception e)
		{
			logger.error(e.getMessage(),e);
		}
		
	}
	
	/**游戏内重连**/
	public void reconnect(SocketRequest request, SocketResponse  response)
	{
		try
		{
			InputMessage im = request.getInputMessage();
			String userId = im.getUTF();
			byte tableId = im.getByte();
			GameSession session = request.getSession();
			
			User user = getExistUser(userId);
			HundredsTaurusTable table = getHundredsTable(tableId);
			if(table == null)
			{
				throw new GameException(GameException.TABLE_NOT_EXIST, "玩家" + userId + "游戏内重连时，找不到对应的桌子");
			}
			HundredsTaurusRoom room = getHundredsRoom(tableId);
			if (room == null)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST, "玩家" + userId + "游戏内重连时，找不到对应的房间");
			}
			
			HundredsTaurusEngine  engine = getEngine(tableId);
			if (engine == null)
			{
				throw new GameException(GameException.ENGINE_NOT_EXIST, "玩家" + userId + "游戏内重连时，找不到对应的游戏引擎");
			}
            HundredsTaurusPlayer player = engine.getPlayer(userId);
			
			//本房间的庄家 或者 之前在本房间内的下注玩家
			if (player != null)
			{
				engine.removeUserInExitList(userId);
			}
			else
			{
				if (user.getCoin() < engine.getConfig().getEntryLimit())
				{
					throw new GameException(GameException.ENTER_ROOM_COIN_LACK, "玩家" + userId + "进入房间" + tableId + "时，玩家筹码不符合进入房间条件，chip = " + user.getCoin());
				}
				
				/**table*/
				bindUserHundredsTable(userId, table);
				/**engine*/
				engine.addPlayer(tableId, userId);
			}
			
			/**room*/
			room.addSession(userId, session);
			
			FieldConfig config = engine.getConfig();
			/**返回客户端需要的数据*/
			OutputMessage om = new OutputMessage(true);
			om.putInt(room.getGameStatus().value);
			User banker = table.getBankerUser();
			if(banker == null)
			{
				om.putString("牛大人");
				om.putString("123456");
				om.putString("192.168.0.1");
				om.putInt(0);
				om.putInt(1000000000);
			}else
			{
				om.putString(banker.getNickName());
				om.putString(String.valueOf(banker.getUserId()));
				om.putString(banker.getLoginIp());
				om.putInt(banker.getDiamond());
				om.putInt(banker.getCoin());
			}
			om.putString(user.getNickName());
			om.putString(userId);
			om.putString(user.getLoginIp());
			om.putInt(user.getDiamond());
			om.putInt(user.getCoin());
			om.putInt(config.getMin_bet());
			om.putInt(config.getRate());
			om.putInt(table.getPlayer1_bet_total());
			om.putInt(table.getPlayer2_bet_total());
			om.putInt(table.getPlayer3_bet_total());
			
			Map<String,User> userMap = engine.getUserMap();
			int size = userMap.size() > 8 ?  8 : userMap.size();
			om.putInt(size);
			int j =0;
			for(Entry<String,User> entry : userMap.entrySet())
			{
				++j;
				User u = entry.getValue();
				om.putString(u.getNickName());
				om.putString(String.valueOf(u.getUserId()));
				om.putString(u.getLoginIp());
				om.putInt(u.getDiamond());
				om.putInt(u.getCoin());
				if(j>size)
				{
					break;
				}
			}
			
			session.sendMessage(PROTOCOL_RECONNECT, om); 
			engine.addUser(userId, user);
			
		}catch(GameException e)
		{
			logger.error(e.getMessage(),e);
			sendError(response, PROTOCOL_RECONNECT, e.getId());
		}
		
	}
	
	/** 进入房间 */
	public void enter(SocketRequest request, SocketResponse response)
	{
		InputMessage msg = request.getInputMessage();
		String userId = msg.getUTF();
		byte tableId = msg.getByte();  //作为房间号码
		logger.info("in enter room, userId is " + userId + "tableId is " + tableId );
		
		GameSession session = request.getSession();
		try
		{
			HundredsTaurusTable gameTable = getUserHundredsTable(userId);
			
			User user = getExistUser(userId);
			HundredsTaurusTable table = getHundredsTable(tableId);
			if (table == null) 
			{
				throw new GameException(GameException.TABLE_NOT_EXIST, "玩家" + userId + "进入房间" + tableId + "时，找不到对应的桌子");
			}
			if (gameTable != null)
			{
				if (gameTable.getTableId()!= tableId)
				{
					throw new GameException(GameException.ROOM_UNFINISHED_GAME, "玩家" + userId + "进入房间" + tableId + "时，在房间" + gameTable.getTableId() + "有未完成的牌局");
				}
			}
			
			HundredsTaurusRoom room = getHundredsRoom(tableId);
			if (room == null)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST, "玩家" + userId + "进入房间" + tableId + "时，找不到对应的房间");
			}
			
			HundredsTaurusEngine  engine = getEngine(tableId);
			if (engine == null)
			{
				throw new GameException(GameException.ENGINE_NOT_EXIST, "玩家" + userId + "进入房间" + tableId + "时，找不到对应的游戏引擎");
			}
			
			HundredsTaurusPlayer player = engine.getPlayer(userId);
			
			//本房间的庄家 或者 之前在本房间内的下注玩家
			if (player != null)
			{
				engine.removeUserInExitList(userId);
			}
			else
			{
				if (user.getCoin() < engine.getConfig().getEntryLimit())
				{
					throw new GameException(GameException.ENTER_ROOM_COIN_LACK, "玩家" + userId + "进入房间" + tableId + "时，玩家筹码不符合进入房间条件，chip = " + user.getCoin());
				}
				
				/**table*/
				bindUserHundredsTable(userId, table);
				/**engine*/
				engine.addPlayer(tableId, userId);
			}
			
			/**room*/
			room.addSession(userId, session);
			
			FieldConfig config = engine.getConfig();
			/**返回客户端需要的数据*/
			OutputMessage om = new OutputMessage(true);
			om.putInt(room.getGameStatus().value);
			User banker = table.getBankerUser();
			if(banker == null)
			{
				om.putString("牛大人");
				om.putString("123456");
				om.putString("192.168.0.1");
				om.putInt(0);
				om.putInt(1000000000);
			}else
			{
				om.putString(banker.getNickName());
				om.putString(String.valueOf(banker.getUserId()));
				om.putString(banker.getLoginIp());
				om.putInt(banker.getDiamond());
				om.putInt(banker.getCoin());
			}
			om.putString(user.getNickName());
			om.putString(userId);
			om.putString(user.getLoginIp());
			om.putInt(user.getDiamond());
			om.putInt(user.getCoin());
			om.putInt(config.getMin_bet());
			om.putInt(config.getRate());
			om.putInt(table.getPlayer1_bet_total());
			om.putInt(table.getPlayer2_bet_total());
			om.putInt(table.getPlayer3_bet_total());
			
			Map<String,User> userMap = engine.getUserMap();
			int size = userMap.size() > 8 ?  8 : userMap.size();
			om.putInt(size);
			int j =0;
			for(Entry<String,User> entry : userMap.entrySet())
			{
				++j;
				User u = entry.getValue();
				om.putString(u.getNickName());
				om.putString(String.valueOf(u.getUserId()));
				om.putString(u.getLoginIp());
				om.putInt(u.getDiamond());
				om.putInt(u.getCoin());
				if(j>size)
				{
					break;
				}
			}
			
			session.sendMessage(PROTOCOL_ENTER_ROOM, om); 
			engine.addUser(userId, user);
			
		}
		catch (GameException e)
		{
			logger.error(e.getMessage(), e);
			sendError(response, PROTOCOL_ENTER_ROOM, e.getId());
		}catch(Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 下注
	 * @param request
	 * @param response
	 */
	
	public void bet(SocketRequest request, SocketResponse response)
	{
		try
		{
			InputMessage im = request.getInputMessage();
			String userId = im.getUTF();
			int tagId = im.getInt();//0-2 分别代表天地人
			int betCoin = im.getInt();
			
			GameSession session = request.getSession();
			
			User user = userAction().getExistUser(userId);
			HundredsTaurusTable table = getUserHundredsTable(userId);
			if(table == null)
			{
				throw new GameException(GameException.TABLE_NOT_EXIST,"玩家"+userId+"下注时找不到桌子！");
			}
			HundredsTaurusRoom room = getHundredsRoom(table.getTableId());
			if(room == null)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST,"玩家"+userId+"下注时找不到房间！");
			}
			HundredsTaurusEngine engine = getEngine(table.getTableId());
			if(engine == null)
			{
				throw new GameException(GameException.ENGINE_NOT_EXIST,"玩家"+userId+"下注时找不到引擎！");
			}
			if(betCoin > user.getCoin())
			{
				throw new GameException(GameException.BET_COIN_LACK,"玩家"+userId+"下注时金币不足！");
			}
			FieldConfig config = engine.getConfig();
			int min_bet = config.getMin_bet();
			int rate = config.getRate();
			if(betCoin != min_bet && betCoin != min_bet*rate && betCoin != min_bet*Math.pow(rate,2) && 
				betCoin != min_bet*Math.pow(rate, 3) && betCoin != min_bet*Math.pow(rate,4))
			{
				throw new GameException(GameException.BET_COIN_ERROR,"玩家"+userId+"下注时传入的金额有误");
			}
			HundredsTaurusPlayer player = engine.getPlayer(userId);
			player.setBet_coin(betCoin);
			if(tagId == 0)
			{
				player.setBet_sky(betCoin);
				table.setPlayer1_bet_total(betCoin);
			}else if(tagId == 1)
			{
				player.setBet_earth(betCoin);
				table.setPlayer2_bet_total(betCoin);
			}else
			{
				player.setBet_people(betCoin);
				table.setPlayer3_bet_total(betCoin);
			}
			OutputMessage om = new OutputMessage(true);
			om.putString(userId);
			om.putInt(tagId);
			om.putInt(betCoin);
			om.putInt(user.getCoin()-betCoin);
			session.sendMessage(PROTOCOL_SELF_BET, om);
			//扣除玩家金币
			userAction().udpateCoin(userId, user.getCoin()-betCoin);
			
			Map<String,User> userMap = engine.getUserMap();
			User user2 = userMap.get(userId);
			
			OutputMessage om2 = new OutputMessage(true);
			om2.putString(userId);
			om2.putInt(tagId);
			om2.putInt(user2.getSeatId());
			om2.putInt(betCoin);
			
			room.sendMessage(PROTOCOL_OTHERS_BET, om2, session);
			
			
		}catch(GameException e)
		{
			logger.error(e.getMessage(),e);
			sendError(response, PROTOCOL_SELF_BET, e.getId());
		}
	}
	
	/**
	 *  获取玩家列表信息
	 * @param request
	 * @param response
	 */
	public void getPlayersInfo(SocketRequest request, SocketResponse response)
	{
		try
		{
			InputMessage im = request.getInputMessage();
			byte roomType = im.getByte();
			
			HundredsTaurusEngine engine = getEngine(roomType);
			Map<String,User> userMap = engine.getUserMap();
			OutputMessage om = new OutputMessage(true);
			om.putInt(userMap.size());
			for(Entry<String,User> entry : userMap.entrySet())
			{
				User user = entry.getValue();
				om.putString(user.getNickName());
				om.putString(String.valueOf(user.getUserId()));
				om.putString(user.getLoginIp());
				om.putInt(user.getDiamond());
				om.putInt(user.getCoin());
			}
			
			response.sendMessage(PROTOCOL_PLAYER_LIST_INFO, om);
			
		}catch(Exception e)
		{
			logger.error(e.getMessage(),e);
		}
	}
	
	public void lastRoundReplay(SocketRequest request, SocketResponse response)
	{
		try
		{
			InputMessage im = request.getInputMessage();
			byte roomType = im.getByte();
			HundredsTaurusEngine engine = getEngine(roomType);
			if(engine.m_showNum<2)
			{
				throw new GameException(GameException.NO_LASTROUND,"没有上局回顾！");
			}
			BullWaybill b = bullWaybillAction().getLastRoundResult(roomType, engine.m_showNum-1);
			
			OutputMessage om = new OutputMessage(true);
			om.putByte((byte)b.getBanker_result());
			om.putByte((byte)(b.getPlayer1_result()&0x0f));
			om.putByte((byte)(b.getPlayer2_result()&0x0f));
			om.putByte((byte)(b.getPlayer3_result()&0x0f));
			om.putString(b.getBanker_cards());
			om.putString(b.getPlayer1_cards());
			om.putString(b.getPlayer2_cards());
			om.putString(b.getPlayer3_cards());
			response.sendMessage(PROTOCOL_LASTROUND_REPLAY, om);
			
		}catch(GameException e)
		{
			logger.error(e.getMessage(),e);
			sendError(response, PROTOCOL_LASTROUND_REPLAY, e.getId());
		}
		
	}
	/**
	 *  拉取路单
	 * @param request
	 * @param response
	 */
	public void getWayBill(SocketRequest request, SocketResponse response)
	{
		try
		{
			InputMessage im = request.getInputMessage();
			byte roomType = im.getByte();
			List<BullWaybill> waybills = bullWaybillAction().selectWaybill(roomType);
			OutputMessage om = new OutputMessage(true);
			om.putInt(waybills.size());
			for(BullWaybill b : waybills)
			{
				om.putInt(b.getBanker_result());
				int result1 = b.getPlayer1_result();
				om.putInt(result1 & 0x0f);
				om.putBoolean(result1>>4 == 1);
				int result2 = b.getPlayer2_result(); 
				om.putInt(result2 & 0x0f);
				om.putBoolean(result2>>4 == 1);
				int result3 = b.getPlayer3_result();
				om.putInt(result3 & 0x0f);
				om.putBoolean(result3>>4 == 1);
			}
			response.sendMessage(PROTOCOL_WAYBILL, om);
			
		}catch(GameException e)
		{
			sendError(response, PROTOCOL_WAYBILL, e.getId());
		}catch(Exception e)
		{
			logger.error(e.getMessage(),e);
		}
	}
	
	/**
	 * 玩家退出房间
	 * @param request
	 * @param response
	 */
	public void exit(SocketRequest request, SocketResponse response)
	{
		try
		{
			InputMessage im = request.getInputMessage();
			String userId = im.getUTF();
			
	        HundredsTaurusTable table = getUserHundredsTable(userId);
			if (table == null)
			{
				throw new GameException(GameException.TABLE_NOT_EXIST, "玩家" + userId + "退出房间时，找不到对应的桌子");
			}
			
			HundredsTaurusRoom room = getHundredsRoom(table.getTableId());
			if (room == null)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST, "玩家" + userId + "退出房间时，找不到对应的房间");
			}
			
			HundredsTaurusEngine engine = getEngine(table.getTableId());
			if (engine == null)
			{
				throw new GameException(GameException.ENGINE_NOT_EXIST, "玩家" + userId + "退出房间时，找不到对应的游戏引擎");
			}
			
			logger.info(" user " + userId + "exit room " + room.getRoomId());
			
			//如果是庄家 或者 玩家已经下注的话，则退出房间不做相关清理
			HundredsTaurusPlayer player =  engine.getPlayer(userId);
			
			if (table.isBanker(userId) || player.getBet_coin() > 0)
			{
				engine.addUserInExitList(userId);
			}
			else
			{
				//正常清理
				/**table*/
				unbindUserHundredsTable(userId);
				/** engine */
				engine.delPlayer(userId);
			}
			
			GameSession session = request.getSession();
			engine.removeUser(userId);
			/** room */
			room.removeSession(userId);
		
			session.sendMessage(PROTOCOL_EXIT_ROOM, new OutputMessage(true));
			
		}catch(GameException e)
		{
			logger.error(e.getMessage(),e);
			sendError(response, PROTOCOL_EXIT_ROOM, e.getId());
		}
	}
	
	
	/** 获取桌子 */
	protected HundredsTaurusTable getHundredsTable(int tableId)
	{
		return GameTableManager.getInstance().getTable(tableId);
	}
	
	/**
	 * 绑定用户和桌子关系
	 */
	protected void bindUserHundredsTable(String userId, HundredsTaurusTable table)
	{
		GameRoomManager.getInstance().bindUserTable(userId, table);
	}

	/**
	 * 解绑定用户和桌子关系
	 */
	protected void unbindUserHundredsTable(String userId)
	{
		GameRoomManager.getInstance().unbindUserTable(userId);
	}

	
	protected HundredsTaurusTable getUserHundredsTable(String userId)
	{
		return GameRoomManager.getInstance().getUserTable(userId);
	}
	
	protected HundredsTaurusRoom getHundredsRoom(int tableId)
	{
		return GameRoomManager.getInstance().getRoom(tableId);
	}
	
	protected void removeEngine(HundredsTaurusTable table)
	{
		HundredsTaurusRoom room = getHundredsRoom(table.getTableId());
		room.remove("GameEngine");
	}

	protected HundredsTaurusEngine getEngine(int tableId)
	{
		HundredsTaurusRoom room = getHundredsRoom(tableId);
		return (HundredsTaurusEngine) room.getObject(GameEngine.GAME_ENGINE);
	}
	
	
	
	
}
