package com.linyun.middle.common.taurus.engine;

import org.apache.log4j.Logger;

import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.common.taurus.eum.BankerChooseBaseScore;
import com.linyun.common.taurus.eum.GameStatus;
import com.linyun.middle.common.taurus.player.TaurusPlayer;
import com.linyun.middle.common.taurus.protocol.TaurusProtocol;
import com.linyun.middle.common.taurus.room.TaurusRoom;
import com.linyun.middle.common.taurus.table.TaurusSeat;
import com.linyun.middle.common.taurus.table.TaurusTable;
import com.linyun.middle.common.taurus.task.GameTimer;
import com.linyun.middle.common.taurus.utils.Utils;

/**
 *  自由抢庄
 *  @author liangbingbing
 * */
public class TaurusFreeEngine extends GameEngine
{
	private static Logger logger = LoggerFactory.getLogger(TaurusFreeEngine.class);
	
	private static final int GAME_STEP_ROB_BANKER = 21;
	private static final int COUNT_DOWN_TIME_SECONDS_ROB_BANKER = 1000*15; //抢庄阶段，单位：秒
	
	public TaurusFreeEngine(TaurusTable table, TaurusRoom room)
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
					//解散时间到，进行解散操作
					OutputMessage om = new OutputMessage(true);
					om.putBoolean(true);
					m_room.sendMessage(TaurusProtocol.PROTOCOL_Ser_Room_Dissolution_Result, om);
					
					logger.info("房间" + m_room.getRoomId() + "[自由枪庄]解散成功");
					destroy();
				}
				return;
			}
			try
			{
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
								notifyAllGameStatus(GameStatus.GAME_STATUS_TABLE_ROB_BANKER);
								setGameEngineStep(GAME_STEP_ROB_BANKER);
								logger.info(m_cur_round + "[自由枪庄]准备完成，进入庄家确定阶段");
							}
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
								logger.info(m_cur_round + "[自由枪庄]押注阶段");
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
								setGameEngineStep(GAME_STEP_DEAL_CARDS);
								logger.info(m_cur_round + "[自由枪庄]发牌阶段");
							}
						}
						break;
					case GAME_STEP_DEAL_CARDS:
						{
							//需要通知客户端玩家的手牌数据
							DealPocker();
							notifyAllGameStatus(GameStatus.GAME_STATUS_TABLE_OPEN_CARDS);
							setGameEngineStep(GAME_STEP_OPEN_CARDS);
							logger.info(m_cur_round + "[自由枪庄]比牌阶段");
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
								logger.info(m_cur_round + "[自由枪庄]进入庄家开牌阶段");
							}
						}
						break;
					case GAME_STEP_BANKER_OPEN_CARDS:
						{
							notifyAllGameStatus(GameStatus.GAME_STATUS_TABLE_BANKER_OPEN_CARDS);
							setGameEngineStep(GAME_STEP_OPEN_CARDS_CARTOON);
							logger.info(m_cur_round + "[自由枪庄]发送庄家开牌阶段状态变化，进入比牌阶段");
						}
						break;
					case GAME_STEP_OPEN_CARDS_CARTOON:
						{
							if (m_engineTimer >= COUNT_DOWN_TIME_SECONDS_OPEN_CARDS_CARTOON)
							{
								setGameEngineStep(GAME_STEP_COMPARE_CARDS);
								logger.info(m_cur_round + "[自由枪庄]比牌阶段");
							}
							
						}
						break;
					case GAME_STEP_COMPARE_CARDS:
						{
							//比牌计算结果，需要通知客户端计算结果相关
							ComparePocker();
							//一局完成的相关处理
							concludeOneRound();
							
							setGameEngineStep(GAME_STEP_FINISH);
							logger.info(m_cur_round + "[自由枪庄]游戏结果");
						}
						break;	
					case GAME_STEP_FINISH:
					{
						if (Finish())
						{
							return;
						}
						setGameEngineStep(GAME_STEP_FINISH_CARTOON);
						logger.info(m_cur_round + "[自由枪庄]播放金币动画");
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
							logger.info(m_cur_round + "[自由枪庄]准备阶段");
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
	

	@Override
	public void BankerSure()
	{
		TaurusSeat[] seats = m_table.getSeats();
		TaurusPlayer player = null;
		
		int [] robBankers = new int[TaurusTable.TABLE_SEAT_NUM];
		int countRobBankers = 0;
		int [] joinGames = new int[TaurusTable.TABLE_SEAT_NUM];
		int countJoinGames = 0;
		synchronized (seats)
		{
			for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
			{
				if (!seats[i].isJoinGame())
				{
					continue;
				}
				
				joinGames[countJoinGames++] = i;
				player = seats[i].getPlayer();
				if (player.isRobBanker())
				{
					robBankers[countRobBankers++] = i;
				}
			}
		}
		
		int pos = -1;//庄家的位子
		if (countRobBankers == 0)
		{
			//没有人抢庄，所有参与游戏玩家随机出一个庄家
			pos = joinGames[Utils.getRandomInt(countJoinGames)];
		}
		else if (countRobBankers == 1)
		{
			//只有一个人抢庄，成为庄家
			pos = robBankers[0];
		}
		else 
		{
			//多人抢庄，从抢庄的人中随机
			pos = robBankers[Utils.getRandomInt(countRobBankers)];
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
	}

	@Override
	public void calcBanker()
	{//设置当前庄家为上一局的庄家//闲家推注会用到
		m_table.setPrevBankerSeatNum(m_table.getCurBankerSeatNum());
	}
}
