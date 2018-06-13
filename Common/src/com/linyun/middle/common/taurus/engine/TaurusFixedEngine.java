package com.linyun.middle.common.taurus.engine;

import org.apache.log4j.Logger;

import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.common.taurus.eum.BankerChooseBaseScore;
import com.linyun.common.taurus.eum.BaseScoreType;
import com.linyun.common.taurus.eum.GameStatus;
import com.linyun.common.taurus.eum.UpBankerScore;
import com.linyun.middle.common.taurus.protocol.TaurusProtocol;
import com.linyun.middle.common.taurus.room.TaurusRoom;
import com.linyun.middle.common.taurus.table.TaurusTable;
import com.linyun.middle.common.taurus.task.GameTimer;

/**
 *  固定庄家
 *  @author liangbingbing 
 * */
public class TaurusFixedEngine extends GameEngine
{
	private static Logger logger = LoggerFactory.getLogger(TaurusFixedEngine.class);
	
	public TaurusFixedEngine(TaurusTable table, TaurusRoom room)
	{
		super(table, room);
		m_table.setCurBankerSeatNum(1);//房主是庄家，庄家位子固定为1号位
		if (m_roomConfig.getUpBankerScore() != UpBankerScore.UP_BANKER_SCORE_NONE)
		{
			m_table.getUserSeat(String.valueOf(m_room.getRoomOwnerId())).getPlayer().setScoreTotal(m_roomConfig.getUpBankerScore().value);
		}
	}
	@Override
	public void InitEngine(TaurusTable table, TaurusRoom room)
	{
		super.InitEngine(table, room);
		m_table.setCurBankerSeatNum(1);//房主是庄家，庄家位子固定为1号位
		if (m_roomConfig.getUpBankerScore() != UpBankerScore.UP_BANKER_SCORE_NONE)
		{
			m_table.getUserSeat(String.valueOf(m_room.getRoomOwnerId())).getPlayer().setScoreTotal(m_roomConfig.getUpBankerScore().value);
		}
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
					
					logger.info("房间" + m_room.getRoomId() + "解散成功");
					destroy();
				}
				return;
			}
			if (m_cur_round >3 && m_table.isDownBanker())
			{
				m_room.sendMessage(TaurusProtocol.PROTOCOL_Ser_FIXED_Banker_Close_Game, new OutputMessage(true));
				logger.info("房间" + m_room.getRoomId() + "[固定庄家]庄家下庄成功");
				destroy();
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
								
								SureReadyNextStep();
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
								logger.info(m_cur_round + "[固定庄家]押注阶段");
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
								logger.info(m_cur_round + "[固定庄家]发牌阶段");
							}
						}
						break;
					case GAME_STEP_DEAL_CARDS:
						{
							//需要通知客户端玩家的手牌数据
							DealPocker();
							notifyAllGameStatus(GameStatus.GAME_STATUS_TABLE_OPEN_CARDS);
							setGameEngineStep(GAME_STEP_OPEN_CARDS);
							logger.info(m_cur_round + "[固定庄家]比牌阶段");
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
								logger.info(m_cur_round + "[固定庄家]进入庄家开牌阶段");
							}
						}
						break;
					case GAME_STEP_BANKER_OPEN_CARDS:
						{
							notifyAllGameStatus(GameStatus.GAME_STATUS_TABLE_BANKER_OPEN_CARDS);
							setGameEngineStep(GAME_STEP_OPEN_CARDS_CARTOON);
							logger.info(m_cur_round + "[固定庄家]发送庄家开牌阶段状态变化，进入比牌阶段");
						}
						break;
					case GAME_STEP_OPEN_CARDS_CARTOON:
						{
							if (m_engineTimer >= COUNT_DOWN_TIME_SECONDS_OPEN_CARDS_CARTOON)
							{
								setGameEngineStep(GAME_STEP_COMPARE_CARDS);
								logger.info(m_cur_round + "[固定庄家]比牌阶段");
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
							logger.info(m_cur_round + "[固定庄家]游戏结果");
						}
						break;	
					case GAME_STEP_FINISH:
					{
						if (Finish())
						{
							return;
						}
						setGameEngineStep(GAME_STEP_FINISH_CARTOON);
						logger.info(m_cur_round + "[固定庄家]播放金币动画");
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
							logger.info(m_cur_round + "[固定庄家]准备阶段");
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

	//确定准备完成后的下一阶段
	public void SureReadyNextStep()
	{
		if (m_roomConfig.getBaseScore() == BaseScoreType.MODE_BANKER_CHOICE)
		{//底分为庄家选择模式，通知客户端 1.庄家选择底分 2.其他玩家等待庄家选择底分
			notifyAllGameStatus(GameStatus.GAME_STATUS_TABLE_BANKER_CHOOSE_BASECOIN);
			setGameEngineStep(GAME_STEP_BANKER_CHOOSE_BASECOIN);
			logger.info(m_cur_round + "[固定庄家]准备完成，进入庄家选择底分阶段");
		}
		else
		{//底分为其他模式，进入押注阶段 通知客户端 1.进入押注阶段
			Bet();
			setGameEngineStep(GAME_STEP_BET);
			logger.info(m_cur_round + "[固定庄家]准备完成，押注阶段");
		}
	}

	@Override
	public void calcBanker()
	{
		//固定庄家，不需要计算庄家
		m_table.setPrevBankerSeatNum(m_table.getCurBankerSeatNum());
	}

}
