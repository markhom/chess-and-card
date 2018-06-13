package com.linyun.middle.common.taurus.utils;

import com.linyun.bottom.util.DateUtils;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.common.entity.User;
import com.linyun.common.taurus.eum.BankerMode;
import com.linyun.common.taurus.eum.BaseScoreType;
import com.linyun.common.taurus.eum.GameStatus;
import com.linyun.middle.common.taurus.bean.TaurusRoomConfig;
import com.linyun.middle.common.taurus.card.HandCard;
import com.linyun.middle.common.taurus.club.BaseClubServer;
import com.linyun.middle.common.taurus.player.TaurusPlayer;
import com.linyun.middle.common.taurus.room.TaurusRoom;
import com.linyun.middle.common.taurus.table.TaurusSeat;
import com.linyun.middle.common.taurus.table.TaurusTable;

public class MessageUtils
{
	//
	public static OutputMessage getEnterRoomMessage(final TaurusRoomConfig config, final String RoomNum, final TaurusRoom room, final TaurusTable table, final int roomOwnerId)
	{
		OutputMessage om = new OutputMessage(true);
		
		om.putString(String.valueOf(roomOwnerId));//房主Id
		om.putString(RoomNum);//房间号
		byte upBankerMode = config.getBankerMode().value;
		om.putByte(upBankerMode);//上庄模式
		if(BankerMode.ValueOf(upBankerMode) == BankerMode.BANKER_MODE_ALL_COMPARE)
		{   
			byte value = config.getAllCompareBaseScore().value;
			om.putInt(value);
		}
		else
		{   
			om.putInt(config.getGameTime() == 0 ? config.getBaseScore().value : config.getClubRoomBaseScore());//底分模式
		}
		om.putInt(config.getRoundNum());//局数
		om.putBoolean(config.getPayMode().value==1);//支付方式
		om.putByte(config.getTimesMode().value);//翻倍规则
		om.putByte(config.getMostRobBanker().value);//最大抢庄
		om.putShort(config.getUpBankerScore().value);//上庄分数
		om.putBoolean(config.getSpecConfig().isAllFace());//五花牛选项
		om.putBoolean(config.getSpecConfig().isBomb());//炸弹牛选项
		om.putBoolean(config.getSpecConfig().isAllSmall());//五小牛选项
		om.putByte(config.getPlayerInjection().value);//闲家推注
		om.putBoolean(config.getAdvancedOptions().isNoEnter());//游戏开始禁止加入
		om.putBoolean(config.getAdvancedOptions().isNoShuffle());//禁止搓牌
		
		if (table != null)
		{
			om.putInt(table.getCurRound());//局数，当前游戏局数
			om.putByte(room.getGameStatus().value);
			om.putByte((byte)table.getRealPlayer());//牌桌上的玩家人数
			om.putBoolean(true);
			
			byte joinGames = 0;
			String bankerId = "";
			for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
			{
				TaurusSeat seat = table.getSeat(i);
				if (seat.isCanSitDown())
				{//空位置，跳过
					continue;
				}
				
				++joinGames;
				
				if (table.getCurBankerSeatNum() == i+1)
				{
					bankerId = seat.getPlayer().getPlayerId();
				}
				
				TaurusPlayer player = seat.getPlayer();
				om.putByte((byte)(seat.getId()-1));//座位号
				om.putString(player.getPlayerId());
				User user = seat.getUser();
				om.putString(user.getNickName());
				om.putInt(player.getScoreTotal());
				om.putString(user.getHeadImgUrl());
				om.putString(user.getLoginAddress());
				om.putString(user.getLoginIp());
				om.putInt(user.getRoundNum());
				om.putString(DateUtils.getFormatDay(user.getRegisterTime()));
				om.putBoolean(seat.isJoinGame());//是否加入游戏
				
				om.putBoolean(seat.isReady());//是否准备
				om.putBoolean(seat.isBet());//是否押注
				om.putInt(player.getBetCoin());
				om.putBoolean(seat.isRobBanker());//是否抢庄
				om.putByte((byte)player.getRobBankerNum());
				boolean isOpenCards = seat.isOpenCards();
				om.putBoolean(isOpenCards);//是否开牌
				om.putBoolean(seat.isKeepSeatStage());
				om.putInt(seat.getKeepSeatTimer()/1000);
				if (isOpenCards)
				{
					HandCard handCard = seat.getCards();
					om.putShort(handCard.getCards()[0]);
					om.putShort(handCard.getCards()[1]);
					om.putShort(handCard.getCards()[2]);
					om.putShort(handCard.getCards()[3]);
					om.putShort(handCard.getCards()[4]);
					om.putByte(seat.getCards().getSpecType().value);
				}
				
			}
			om.putString(bankerId);
			
			if (config.getBankerMode() == BankerMode.BANKER_MODE_BRIGHT_ROB)
			{
				om.putByte(joinGames);
				for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
				{
					TaurusSeat seat = table.getSeat(i);
					if (seat.isCanSitDown())
					{//空位置，跳过
						continue;
					}
					TaurusPlayer player = seat.getPlayer();
					om.putString(player.getPlayerId());
					HandCard handCard = seat.getCards();
					om.putShort(handCard.getCards()[0]);
					om.putShort(handCard.getCards()[1]);
					om.putShort(handCard.getCards()[2]);
					om.putShort(handCard.getCards()[3]);
				}
			}
		}
		else
		{
			om.putInt(0);//局数，游戏未开始
			om.putByte(GameStatus.GAME_STATUS_INIT.value);
			om.putByte((byte)0);//牌桌上的玩家人数，没有人
			om.putBoolean(false);
		}
		
		return om;
	}
	
	public static OutputMessage getSelfEnterTableMessage(final TaurusRoom room, final TaurusTable table, byte seatId)
	{
		OutputMessage om = new OutputMessage(true);
		
		om.putInt(table.getCurRound());//局数，当前游戏局数
		om.putByte(room.getGameStatus().value);
		String bankerId = null;
		if (table.getCurBankerSeatNum() == 0 || table.getCurBankerSeatNum() == -1)
		{
			bankerId = "";
		}
		else
		{
			bankerId = table.getSeat(table.getCurBankerSeatNum()-1).getPlayer().getPlayerId();
		}
		om.putString(bankerId);
		om.putByte(seatId);
	
		return om;
	}
	
	/** 游戏内重连数据获取 */
	public static OutputMessage getReconnectMessageUtils(final TaurusRoom room, final TaurusTable table, final OutputMessage om)
	{
		if (table != null)
		{
			om.putInt(table.getCurRound());//局数，当前游戏局数
			om.putByte(room.getGameStatus().value);
			om.putByte((byte)table.getRealPlayer());//牌桌上的玩家人数
			om.putBoolean(true);
			String bankerId = "";
			byte joinGames = 0;
			for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
			{
				TaurusSeat seat = table.getSeat(i);
				if (seat.isCanSitDown())
				{//空位置，跳过
					continue;
				}
				
				++joinGames;
				
				if (table.getCurBankerSeatNum() == (i+1))
				{
					bankerId = seat.getPlayer().getPlayerId();
				}

				TaurusPlayer player = seat.getPlayer();
				om.putByte((byte)(seat.getId()-1));
				om.putString(player.getPlayerId());
				User user = seat.getUser();
				om.putString(user.getNickName());
				om.putInt(player.getScoreTotal());
				om.putString(user.getHeadImgUrl());
				om.putString(user.getLoginAddress());
				om.putString(user.getLoginIp());
				om.putInt(user.getRoundNum());
				om.putString(DateUtils.getFormatDay(user.getRegisterTime()));
				om.putBoolean(seat.isJoinGame());
				om.putBoolean(seat.isReady());//是否准备
				om.putBoolean(seat.isBet());//是否押注
				om.putInt(player.getBetCoin());
				om.putBoolean(seat.isRobBanker());//是否抢庄
				om.putByte((byte)player.getRobBankerNum());
				
				boolean isOpenCards = seat.isOpenCards();
				om.putBoolean(isOpenCards);//是否开牌
				om.putBoolean(seat.isKeepSeatStage());
				om.putInt(seat.getKeepSeatTimer()/1000);
				if (isOpenCards)
				{
					HandCard handCard = seat.getCards();
					om.putShort(handCard.getCards()[0]);
					om.putShort(handCard.getCards()[1]);
					om.putShort(handCard.getCards()[2]);
					om.putShort(handCard.getCards()[3]);
					om.putShort(handCard.getCards()[4]);
					om.putByte(seat.getCards().getSpecType().value);
				}
				
			}
			om.putString(bankerId);
			
			
			if (room.getConfig().getBankerMode() == BankerMode.BANKER_MODE_BRIGHT_ROB)
			{
				om.putByte(joinGames);
				for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
				{
					TaurusSeat seat = table.getSeat(i);
					if (seat.isCanSitDown())
					{//空位置，跳过
						continue;
					}
					
					TaurusPlayer player = seat.getPlayer();
					om.putString(player.getPlayerId());
					HandCard handCard = seat.getCards();
					om.putShort(handCard.getCards()[0]);
					om.putShort(handCard.getCards()[1]);
					om.putShort(handCard.getCards()[2]);
					om.putShort(handCard.getCards()[3]);
				}
			}
		}
		else
		{
			om.putInt(0);//局数，游戏未开始
			om.putByte(GameStatus.GAME_STATUS_INIT.value);//游戏状态，未开始
			om.putString("");//庄家Id
			om.putBoolean(false);
		}
		return om;
	}
	
	public static OutputMessage getReconnectMessage(final TaurusRoom room, final TaurusTable table)
	{
		OutputMessage om = new OutputMessage(true);
		getReconnectMessageUtils(room, table, om);
		return om;
	}
	
	/** 游戏外数据重连数据获取 */
	public static OutputMessage getOutReconnectMessage(final TaurusRoom room, final TaurusTable table)
	{
		OutputMessage om = new OutputMessage(true);
		
		TaurusRoomConfig config = room.getConfig();
		om.putString(String.valueOf(room.getRoomOwnerId()));//房主Id
		om.putString(String.valueOf(room.getRoomId()));//房间号
		om.putByte(config.getBankerMode().value);//上庄模式
		om.putInt(config.getBaseScore() != BaseScoreType.MODE_SCROLL_SELECTED ? config.getBaseScore().value : config.getClubRoomBaseScore());//底分模式
		om.putInt(config.getRoundNum());//局数
		om.putBoolean(config.getPayMode().value==1);//支付方式 
		om.putByte(config.getTimesMode().value);//翻倍规则
		om.putByte(config.getMostRobBanker().value);//最大抢庄
		om.putShort(config.getUpBankerScore().value);//上庄分数
		om.putBoolean(config.getSpecConfig().isAllFace());//五花牛选项
		om.putBoolean(config.getSpecConfig().isBomb());//炸弹牛选项
		om.putBoolean(config.getSpecConfig().isAllSmall());//五小牛选项
		om.putByte(config.getPlayerInjection().value);//闲家推注
		om.putBoolean(config.getAdvancedOptions().isNoEnter());//游戏开始禁止加入
		om.putBoolean(config.getAdvancedOptions().isNoShuffle());//禁止搓牌
	
		getReconnectMessageUtils(room, table, om);
		return om;
	}
}
