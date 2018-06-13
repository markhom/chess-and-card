package com.linyun.club.taurus.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.linyun.bottom.container.GameSession;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.club.taurus.manager.GameRoomManager;
import com.linyun.club.taurus.service.HundredsTaurusServer;
import com.linyun.club.taurus.task.GameTimer;
import com.linyun.club.taurus.utils.CardStyleMath;
import com.linyun.common.entity.BullGameLog;
import com.linyun.common.entity.BullWaybill;
import com.linyun.common.entity.FieldConfig;
import com.linyun.common.entity.GameAccountLog;
import com.linyun.common.entity.User;
import com.linyun.middle.common.taurus.card.Cards;
import com.linyun.middle.common.taurus.card.HandCard;
import com.linyun.middle.common.taurus.player.HundredsTaurusPlayer;
import com.linyun.middle.common.taurus.room.HundredsTaurusRoom;
import com.linyun.middle.common.taurus.room.HundredsTaurusRoom.GameStatus;
import com.linyun.middle.common.taurus.server.ActionAware;
import com.linyun.middle.common.taurus.table.HundredsTaurusTable;

/**
*  @Author walker
*  @Since 2018年5月23日
**/

public class HundredsTaurusEngine extends GameEngine
{
	
	    private static final Logger logger = LoggerFactory.getLogger(HundredsTaurusEngine.class);
	    //以下是游戏状态
		public static final int GAME_STATUS_BET = 1;  //押注阶段
		public static final int GAME_STATUS_STOP = 2;  //等待开奖
		public static final int GAME_STATUS_PAUSE = 3; //暂停
		public static final int GAME_STATUS_SUBSTITUTION = 4; //换人
		public static final int GAME_STATUS_SHUFFLE = 5; //洗牌
			
		public static final int ROOM_READY = 101; //房间 - 准备\ 开始
		public static final int ROOM_STOP = 102; //房间 -  停止\ 休息
		public static final int ROOM_CLOSE = 103;//房间 -  关闭 \ 停业
		
		public static final int BET_COUNTDOWN_COST=15;//下注倒计时初始值 单位 ：秒
		public static final int WAIT_LOTTERY_COUNTDOWN_COST = 3;//等待开奖倒计时 
		public static final int OPEN_CARDS_COUNTDOWN_COST = 6; //开牌动画倒计时
		
		/** 停止下注的秒数    需要在引擎状态由下注状态变为停止下注状态的时候赋值 */
		public int start_bet_countdown_second;
		
		/**每一局的比牌结果*/ 
		private HandCard m_cards_banker = new HandCard();//庄家的牌
		private HandCard m_cards_player1 = new HandCard();//天的牌
		private HandCard m_cards_player2 = new HandCard();//地的牌
		private HandCard m_cards_player3 = new HandCard();//人的牌
		
		final HundredsTaurusRoom  m_room;
		ActionAware m_action = new ActionAware();
		
		//上庄次数
		private int up_banker_time;
		
		/** 游戏的配置信息 */
		protected FieldConfig config;
		
	    private int m_engineTimer = 0; //引擎计时器
	    
		
		public HundredsTaurusEngine(HundredsTaurusTable table, FieldConfig config, HundredsTaurusRoom _room)
		{
			super(table);
			this.config = config;
			m_room = _room;
			step = GAME_STEP_WAIT;
			this.up_banker_time = 0;
			this.m_engineTimer =0;
		}
		
		//是否需要通知客户端封盘
		boolean isNeedSendCloseFieldBegin = false;
		//是否需要通知客户端封盘结束
		boolean isNeedSendCloseFieldEnd = false;
		
		@Override
		public void running(int timeFrame) throws Throwable
		{
			/** 游戏末开始 */

			if (step <= GAME_STEP_BEGIN)
				return;
			if (step > GAME_STEP_BEGIN && step < GAME_STEP_END)
			{
				try
				{
					/*String tableId = table.getTableNum();
					TableInfo tableInfo = TableInfoUtils.readTableInfoFromRedis(tableId);
					
					if (tableInfo == null)
					{
						return;
					}
					//处理封盘相关
					if (IsCloseField(tableInfo))
					{
						return;
					}*/
					
					m_engineTimer += GameTimer.DEFAULT_TIME;
					switch (step)
					{
						case GAME_STEP_WAIT:
							{
							   //Wait_ReadTableInfo(tableInfo);
							   setGameEngineStep(GAME_STEP_START_BET);
							}
							break;
						case GAME_STEP_START_BET:
							{
								//StartBet_ReadTableInfo(tableInfo);
								startBetCountDown();
							}
							break;
						case GAME_STEP_STOP_BET:    
							{
								StopBet();
							}
							break;
						case GAME_STEP_DEAL: 
							{
								DealPocker();
							}
							break;
						case GAME_STEP_CMP:
							{
								ComparePocker();
							}
							break;
						case GAME_STEP_FINISH:
							{
								finish();
								//handlerForeResult();
							}
							break;
						case GAME_STEP_REST:
							{
								//给前端播放动画预留时间
								if(m_engineTimer >= OPEN_CARDS_COUNTDOWN_COST)
								{
									setGameEngineStep(GAME_STEP_WAIT);
									notifyAllGameStatus(GameStatus.STARTBET);
									++m_showNum;
								}
							}
							break;
					}
					//handlerRepairRoadBill();
					
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		/**开始下注倒计时*/
		private void startBetCountDown()
		{
			OutputMessage om = new OutputMessage(true);
			om.putInt(BET_COUNTDOWN_COST-m_engineTimer);
			m_room.sendMessage(HundredsTaurusServer.PROTOCOL_BET_COUNTDOWN, om);
			if(m_engineTimer >= BET_COUNTDOWN_COST)
			{
				setGameEngineStep(GAME_STEP_STOP_BET);
				notifyAllGameStatus(GameStatus.WAITLOTTERY);
			}
			
		}
		
		protected void setGameEngineStep(int step)
		{
			m_engineTimer = 0; //不管什么时候进入的下一步骤，计时器需要清0
			this.step = step;
		}
		
		/**
		 * 通知客户端游戏状态发生变化 --- 公用方法
		 * */
		protected void notifyAllGameStatus(GameStatus status)
		{
			m_room.setGameStatus(status);
			OutputMessage om = new OutputMessage(true);
			om.putInt(status.value);
			m_room.sendMessage(HundredsTaurusServer.PROTOCOL_GAME_STATUS_CHANGE, om);
		}
		
		/*private boolean IsCloseField(TableInfo tableInfo)
		{
			*//** 先判断房间状态，1.准备开始状态下 ，直接返回不需要封盘 2.停止、关闭状态下，返回需要封盘*//*
			if (tableInfo.Status == ROOM_READY)
			{
				return false;
			}
			else if (tableInfo.Status == ROOM_STOP || tableInfo.Status == ROOM_CLOSE)
			{
				return true;
			}
			
			if (tableInfo.Status == GAME_STATUS_BET || tableInfo.Status == GAME_STATUS_STOP)
			{//正常情况，不处理
				isNeedSendCloseFieldBegin = true;
				if (isNeedSendCloseFieldEnd)
				{
					isNeedSendCloseFieldEnd = false;
					CloseField(getCloseFieldStatusByStep(), false);
					logger.info("第" + m_showNum + "局恢复正常，阶段为" + getCloseFieldStatusByStep() + "," + DateUtils.getFormatNowTime());
				}
				return false;
			}
			
			if (tableInfo.Status == GAME_STATUS_PAUSE )//暂停 
			{
				if (isNeedSendCloseFieldBegin)
				{
					isNeedSendCloseFieldBegin = false;
					CloseField((byte)0, false);
					logger.info("第" + m_showNum + "局暂停/挂起 封盘，状态：" + tableInfo.Status + "," + DateUtils.getFormatNowTime());
					isNeedSendCloseFieldEnd = true;
				}
			}
			else if (tableInfo.Status == GAME_STATUS_SUBSTITUTION || tableInfo.Status == GAME_STATUS_SHUFFLE) //   换人 || 洗牌
			{
				if (isNeedSendCloseFieldBegin)
				{
					
					isNeedSendCloseFieldBegin = false;
					CloseField((byte)0, true);
					logger.info("第" + m_showNum + "换人/洗牌 封盘，状态：" + tableInfo.Status + "," + DateUtils.getFormatNowTime());
					//洗牌阶段的话，设置洗牌时的局数
					m_action.roomAction().updateShuffleRound(m_showNum, m_room.getRoomId());
					m_room.setShuffleRound(m_showNum);
					
					isNeedSendCloseFieldEnd = true;
					//通知前端洗牌
					m_room.sendMessage(GameServer.PROTOCOL_Game_shuffle, new OutputMessage());
					
					if(tableInfo.Status == GAME_STATUS_SHUFFLE && step == GAME_STEP_START_BET)
					{
						step = GAME_STEP_WAIT;
						
						revokeOrShuffle();
					}
				}
			}
			
			return true;
		}*/
		private byte getCloseFieldStatusByStep()
		{
			byte status = 0;
			if (step == GAME_STEP_START_BET)
			{
				status = 1;
			}
			else
			{
				status = 2;
			}
			
			return status;
		}
		
		/*private void handlerForeResult()
		{
			CardsUtils.readForeRoundResult(table.getTableNum(), m_showNum);
		}*/
		
		/*private void handlerRepairRoadBill()
		{
			
			List<BullWayBill> blist = new ArrayList<BullWayBill>();
				
			List<RoadBillInfo> foreRoadlist = RoadBillInfoUtils.ReadForeRoadBillFromRedis(table.getTableNum());
			
			if(foreRoadlist != null)
			{
				for(RoadBillInfo rbi : foreRoadlist)
				{
					if(m_room.getShuffleRound()>= rbi.ShowNum) 
					{
					  //洗牌前的补单数据，不做处理
						continue;
					}
					BullWayBill bullWaybill = m_action.bullWayBillAction().selectBullWaybill(table.getId(), rbi.ShowNum);
					if(bullWaybill != null)
					{
					  bullWaybill.setBanker(Integer.parseInt(RoadBillInfoUtils.getResultByRoadBillStatus(rbi.Status)[0]));
					  bullWaybill.setPlayer1(RoadBillInfoUtils.getResultByRoadBillStatus(rbi.Status)[1]);
					  bullWaybill.setPlayer2(RoadBillInfoUtils.getResultByRoadBillStatus(rbi.Status)[2]);
					  bullWaybill.setPlayer3(RoadBillInfoUtils.getResultByRoadBillStatus(rbi.Status)[3]);
					  m_action.bullWayBillAction().updateBullWaybill(bullWaybill);
					  blist.add(bullWaybill);
					}else
					{
						//发送有效的补单数据到客户端
						BullWayBill  wb = new BullWayBill();
						wb.setRoomId(table.getId());
						wb.setRoomType((byte)SubUtil.getLastOneNum(config.getGameGrade())); //房间类型  普通场或者高级场 贵宾场
						wb.setRound(rbi.ShowNum);   //当前局数
						wb.setBanker(Integer.parseInt(RoadBillInfoUtils.getResultByRoadBillStatus(rbi.Status)[0]));
						wb.setPlayer1(RoadBillInfoUtils.getResultByRoadBillStatus(rbi.Status)[1]);
						wb.setPlayer2(RoadBillInfoUtils.getResultByRoadBillStatus(rbi.Status)[2]);
						wb.setPlayer3(RoadBillInfoUtils.getResultByRoadBillStatus(rbi.Status)[3]);
						wb.setSysProfit(0);
						m_action.bullWayBillAction().addBullWayBill(wb);
						
						blist.add(wb);
					}
					
				}
			}
			
				if (!blist.isEmpty())
				{
					OutputMessage om = new OutputMessage();
					om.putInt(blist.size());
					for (BullWayBill b : blist)
					{
						logger.info("补单成功,路单局号为:"+b.getRound());
						//庄家的点数
						om.putInt(b.getBanker());
						//闲家1的点数
					    om.putInt(Integer.valueOf(b.getPlayer1(),16)& 0x0f);
						//闲家1的输赢
					    om.putInt(Integer.valueOf(b.getPlayer1(),16) >> 4);
					    om.putInt(Integer.valueOf(b.getPlayer2(),16)& 0x0f);
						om.putInt(Integer.valueOf(b.getPlayer2(),16)>> 4);
						om.putInt(Integer.valueOf(b.getPlayer3(),16)& 0x0f);
					    om.putInt(Integer.valueOf(b.getPlayer3(),16)>> 4);
						om.putInt(b.getRound());
					}
					//发送补单的路单数据到客户端
					m_room.sendMessage(GameServer.PROTOCOL_Game_road_bill, om);
				}
				
			}
			*/
		
		/*private void CloseField(byte status, boolean isShuffle)
		{
			OutputMessage om = new OutputMessage();
			om.putByte(status);
			om.putBoolean(isShuffle);
			m_room.sendMessage(GameServer.PROTOCOL_Game_closeField, om);
		}*/
		
		/*private void Wait_ReadTableInfo(TableInfo tableInfo)
		{
			if (tableInfo == null)
			{
				logger.error("In Wait_ReadTableInfo function, tableInfo is null");
				return;
			}
			
			m_curRound = tableInfo.Total;
			m_showNum = tableInfo.ShowNum;
			
			//如果读取的桌子的状态是开始下注的状态  并且  引擎的状态是等待下注的阶段的话    则将 引擎的状态设置为开始下注的阶段 
			if (tableInfo.Status == GAME_STATUS_BET)
			{
				// 通知所有客户端玩家开始下注倒计时 
				OutputMessage om = new OutputMessage();
				om.putInt(tableInfo.StartTotal);
				om.putInt(m_curRound);
				m_room.sendMessage(GameServer.PROTOCOL_Game_start_bet, om);
				
				step = GAME_STEP_START_BET;
			}

		}*/
		
		/*private void StartBet_ReadTableInfo(TableInfo tableInfo)
		{
			if (tableInfo == null)
			{
				logger.error("In StartBet_ReadTableInfo function, tableInfo is null");
				return;
			}
			
			if (tableInfo.Status == GAME_STATUS_BET)
			{ 
				//发送倒计的时间到前端
				OutputMessage om = new OutputMessage();
				om.putInt(tableInfo.StartTotal - tableInfo.StartCurr);
				m_room.sendMessage(GameServer.PROTOCOL_Game_start_bet_time_countDown, om);
			}
			else if (tableInfo.Status == GAME_STATUS_STOP)//如果读取的桌子的状态是停止下注的状态  并且  引擎的状态是开始下注的阶段的话    则将 引擎的状态设置为停止下注的阶段
			{
				step = GAME_STEP_STOP_BET;
			}

		}*/
		

		@Override
		public void StopBet() 
		{
			
			/** 通知所有客户端玩家 停止下注*/
			
			//进入发牌阶段
			setGameEngineStep(GAME_STEP_DEAL);
			notifyAllGameStatus(GameStatus.WAITLOTTERY);
			
			System.out.println(table.getTableId() + "进入发牌阶段");
		}


		
		public void DealPocker()
		{     
			//int tableId = table.getTableId();
			if(m_engineTimer >= WAIT_LOTTERY_COUNTDOWN_COST)
			{
				//得到六副手牌
				HandCard[] handCards = Cards.getAllHandCard(String.valueOf(m_room.getRoomId()));
				m_cards_banker.setCards(handCards[0].getCards());
				m_cards_player1.setCards(handCards[1].getCards());
				m_cards_player2.setCards(handCards[2].getCards());
				m_cards_player3.setCards(handCards[3].getCards());
				setGameEngineStep(GAME_STEP_CMP);
			}
			
			//CardsInfo cardsInfo = CardsUtils.ReadOneRoundResultFromRedis(tableId, m_showNum);
			
			/*if (cardsInfo != null)
			{
				
				if (cardsInfo.Status == CardsUtils.CARDS_STATUS_OK) //or cardsInfo.Done value is ok
				{//如果发牌的数据是正确的
					
					m_cards_banker.setCards(cardsInfo.cardsBanker);
					m_cards_player1.setCards(cardsInfo.cardsPlayer1);
					m_cards_player2.setCards(cardsInfo.cardsPlayer2);
					m_cards_player3.setCards(cardsInfo.cardsPlayer3);
					//进入比牌阶段
					step = GAME_STEP_CMP;
				}
				else
				{//如果发牌数据时错误的，那么本局牌不进行计算，1.归还玩家本局押注的筹码 2.通知前端牌数据出错 3.通知玩家补单数据
					//添加到补单列表中
					logger.info("add roadbill , showNum =" + m_showNum);
					step = GAME_STEP_WAIT;
					
					revokeOrShuffle();
				}
			
				
			}*/

		} 
		
		/**用于任意阶段实行撤单或洗牌操作的后续处理 */
		/*public void revokeOrShuffle()
		{
			synchronized (playerMap) 
				  {
				//1.归还玩本局押注筹码
				for (Entry<String, Player> entry : playerMap.entrySet())
				{
					TaurusPlayer player = (TaurusPlayer) entry.getValue();
					int betChip = player.getBetChip();
					if(betChip > 0)
					{
						String playerId = player.getPlayerId();
						//归还玩家本局押注的筹码
						if(playerId.startsWith("1"))
						{
							m_action.userAction().updateUserCoin(playerId, player.getBetChipActual(), 0);
						}else
						{
							PlatformUser p = m_action.platformUserAction().getExistUserById(playerId);
							long chip = p.getChip();
							p.setChip(chip+player.getBetChipActual());
							m_action.platformUserAction().updatePlatformUser(p);
						}
					    
						CommonUser user = getExistUser(playerId);
						
						清空错误牌局玩家的下注信息
						player.clear();
						//通知前端牌数据出错
						OutputMessage om = new OutputMessage();
						om.putInt(betChip);
						om.putString(String.valueOf(user.getChip()));
						GameSession session = Container.getSessionById(BaseServer.userSessionMap.get(player.getPlayerId()));
						session.sendMessage(GameServer.PROTOCOL_Game_cards_error, om);
						try
						{
							m_action.lobbyAction().SendMessage(player.getPlayerId(), GameServer.PROTOCOL_Game_new_message, new OutputMessage());	
						}catch(Exception e)
						{
							logger.error(e.getMessage(),e);
						}
						
						
					   //撤单通知
						SiteLetter s = new SiteLetter();
						s.setGameType(ConstantsUtils.GAME_TYPE_牛牛);
						s.setPlayerId(player.getPlayerId());
						s.setContent("由于奖源开奖延迟，为了让您免于遭受损失。已为您成功撤单并已返还您的下注金额，请注意查收。");
						s.setStatus(ConstantsUtils.NOT_READED);
						m_action.commonAction().addSiteLetter(s);
					}
					
				  }
				}
		}*/
		@Override
		public void ComparePocker() 
		{
			m_cards_banker.calcHundredsCardType();
			m_cards_player1.calcHundredsCardType();
			m_cards_player2.calcHundredsCardType();
			m_cards_player3.calcHundredsCardType();
            setGameEngineStep(GAME_STEP_FINISH);
			System.out.println(table.getTableId() + "进入结算阶段");
		}

		@Override
		public void finish() 
		{
			//@need 一局打完 结算相关
			concludeRound();
			reset();
			setGameEngineStep(GAME_STEP_REST);
			
			
			System.out.println(table.getTableId() + "进入等待阶段");
		}
		
		/**结算一局筹码相关的*/
		private void concludeRound()
		{
			
			Set<String> setPlayerId = playerMap.keySet();
			HundredsTaurusPlayer player = null;
			User dbUser = null;
			/**
			 * 每局先结算出闲家1,2,3的牌局倍率
			 */
			int[] odds = CardStyleMath.calPlayerIsWin(m_cards_banker, m_cards_player1, m_cards_player2, m_cards_player3);
			int p1_isWin = odds[0]>0 ? 1:0;
			int p2_isWin = odds[1]>0 ? 1:0;
			int p3_isWin = odds[2]>0 ? 1:0;

			//先循环遍历计算庄家的总输赢
			int banker_result = 0;
			for(String playerId : setPlayerId)
			{
				if (table.isBanker(playerId))
				{
					//庄家不在这里算，在最后结算庄家，这里结算所有跟注玩家
					continue;
				}
				player = playerMap.get(playerId);
				int bet_coin = player.getBet_coin();//玩家下注总和
				
				/** 玩家的一局得分 */
				CardStyleMath.clacPlayerScore(player,odds);
				
				if(bet_coin == 0)
				{
					continue;
				}
				/** 玩家一局的派奖金额 : 如果为正数则表示盈利  如果为负数则表示玩家该局的亏损*/
				int score_get_player = player.getScore();
				
				dbUser = getExistUser(playerId);
				int base_coin = dbUser.getCoin()+bet_coin; //玩家的本金，用于判断玩家是否够赔或者本局得分是否超过本金
				if(base_coin < Math.abs(score_get_player))
				{
					if(score_get_player < 0)
					{
						score_get_player = -base_coin;
					}else if(score_get_player > 0)
					{
						score_get_player = base_coin;
					}
				}
				player.setScore(score_get_player);
				
				banker_result -=score_get_player;
				
			}
			
			
			for (String playerId : setPlayerId)
			{
				if (table.isBanker(playerId))
				{
					//庄家不在这里算，在最后结算庄家，这里结算所有跟注玩家
					continue;
				}
				
				player = playerMap.get(playerId);
				int bet_coin = player.getBet_coin();//玩家下注总和
				
				if(bet_coin == 0)
				{
					GameSession session = m_room.getSessionByUserId(playerId);
					if (session != null)
					{
						OutputMessage om = new OutputMessage(true);
						om.putInt(-1);
						om.putInt(-1);
						om.putInt(getExistUser(playerId).getCoin());
						om.putBoolean(p1_isWin>0);
						om.putBoolean(p2_isWin>0);
						om.putBoolean(p3_isWin>0);
						om.putByte(m_cards_banker.getSpecType().value);
						om.putByte(m_cards_player1.getSpecType().value);
						om.putByte(m_cards_player2.getSpecType().value);
						om.putByte(m_cards_player3.getSpecType().value);
						
						om.putShort(m_cards_banker.getCards()[0]);
						om.putShort(m_cards_banker.getCards()[1]);
						om.putShort(m_cards_banker.getCards()[2]);
						om.putShort(m_cards_banker.getCards()[3]);
						om.putShort(m_cards_banker.getCards()[4]);
						om.putShort(m_cards_player1.getCards()[0]);
						om.putShort(m_cards_player1.getCards()[1]);
						om.putShort(m_cards_player1.getCards()[2]);
						om.putShort(m_cards_player1.getCards()[3]);
						om.putShort(m_cards_player1.getCards()[4]);
						om.putShort(m_cards_player2.getCards()[0]);
						om.putShort(m_cards_player2.getCards()[1]);
						om.putShort(m_cards_player2.getCards()[2]);
						om.putShort(m_cards_player2.getCards()[3]);
						om.putShort(m_cards_player2.getCards()[4]);
						om.putShort(m_cards_player3.getCards()[0]);
						om.putShort(m_cards_player3.getCards()[1]);
						om.putShort(m_cards_player3.getCards()[2]);
						om.putShort(m_cards_player3.getCards()[3]);
						om.putShort(m_cards_player3.getCards()[4]);
						
						session.sendMessage(HundredsTaurusServer.PROTOCOL_GAME_RESULT, om);
					}
					else
					{
						logger.warn("桌号" + table.getTableId() + "结算游戏结果的时候找不到玩家的session,playerId = " + playerId);
					}
					/*过滤没有下注的玩家*/
					continue ;
				}
				
				/** 玩家一局的派奖金额 : 如果为正数则表示盈利  如果为负数则表示玩家该局的亏损*/
				int score_get_player = player.getScore();
				
				dbUser = getExistUser(playerId);
				
				//记录玩家的帐变记录   玩家是跟注玩家
				GameAccountLog g = new GameAccountLog();
				g.setRoomType(table.getType());
				g.setRound(m_showNum);
				g.setUserId(dbUser.getUserId());
				g.setOldMoney(dbUser.getCoin()+bet_coin);//玩家下注之前的金额
				g.setNewMoney(dbUser.getCoin()+bet_coin+score_get_player);//玩家结算后金额
				g.setBetCoin(bet_coin);
				g.setRewardMoney(bet_coin+score_get_player);
				
				m_action.gameAccountLogAction().addGameAccountLog(g);
				
				//往玩家的游戏日志中,插入玩家这一局玩家的下注情况，玩家是跟注玩家
				BullGameLog ugl = new BullGameLog();
				ugl.setRoomType(table.getType());
				ugl.setRound(m_showNum);
				ugl.setUserId(dbUser.getUserId());
				ugl.setPlayer1_bet(player.getBet_sky());
				ugl.setPlayer2_bet(player.getBet_earth());
				ugl.setPlayer3_bet(player.getBet_people());
				ugl.setBet_total(player.getBet_coin());
				ugl.setReward_total(bet_coin+score_get_player);
				ugl.setIsBanker(0);//非庄家
				m_action.bullGameLogAction().addBullGameLog(ugl);
					
				//** 计算跟注玩家的筹码 *
				int coinNew = dbUser.getCoin()+bet_coin+score_get_player;
				dbUser.setCoin(coinNew);
				m_action.userAction().udpateCoin(playerId, coinNew);
				//刷新列表中玩家的金币
				User user = userMap.get(playerId);
				user.setCoin(coinNew);
				
				/** 发送跟注玩家本局的结算结果到客户端*/
				{ 
					GameSession session = m_room.getSessionByUserId(playerId);
					if (session != null)
					{
						OutputMessage om = new OutputMessage(true);
						om.putInt(banker_result);
						om.putInt(score_get_player);
						om.putInt(dbUser.getCoin());
						om.putBoolean(p1_isWin>0);
						om.putBoolean(p2_isWin>0);
						om.putBoolean(p3_isWin>0);
						om.putByte(m_cards_banker.getSpecType().value);
						om.putByte(m_cards_player1.getSpecType().value);
						om.putByte(m_cards_player2.getSpecType().value);
						om.putByte(m_cards_player3.getSpecType().value);
						
						om.putShort(m_cards_banker.getCards()[0]);
						om.putShort(m_cards_banker.getCards()[1]);
						om.putShort(m_cards_banker.getCards()[2]);
						om.putShort(m_cards_banker.getCards()[3]);
						om.putShort(m_cards_banker.getCards()[4]);
						om.putShort(m_cards_player1.getCards()[0]);
						om.putShort(m_cards_player1.getCards()[1]);
						om.putShort(m_cards_player1.getCards()[2]);
						om.putShort(m_cards_player1.getCards()[3]);
						om.putShort(m_cards_player1.getCards()[4]);
						om.putShort(m_cards_player2.getCards()[0]);
						om.putShort(m_cards_player2.getCards()[1]);
						om.putShort(m_cards_player2.getCards()[2]);
						om.putShort(m_cards_player2.getCards()[3]);
						om.putShort(m_cards_player2.getCards()[4]);
						om.putShort(m_cards_player3.getCards()[0]);
						om.putShort(m_cards_player3.getCards()[1]);
						om.putShort(m_cards_player3.getCards()[2]);
						om.putShort(m_cards_player3.getCards()[3]);
						om.putShort(m_cards_player3.getCards()[4]);
						
						session.sendMessage(HundredsTaurusServer.PROTOCOL_GAME_RESULT, om);
					}
					else
					{
						logger.warn("结算游戏结果的时候找不到玩家的session,playerId = " + playerId);
					}
				}
				
			}
			
			//每局结算后刷新玩家座次信息
		    userMap = sortMapByValue(userMap);
			int size = userMap.size()>8 ? 9 : userMap.size();
			
			OutputMessage om = new OutputMessage(true);
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
			om.putInt(size);
			int j =0;
			//重新给list中最多前9的玩家标座位号
			for(Entry<String,User> entry : userMap.entrySet())
			{
				++j;
				User u = entry.getValue();
				u.setSeatId(j);
				om.putString(u.getNickName());
				om.putString(String.valueOf(u.getUserId()));
				om.putString(u.getLoginIp());
				om.putInt(u.getDiamond());
				om.putInt(u.getCoin());
				if(j > size)
				{
					break;
				}
			}
			
			m_room.sendMessage(HundredsTaurusServer.PROTOCOL_REFRESH_SEATS_INFO, om);
			
			/** 判断庄家的位置是否有玩家，如果有，则计算庄家的 *//*
			CommonUser userBanker = table.getBankerSeat().getUser();
			*//** 系统本局的盈利情况  正数为盈利  负数为亏损 *//*
			long score_sys = 0;
			if (userBanker != null)
			{   
				*//** 庄家本局得分 *//*
				long score_get_banker = 0;
				*//** 庄家位置上玩家的最终分数 *//*
				long score_banker = userBanker.getChip();
				if (score_banker >= Math.abs(scoreTotal))
				{*//** 庄家的筹码不少于当前局跟注玩家的计算结果的总和 *//*
					//输赢都是庄家全部负责
					score_get_banker = -scoreTotal;
					score_sys = scoreTotalPlayersByDrawWater;
				}
				else 
				{*//** 庄家的筹码少于当前局跟注玩家计算结果的总和 *//*
					//需要系统帮付
					if (scoreTotal > score_banker) //所有跟注玩家赢的金币总和大于庄家的自身携带的金币，系统帮赔款
					{//跟注玩家计算结果的总和大于0，即庄家亏损，系统也亏损
						//庄家赔玩，系统帮赔
						score_get_banker = -score_banker; 
						score_sys = -scoreTotal - score_get_banker + scoreTotalPlayersByDrawWater;
					}
					else if (scoreTotal < -score_banker)//所有跟注玩家输的金币总和大于庄家自身携带的金币，系统跟着赚钱
					{//跟注玩家计算结果的总和小于0，即庄家盈利，系统也盈利
						score_get_banker = score_banker; 
						score_sys = -scoreTotal - score_get_banker + scoreTotalPlayersByDrawWater;
					}
				}
				 庄家位置上的玩家的抽水
				int  bankerDrawWater = 0 ;
				//普通场的上庄玩家盈利抽取5%
				if(score_get_banker > 0)
				{
					BigDecimal b = new BigDecimal(0.01d);
					BigDecimal c = new BigDecimal(config.getPumpScale());
					double pumpScale = b.multiply(c).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
					bankerDrawWater = (int)(score_get_banker*pumpScale);
					//玩家是庄家，玩家的的抽水写进数据库
					
					DrawWaterLog dwl = new DrawWaterLog();
					dwl.setGameType(ConstantsUtils.GAME_TYPE_牛牛);
					dwl.setRoomType(SubUtil.getLastOneNum(config.getGameGrade()));
					dwl.setRound(m_showNum);
					dwl.setUserId(userBanker.getId());
					dwl.setDrawWater(bankerDrawWater);
					m_action.drawWaterLogAction().addDrawWaterLog(dwl);
					
					score_get_banker -= bankerDrawWater ;
				}
				//如果庄家位置上的玩家盈利，将抽水累加到系统盈利里面
				score_sys  += bankerDrawWater ;
				score_banker +=score_get_banker;
				
					User user = (User) userBanker;
					更新玩家的帐变金额的信息，玩家是庄家,玩家此时是庄家，则新增一条帐变金额记录
					GameAccountLog  gal = new GameAccountLog();
					gal.setGameType(ConstantsUtils.GAME_TYPE_牛牛);
					gal.setRoomType(SubUtil.getLastOneNum(config.getGameGrade()));
					gal.setUserId(userBanker.getId());
					gal.setRound(m_showNum);
					gal.setOld_money(userBanker.getChip()); // 没有和玩家结算之前的筹码
					gal.setCur_money(score_banker); // 庄家位置上的玩家的最新筹码
					gal.setChangeMoney(0); //帐变金额，庄家不下注，下注金额是0
					gal.setRewardMoney((int)score_get_banker);  //派奖金额
					gal.setDrawWater(bankerDrawWater);
					gal.setIp(user.getLoginIp());
					m_action.gameAccountLogAction().addGameAccountLog(gal);
					
					更新玩家的游戏日志信息，玩家是庄家
					BullGameLog ugl = new BullGameLog();
					ugl.setUserId(userBanker.getId());
					ugl.setRoomType((byte)SubUtil.getLastOneNum(config.getGameGrade()));
					ugl.setRoomNum(table.getId()); //房间号
					ugl.setRound(m_showNum);  //当前局数
					ugl.setReward_Total((int)score_get_banker);  //派奖金额
					ugl.setIp(user.getLoginIp());
					ugl.setUpBankerMoney(((Long)userBanker.getChip()).intValue());
					ugl.setIsBanker((byte)1); 
					ugl.setBanker_result(m_cards_banker.getSpecType().value);
					String p1_isWin= String.valueOf(player1_isWin ? 1:0);
				    ugl.setPlayer1_result(p1_isWin+Integer.toHexString(m_cards_player1.getSpecType().value));
				    String p2_isWin= String.valueOf(player2_isWin ? 1:0);
				    ugl.setPlayer2_result(p2_isWin+Integer.toHexString(m_cards_player2.getSpecType().value));
				    String p3_isWin = String.valueOf(player3_isWin ? 1:0) ;
				    ugl.setPlayer3_result(p3_isWin+Integer.toHexString(m_cards_player3.getSpecType().value));
				    ugl.setBanker_cards(transferCards2String(m_cards_banker.getCards()));
				    ugl.setPlayer1_cards(transferCards2String(m_cards_player1.getCards()));
				    ugl.setPlayer2_cards(transferCards2String(m_cards_player2.getCards()));
				    ugl.setPlayer3_cards(transferCards2String(m_cards_player3.getCards()));
				    m_action.bullGameLogAction().addBullGameLog(ugl);
				    
				    //1.处理桌子上的庄家数据
					userBanker.setChip(score_banker);
					//2.处理庄家位置玩家数据到数据库
					m_action.userAction().updatePlayerChip(userBanker.getId(),score_banker);
			    
				*//** 发送庄家本局的结算结果到客户端 *//*
				GameSession session = Container.getSessionById(BaseServer.userSessionMap.get(userBanker.getId()));
				if (session != null)
				{
					OutputMessage om = new OutputMessage();
					om.putString(DateUtils.getFormatNowTime());   //当前时间
					om.putString(String.valueOf(score_get_banker));
					om.putInt(m_cards_banker.getSpecType().value);
					om.putInt(m_cards_player1.getSpecType().value);
					om.putInt(player1_isWin ? 1:0);
					om.putInt(m_cards_player2.getSpecType().value);
					om.putInt(player2_isWin ? 1:0);
					om.putInt(m_cards_player3.getSpecType().value);
					om.putInt(player3_isWin ? 1:0);
					
					om.putInt(m_showNum);
					om.putString(String.valueOf(score_banker));
					om.putByte((byte)1);
					om.putInt(m_cards_banker.getCards()[0]);
					om.putInt(m_cards_banker.getCards()[1]);
					om.putInt(m_cards_banker.getCards()[2]);
					om.putInt(m_cards_banker.getCards()[3]);
					om.putInt(m_cards_banker.getCards()[4]);
					om.putInt(m_cards_player1.getCards()[0]);
					om.putInt(m_cards_player1.getCards()[1]);
					om.putInt(m_cards_player1.getCards()[2]);
					om.putInt(m_cards_player1.getCards()[3]);
					om.putInt(m_cards_player1.getCards()[4]);
					om.putInt(m_cards_player2.getCards()[0]);
					om.putInt(m_cards_player2.getCards()[1]);
					om.putInt(m_cards_player2.getCards()[2]);
					om.putInt(m_cards_player2.getCards()[3]);
					om.putInt(m_cards_player2.getCards()[4]);
					om.putInt(m_cards_player3.getCards()[0]);
					om.putInt(m_cards_player3.getCards()[1]);
					om.putInt(m_cards_player3.getCards()[2]);
					om.putInt(m_cards_player3.getCards()[3]);
					om.putInt(m_cards_player3.getCards()[4]);
					
					session.sendMessage(GameServer.PROTOCOL_Game_result, om);
				}
				else
				{
					logger.info("结算游戏结果的时候找不到玩家<庄家>的session,playerId = " + userBanker.getId());
				}
			}
			else  //庄家的位置上没有玩家，则认为是系统
			{
				score_sys = -scoreTotal + scoreTotalPlayersByDrawWater ;
			}
			*/
			/** 这里将路单的数据写进数据库 */
			
			BullWaybill  wb = new BullWaybill();
			wb.setRoomType((byte)m_room.getRoomId());
			wb.setRound(m_showNum);   //当前局数
			wb.setBanker_result(m_cards_banker.getSpecType().value);
			wb.setBanker_cards(transferCards2String(m_cards_banker.getCards()));
			wb.setPlayer1_result(p1_isWin*16 + m_cards_player1.getSpecType().value);
			wb.setPlayer1_cards(transferCards2String(m_cards_player1.getCards()));
			wb.setPlayer2_result(p2_isWin*16 + m_cards_player2.getSpecType().value);
			wb.setPlayer2_cards(transferCards2String(m_cards_player2.getCards()));
			wb.setPlayer3_result(p3_isWin*16 + m_cards_player3.getSpecType().value);
			wb.setPlayer3_cards(transferCards2String(m_cards_player3.getCards()));
			 
			m_action.bullWaybillAction().addBullWaybill(wb);

			/*if (table.getBankerSeat().getUser() != null)
			{
				//庄家位子上的玩家的上庄次数加一
				up_banker_time ++ ;
				byte flag = IsCanBank(userBanker); //每一局结束判断庄家的筹码是否还够上庄
				if(flag != 0) 不能上庄
				{
					//如果庄家的金额不够，则把玩家从庄家位置上移走，如果庄家对列不为空，则把对列中的第一人移到庄家位置
					userBankerToCancel(userBanker, flag);
					
					//庄家位子上的计数清0
					up_banker_time = 0;
				}
			}
			else
			{//庄家位置上没人，等待列表有人
				waitPlayerUpBanker();
			}*/
		}
		
		/**将手牌转换成字符串保存到数据库中 */
		private String transferCards2String(short[] cards)
		{
			StringBuilder sb = new StringBuilder();
			for(short i : cards)
			{
				sb.append(i).append(",");
			}
			String result = sb.substring(0, sb.lastIndexOf(","));
			return result;
		}
		/** 每局打完需要做的相关清理 */
		private void reset()
		{
			//每盘结束清理牌数据
			m_cards_banker.reset();
			m_cards_player1.reset();
			m_cards_player2.reset();
			m_cards_player3.reset();
			table.reset();
			
			//清除玩家的一局押注相关信息
			synchronized (playerMap)
			{
				Set<String> setUserId = playerMap.keySet();
				for (String userId :setUserId)
				{
					HundredsTaurusPlayer player = playerMap.get(userId);
					player.clear();
				}
			}
			
			synchronized (listExitUserId)
			{
				
				//清理退出房间的，但已经在本局下注的玩家
			    for(Iterator<String> it = listExitUserId.iterator();it.hasNext();)
				{
			    	String userId = it.next();
					if (table.isBanker(userId))
					{//庄家，不做处理
						
					}
					else
					{
						//正常跟注玩家，直接清理
						/**table*/
						try
						{
							GameRoomManager.getInstance().unbindUserTable(userId);
							/** engine */
							this.delPlayer(userId);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
						it.remove();
						logger.info("玩家" + userId +"在房间" + table.getTableId() + "押注并且在本局游戏结束前退出了房间，没有再加入，被系统清理掉");
					}
				}
			}
		}
	   
		/*public byte IsCanBank(User user)
		{
			*//**进行上庄条件的判断， 包括 1.上庄最低限额2.历史充值金额<单位为元和金币换算需要拿这个数乘以1000>*//*
			int leastUpBankerLimit = config.getUpBankerMoney(); //上庄的最小金额
			if (user.getChip() < leastUpBankerLimit)
			{
				return 1 ;
			}
			
			if (up_banker_time >= 3)
			{
				return 2;
			}
			return 0;
		} */
		
		//处于庄家位置的玩家下庄 1.一局玩完 统计结果庄家筹码不够下庄  2.该玩家已经坐庄满三局也会下庄
		/*public void userBankerToCancel(User userBanker, byte flag)
		{
			 //如果庄家的金额不够，则把玩家从庄家位置上移走，如果庄家队列不为空，则把队列中的第一人移到庄家位置
			table.removeBankerUser();
			  
			String bankerId = String.valueOf(userBanker.getUserId());
			
			*//** 通知庄家自己被踢下庄 begin *//*
			logger.info("房间" + table.getTableId() + "玩家" + bankerId + "被踢下庄成功，状态" + flag);
			GameSession sessionBanker = Container.getSessionById(BaseServer.userSessionMap.get(bankerId));
			if (sessionBanker != null)
			{
				OutputMessage omBanker = new OutputMessage();
				omBanker.putString(bankerId);
				omBanker.putByte(flag);
				sessionBanker.sendMessage(GameServer.PROTOCOL_Game_kick_banker, omBanker);
			}
			else
			{
				logger.info("房间" + table.getId() + "玩家" + bankerId + "在庄家位置上，已经不在房间内或者断线，不发送被踢下庄的协议给玩家");
			}
			
			*//** end **//*	
			
			waitPlayerUpBanker();
		}*/
		/*private void waitPlayerUpBanker() 
		{
			synchronized (listBankers)
			{
				for(int i=0; i<listBankers.size();i++)
				{
					TaurusPlayer  player = (TaurusPlayer) listBankers.get(i);
					CommonUser user = getExistUser(player.getPlayerId());
					if(user.getState()==User.STATE_FROZEN)
					{
						listBankers.remove(i);
					}
				}
				//如果庄家等待列表中有人,则需要处理
				if (listBankers.size() != 0)
				{    
					String playerId = listBankers.get(0).getPlayerId();//对列中第一个玩家的id
					CommonUser user = getExistUser(playerId);
					try 
					{
						m_room.upBankerSuccess(GameServer.PROTOCOL_Game_bank_success, this, table, user, BaseServer.userSessionMap);
					} catch (Exception e) 
					{
						// TODO Auto-generated catch block
						logger.error(e.getMessage(),e); 
					}
				}
			}
		}*/
		
		public FieldConfig getConfig() 
		{
			return this.config;
		}
		
		public void setConfig(FieldConfig config)
		{
			this.config = config;
		}
		
		public User getExistUser(String userId)
		{
			User user = m_action.userAction().getExistUser(userId);
			return user;
		}
		
		/**
	     * 使用 Map按value进行排序
	     * @param map
	     * @return
	     */
	    public static Map<String, User> sortMapByValue(Map<String, User> oriMap) {
	       
	    	Map<String, User> sortedMap = new LinkedHashMap<String, User>();
	        if (oriMap == null || oriMap.isEmpty()) {
	            return sortedMap;
	        }
	       
	        List<Map.Entry<String, User>> entryList = new ArrayList<Map.Entry<String, User>>(
	                oriMap.entrySet());
	        Collections.sort(entryList, new Comparator<Map.Entry<String,User>>() { 

				@Override
				public int compare(Entry<String, User> o1, Entry<String, User> o2) {
					// TODO Auto-generated method stub
					return o2.getValue().getCoin()-o1.getValue().getCoin();
				}
			});

	        Iterator<Map.Entry<String, User>> iter = entryList.iterator();
	        Map.Entry<String, User> tmpEntry = null;
	        while (iter.hasNext()) {
	            tmpEntry = iter.next();
	            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
	        }
	        return sortedMap;
	    }
		
		public void addUser(String userId,User user)
		{
			synchronized(userMap)
			{
				userMap.put(userId,user);
			}
		}
		
		public void removeUser(String userId)
		{
			synchronized(userMap)
			{
			  userMap.remove(userId);
		    }
		}
		
		@Override
		public void addPlayer(int tableId, String userId) 
		{
			synchronized (playerMap)
			{
				curPlayers++;
				HundredsTaurusPlayer p = new HundredsTaurusPlayer(String.valueOf(tableId), userId);
				playerMap.put(p.getPlayerId(), p);
			}
			
		}

}
