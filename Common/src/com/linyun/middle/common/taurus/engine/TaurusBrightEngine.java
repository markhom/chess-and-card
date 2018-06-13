package com.linyun.middle.common.taurus.engine;

import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.net.SyslogAppender;

import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.common.taurus.eum.BankerChooseBaseScore;
import com.linyun.common.taurus.eum.GameStatus;
import com.linyun.middle.common.taurus.card.Cards;
import com.linyun.middle.common.taurus.card.HandCard;
import com.linyun.middle.common.taurus.player.TaurusPlayer;
import com.linyun.middle.common.taurus.protocol.TaurusProtocol;
import com.linyun.middle.common.taurus.room.TaurusRoom;
import com.linyun.middle.common.taurus.table.TaurusSeat;
import com.linyun.middle.common.taurus.table.TaurusTable;
import com.linyun.middle.common.taurus.task.GameTimer;

/**
 *  明牌抢庄
 *  @author liangbingbing
 *  */
public class TaurusBrightEngine extends GameEngine
{
	private static final int GAME_STEP_ROB_BANKER_DEAL_POCKER = 21; //抢庄发牌，第一阶段只发两张牌
	private static final int GAME_STEP_ROB_BANKER = 22;
	
	private static final int COUNT_DOWN_TIME_SECONDS_ROB_BANKER = 8*1000; //
	
	private static Logger logger = LoggerFactory.getLogger(TaurusBrightEngine.class);
	
	public TaurusBrightEngine(TaurusTable table, TaurusRoom room)
	{
		super(table, room);
	}

	@Override
	public void running(int timeFrame) throws Throwable 
	{
		/** 游戏末开始 */
		if (m_step <= GAME_STEP_BEGIN)
			return;
		if (m_step > GAME_STEP_BEGIN && m_step < GAME_STEP_END)
		{
			if (m_table.isDissolutionStage())
			{//解散阶段暂停游戏内逻辑,进行解散计数
				logger.info("解散阶段" + m_table.getDissolutionTimer());
				m_table.addDissolutionTimerCount(GameTimer.DEFAULT_TIME);
				if (m_table.isAllAgreeDissolution() || (m_table.getDissolutionTimer() >= COUNT_DOWN_TIME_SECONDS_DISSOLUTION_ROOM))
				{
					if ((m_cur_round>1) || oneRoundIsEnd)
					{
						handleUserTableNum();
					}
					
					if(isNewH5)
					{
						returnClubMemberCoin();
					}
					//解散时间到，进行解散操作
					OutputMessage om = new OutputMessage(true);
					om.putBoolean(true);
					m_room.sendMessage(TaurusProtocol.PROTOCOL_Ser_Room_Dissolution_Result, om);
					
					logger.info("房间" + m_room.getRoomId() + "[明牌抢庄]解散成功");
					destroy();
				}
				return;
			}
			
			//处理桌子各个位置的保座离桌状态
			handleKeepSeatExistTable();
			
			//如果参与游戏的人数少于2个终止游戏
			if(m_table.isInterruptStage())
			{
				if(m_room.getGameStatus() != GameStatus.GAME_STATUS_PAUSE)
				{
					logger.info("每次检测到人数不足两个时，房间的所属阶段为："+m_step);
					 notifyAllGameStatus(GameStatus.GAME_STATUS_PAUSE);
					 m_table.setCurBankerSeatNum(-1);
				}
				m_gameTimer += GameTimer.DEFAULT_TIME;
				if(m_gameTimer >= m_roomConfig.getGameTime()*60*1000)
				{
					returnClubMemberCoin();//游戏结束返还玩家金币
				    //通知所有房间内的客户端，牌局结束，解散房间
				    handleUserTableNum();
				    notifyAllGameStatus(GameStatus.GAME_STATUS_END);
				    logger.info("房间"+m_room.getRoomId()+"牌局完成，房间解散");
				    
					destroy();
				}
				
				return;
			}
			
			try
			{
				m_gameTimer += GameTimer.DEFAULT_TIME;
				m_engineTimer += GameTimer.DEFAULT_TIME;
				switch (m_step)
				{
					case GAME_STEP_READY:
						{
							if (m_engineTimer == COUNT_DOWN_TIME_SECONDS_AUTO_ACTION)
							{
								//托管倒计时时间到,处理托管玩家
								autoActionReady();
							}
							
							if (m_table.isAllReady() || (m_engineTimer >= COUNT_DOWN_TIME_SECONDS_READY))
							{
								if (!m_table.isAllReady())
								{
									//处理未托管 并且 未操作玩家
									HandleNoOperateReady();
								}
								setGameEngineStep(GAME_STEP_ROB_BANKER_DEAL_POCKER);
								m_room.setGameStatus(GameStatus.GAME_STATUS_TABLE_ROB_BANKER);
								logger.info(m_cur_round + "[明牌抢庄]准备完成，进入第一次发牌阶段");
							}
						}
						break;
					case GAME_STEP_ROB_BANKER_DEAL_POCKER:
						{
							RobBankerDealPocker();
							setGameEngineStep(GAME_STEP_ROB_BANKER);
							notifyAllGameStatus(GameStatus.GAME_STATUS_TABLE_ROB_BANKER);
							logger.info(m_cur_round + "[明牌抢庄]进入第一次发牌完成，进入抢庄阶段");
						}
						break;
					case GAME_STEP_ROB_BANKER:
						{
							if (m_table.isAllRobBanker() || m_engineTimer >= COUNT_DOWN_TIME_SECONDS_ROB_BANKER)
							{
								if (!m_table.isAllRobBanker())
								{
									//处理未托管 并且 未操作玩家
									HandleNoOperateRobBanker();
								}
								setGameEngineStep(GAME_STEP_BANKER_SURE);
								logger.info(m_cur_round + "[明牌抢庄]抢庄完成，庄家确定阶段");
							}
						}
						break;
					case GAME_STEP_BANKER_SURE:
						{
							BankerSure();
						}
						break;
					case GAME_STEP_BANKER_SURE_CARTTON:
						{
							if (m_engineTimer >= COUNT_DOWN_TIME_SECONDS_BANKER_SURE_CARTOON)
							{
								BankerSureCartoonEnd();
							}
						}
						break;
					case GAME_STEP_BANKER_CHOOSE_BASECOIN:
						{
							if (m_table.isChooseBaseCoin() || (m_engineTimer >= COUNT_DOWN_TIME_SECONDS_BANKER_CHOOSE_BASECOIN))
							{
								if (!m_table.isChooseBaseCoin())
								{//时间到<这里的托管时间和正常倒计时时间一致，只做一次处理即可>，庄家未选择，默认选择最小值
									m_table.setChooseBaseCoin(BankerChooseBaseScore.BANKER_BASE_SCORE_2);
									OutputMessage om = new OutputMessage(true);
									om.putByte(BankerChooseBaseScore.BANKER_BASE_SCORE_2.value);
									m_room.sendMessage(TaurusProtocol.PROTOCOL_Ser_Banker_Choose_BaseCoin, om);
								}
								
								setGameEngineStep(GAME_STEP_BET);
								logger.info(m_cur_round + "[明牌抢庄]押注阶段");
								//@need 通知客户端押注开始
								Bet();
							}
						}
						break;
					case GAME_STEP_BET:
						{
							if (m_engineTimer == COUNT_DOWN_TIME_SECONDS_AUTO_ACTION)
							{
								//托管倒计时时间到,处理托管玩家
								autoActionBet();
							}
							
							if (m_table.isAllBet() || (m_engineTimer >= COUNT_DOWN_TIME_SECONDS_BET))
							{
								if (!m_table.isAllBet())
								{
									//处理未托管 并且 未操作玩家
									HandleNoOperateBet();
								}
								setGameEngineStep(GAME_STEP_OPEN_CARDS);
								notifyAllGameStatus(GameStatus.GAME_STATUS_TABLE_OPEN_CARDS);
								
								logger.info(m_cur_round + "[明牌抢庄]押注完成，进入亮牌阶段");
								
								calCardType();
								
							}
						}
						break;
					case GAME_STEP_OPEN_CARDS:
						{
							if (m_engineTimer == COUNT_DOWN_TIME_SECONDS_AUTO_ACTION)
							{
								//托管倒计时时间到,处理托管玩家
								autoActionOpenCards();
							}
							
							if (m_table.isAllOpenCards() || (m_engineTimer >= COUNT_DOWN_TIME_SECONDS_OPEN_CARDS))
							{
								if (!m_table.isAllOpenCards())
								{
									//处理未托管 并且 未操作玩家
									HandleNoOperateOpenCards();
								}
								
								setGameEngineStep(GAME_STEP_BANKER_OPEN_CARDS);
								logger.info(m_cur_round + "[明牌抢庄]亮牌完成，进入庄家开牌阶段");
							}
						}
						break;
					case GAME_STEP_BANKER_OPEN_CARDS:
						{
							notifyAllGameStatus(GameStatus.GAME_STATUS_TABLE_BANKER_OPEN_CARDS);
							setGameEngineStep(GAME_STEP_OPEN_CARDS_CARTOON);
							logger.info(m_cur_round + "[明牌抢庄]发送庄家开牌阶段状态变化，进入比牌阶段");
						}
						break;
					case GAME_STEP_OPEN_CARDS_CARTOON:
						{
							if (m_engineTimer >= COUNT_DOWN_TIME_SECONDS_OPEN_CARDS_CARTOON)
							{
								setGameEngineStep(GAME_STEP_COMPARE_CARDS);
								logger.info(m_cur_round + "[明牌抢庄]比牌阶段");
							}
							
						}
						break;
					case GAME_STEP_COMPARE_CARDS:
						{
							if(isNewH5)
							{
								ComparePockerForNewH5();
							}else
							{
								ComparePocker();
							}
							
							//一局完成的相关处理
							concludeOneRound();
							
							setGameEngineStep(GAME_STEP_FINISH);
							logger.info(m_cur_round + "[明牌抢庄]游戏结果");
						}
						break;	
					case GAME_STEP_FINISH:
					{
						if (Finish())
						{
							return;
						}
						setGameEngineStep(GAME_STEP_FINISH_CARTOON);
						logger.info(m_cur_round + "[明牌抢庄]播放金币动画");
						break;
					}
					case GAME_STEP_FINISH_CARTOON:
					{//重置一局数据 并且开始下一局
						if (m_engineTimer >= COUNT_DOWN_TIME_SECONDS_GAME_FINISH_CARTOON)
						{
							//重置一局相关的数据
							Reset();
							//设置桌子上的所有玩家状态为加入游戏,用于处理不是第一局加进来的玩家的处理 
							m_table.setAllJoinGame();
							//处理桌子上玩家的 房间号<后面加入的，不是开始游戏时就已经存在的>
							handleUserTableNum();
							//开始下一局
							this.m_cur_round += 1; //游戏开始，当前局数+1
							notifyAllGameStatus(GameStatus.GAME_STATUS_TABLE_READY);
							setGameEngineStep(GAME_STEP_READY);
							logger.info(m_cur_round + "[明牌抢庄]准备阶段");
							m_table.setCurRound(this.m_cur_round);
						}
					}
					break;
				}
			}
			catch (Exception e)
			{
				logger.error(e);
			}
		}
	}

	public void RobBankerDealPocker()
	{
		//得到六副手牌
		HandCard[] handCards = Cards.getAllHandCard(String.valueOf(m_room.getRoomId()));
		int count = 0;
		for (int i=0; i<SEAT_NUM; ++i)
		{//需要注意玩家分散坐的情况
			TaurusSeat seat = m_table.getSeat(i);
			if (!seat.isJoinGame())
			{//未加入游戏的玩家
				continue;
			}
			seat.getCards().setCards(handCards[count++].getCards());
		}
		
		OutputMessage om = new OutputMessage(true);
		om.putByte((byte)count);
		for (int i=0; i<SEAT_NUM; ++i)
		{//需要注意玩家分散坐的情况
			TaurusSeat seat = m_table.getSeat(i);
			if (!seat.isJoinGame())
			{//未加入游戏的玩家
				continue;
			}
			om.putString(seat.getPlayer().getPlayerId());
			om.putShort(seat.getCards().getCards()[0]);
			om.putShort(seat.getCards().getCards()[1]);
			om.putShort(seat.getCards().getCards()[2]);
			om.putShort(seat.getCards().getCards()[3]);
			
			logger.info("[明牌抢庄]before calc userId:" + seat.getPlayer().getPlayerId() + "[" + seat.getCards().toString() + "]") ;
			
		}
		m_room.sendMessage(TaurusProtocol.PROTOCOL_Ser_Deal_Cards, om);
	}
	//开牌前对玩家手里的牌进行排序
	public void calCardType()
	{
		//开牌前先对牌进行排序
		for (int i=0; i<SEAT_NUM; ++i)
		{//需要注意玩家分散坐的情况
			TaurusSeat seat = m_table.getSeat(i);
			if (!seat.isJoinGame())
			{//未加入游戏的玩家
				continue;
			}
			
			seat.getCards().calcCardType(m_roomConfig.getSpecConfig());//发送无序牌完成了，需要计算当前座位上上的牌
			logger.info("[明牌抢庄]after calc userId:" + seat.getPlayer().getPlayerId() + "[" + seat.getCards().toString() + "]") ;
		}
	}
	@Override
	public void calcBanker()
	{//设置当前庄家为上一局的庄家//闲家推注会用到
		m_table.setPrevBankerSeatNum(m_table.getCurBankerSeatNum());
	}
	
	@Override
	public void BankerSure()
	{
		/**计算思路：1，先找到所有的抢庄玩家   
		 * 2.然后根据抢庄玩家抢庄的倍数来确定庄家  2-1: 倍数最高的只有一位  直接成为庄家
		 * 							  2-2：倍数最高的有多位  随机一位成为庄家
		 * */
		TaurusSeat[] seats = m_table.getSeats();
		TaurusPlayer player = null;
		int [] robBankers = new int[TaurusTable.TABLE_SEAT_NUM];//抢庄玩家列表
		int countRobBankers = 0;
		int [] joinGames = new int[TaurusTable.TABLE_SEAT_NUM];
		int countJionGames = 0;
		int maxRobNum = 1;//如果没人抢庄，则最大抢庄分值为1
		synchronized (seats)
		{
			for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
			{
				if (!seats[i].isJoinGame())
				{
					continue;
				}
				
				joinGames[countJionGames++] = i;
				player = seats[i].getPlayer();
				if (player.getRobBankerNum() != 0)
				{
					robBankers[countRobBankers++] = i;
					if (maxRobNum < player.getRobBankerNum())
					{
						maxRobNum = player.getRobBankerNum();
					}
				}
			}
		}
		logger.info("[明牌抢庄] maxRobNum is " + maxRobNum + ",joinGame number is " + countJionGames);
		m_table.setBrightRobBankerNum(maxRobNum);
		
		int pos = -1;//庄家的位子
		if (countRobBankers == 0)
		{
			//没有人抢庄，所有参与游戏玩家随机出一个庄家
			int index = (new Random()).nextInt(countJionGames);
			pos = joinGames[index];
			logger.info("[明牌抢庄]没人抢庄，所有参与游戏玩家随机出一个庄家：pos =" + pos + ",index = " + index);
		}
		else if (countRobBankers == 1)
		{
			//只有一个人抢庄，成为庄家
			pos = robBankers[0];
			logger.info("[明牌抢庄]只有一个人抢庄，成为庄家：pos =" + pos);
		}
		else 
		{
			/**  先找到最多抢庄的倍数，然后进行比较，找到和最多抢庄倍数相等的抢庄玩家， 1个的话直接成为庄家 ，多个的话，从其中随机
			 * */
			//多人抢庄   ，比较抢庄的倍数,  如果抢装倍数最高不止一人的话，从最多抢庄的人中随机
			int[] maxRobBankerList = new int[TaurusTable.TABLE_SEAT_NUM];
			int countMaxRobBanker = 0;
			for (int i=0; i<countRobBankers; ++i)
			{
				if (maxRobNum == seats[robBankers[i]].getPlayer().getRobBankerNum())
				{
					maxRobBankerList[countMaxRobBanker++] = robBankers[i];
				}
			}
			
			if (countMaxRobBanker == 1)
			{
				pos = maxRobBankerList[0];
				logger.info("[明牌抢庄]最多抢庄人数只有一个，成为庄家 ：pos =" + pos);
			}
			else
			{
				pos = maxRobBankerList[(new Random()).nextInt(countMaxRobBanker)];
				logger.info("[明牌抢庄]最多抢庄人数有多个，从其中随机：pos =" + pos);
			}
		}
		
		String bankerId = m_table.getSeat(pos).getPlayer().getPlayerId();//庄家id
		//将对应位置上的玩家设置为庄家
		m_table.setCurBankerSeatNum(pos+1);
		m_table.getSeat(pos).getPlayer().setIsBanker(true);
		
		//通知客户端庄家确定
		OutputMessage om = new OutputMessage(true);
		om.putString(bankerId);
		m_room.sendMessage(TaurusProtocol.PROTOCOL_Ser_Banker_Info, om);
		setGameEngineStep(GAME_STEP_BANKER_SURE_CARTTON);
		logger.info(m_cur_round + "[明牌抢庄]庄家确定完成，进入庄家确定动画阶段，庄家id" + bankerId);
	}
}
