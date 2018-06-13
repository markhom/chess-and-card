package com.linyun.middle.common.taurus.engine;

import org.apache.log4j.Logger;

import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.common.taurus.eum.GameStatus;
import com.linyun.common.taurus.eum.specCardType;
import com.linyun.middle.common.taurus.bean.TaurusRoomConfig;
import com.linyun.middle.common.taurus.card.CardStyleMath;
import com.linyun.middle.common.taurus.card.HandCard;
import com.linyun.middle.common.taurus.player.TaurusPlayer;
import com.linyun.middle.common.taurus.protocol.TaurusProtocol;
import com.linyun.middle.common.taurus.room.TaurusRoom;
import com.linyun.middle.common.taurus.table.TaurusSeat;
import com.linyun.middle.common.taurus.table.TaurusTable;
import com.linyun.middle.common.taurus.task.GameTimer;

/** 
 * 通比牛牛 1.没有庄家 
 * @author liangbingbing 
 * */
public class TaurusAllEngine extends GameEngine
{
	private static Logger logger = LoggerFactory.getLogger(TaurusAllEngine.class);
	public TaurusAllEngine(TaurusTable table, TaurusRoom room)
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
					
					logger.info("房间" + m_room.getRoomId() + "[通比牛牛]解散成功");
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
								
								setGameEngineStep(GAME_STEP_DEAL_CARDS);
								logger.info(m_cur_round + "[通比牛牛]准备完成，进入发牌阶段");
							}
						}
						break;
					case GAME_STEP_DEAL_CARDS:
						{
							//需要通知客户端玩家的手牌数据
							DealPocker();
							notifyAllGameStatus(GameStatus.GAME_STATUS_TABLE_OPEN_CARDS);
							setGameEngineStep(GAME_STEP_OPEN_CARDS);
							logger.info(m_cur_round + "[通比牛牛]进入亮牌阶段");
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
								logger.info(m_cur_round + "[通比牛牛]进入庄家开牌阶段");
							}
						}
						break;
					case GAME_STEP_BANKER_OPEN_CARDS:
						{
							notifyAllGameStatus(GameStatus.GAME_STATUS_TABLE_BANKER_OPEN_CARDS);
							setGameEngineStep(GAME_STEP_OPEN_CARDS_CARTOON);
							logger.info(m_cur_round + "[通比牛牛]发送庄家开牌阶段状态变化，进入比牌阶段");
						}
						break;
					case GAME_STEP_OPEN_CARDS_CARTOON:
						{
							if (m_engineTimer >= COUNT_DOWN_TIME_SECONDS_OPEN_CARDS_CARTOON)
							{
								setGameEngineStep(GAME_STEP_COMPARE_CARDS);
								logger.info(m_cur_round + "[通比牛牛]播放开牌动画完毕，进入比牌阶段");
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
							logger.info(m_cur_round + "[通比牛牛]游戏结果");
						}
						break;	
					case GAME_STEP_FINISH:
					{
						if (Finish())
						{
							return;
						}
						setGameEngineStep(GAME_STEP_FINISH_CARTOON);
						logger.info(m_cur_round + "[通比牛牛]播放金币动画");
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
							logger.info(m_cur_round + "[通比牛牛]准备阶段");
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
	public void ComparePocker()
	{
		/**
		 * 计算思路 1.先找到牌最大的玩家   2.然后计算分数   
		 *    赢家赢的分数 = 其他参加游戏的玩家人数 * 赢家的牌对应赔率 * 底注分数
		 *    输家输的分数 =  赢家的牌对应赔率 * 底注分数
		 */
		HandCard maxHandCard = null;
		int position = -1;
		TaurusPlayer winPlayer = null;
		int count = 0;
		for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
		{
			TaurusSeat seat = m_table.getSeat(i);
			if (!seat.isJoinGame())
			{//没有加入游戏的玩家，跳过
				continue;
			}
			
			++count;
			if (position == -1)
			{
				position = seat.getId();
				maxHandCard = seat.getCards();
				winPlayer = seat.getPlayer();
			}
			else
			{
				if (!compareTwoHandcards(maxHandCard, seat.getCards()))
				{
					position = seat.getId();
					maxHandCard = seat.getCards();
					winPlayer = seat.getPlayer();
				}
			}
		}
		
		//赢家一局的得分情况
		int winer_score_round = (count-1)*CardStyleMath.getOddsByTimesMode(maxHandCard.getSpecType(), m_roomConfig.getTimesMode())*getBetScore(m_roomConfig.getAllCompareBaseScore().value);
		winPlayer.setScore(winer_score_round);
		winPlayer.setScoreTotal(winPlayer.getScoreTotal() + winer_score_round);
		for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
		{
			TaurusSeat seat = m_table.getSeat(i);
			if (!seat.isJoinGame())
			{//没有加入游戏的玩家，跳过
				continue;
			}
			if (seat.getId() == position)
			{
				continue;
			}
			
			TaurusPlayer player = seat.getPlayer();
			calcPlayerScore(maxHandCard.getSpecType(), player);
			player.setScoreTotal(player.getScoreTotal() + player.getScore());
		}
	}
	
	@Override
	public void calcBanker()
	{
		// 通比牛牛， 不需要确定庄家
	}
	
	/**比较两幅手牌，前者大于后者返回true，否则返回false， 不存在大小相等的两幅牌*/
	public boolean compareTwoHandcards(HandCard handCard1, HandCard handCard2) 
	{
		specCardType type1 = handCard1.getSpecType();
		specCardType type2 = handCard2.getSpecType();
		int value = type1.value - type2.value;
		if (value > 0)
		{
			return true;
		}
		else if (value < 0)
		{
			return false;
		}
		else
		{
			return CardStyleMath.compareTwoCards(CardStyleMath.getMaxCardByHandCard(handCard1), CardStyleMath.getMaxCardByHandCard(handCard2));
		}
	}
	
	public void calcPlayerScore(final specCardType winerCardType, final TaurusPlayer player)
	{
		TaurusRoomConfig config = m_roomConfig;
		int baseScore = getBetScore(m_roomConfig.getAllCompareBaseScore().value);
		if (config.getSpecConfig().isAllSmall() && (winerCardType == specCardType.CARD_TYPE_ALL_SMALL))
		{
			player.setScore(-1*baseScore*CardStyleMath.CARD_TYPE_ALL_SMALL_ODDS);
		}
		else if (config.getSpecConfig().isBomb() && (winerCardType == specCardType.CARD_TYPE_BOMB))
		{
			player.setScore(-1*baseScore*CardStyleMath.CARD_TYPE_BOMB_ODDS);
		}
		else if (config.getSpecConfig().isAllFace() && (winerCardType == specCardType.CARD_TYPE_ALL_FACE))
		{
			player.setScore(-1*baseScore*CardStyleMath.CARD_TYPE_ALL_FACE_ODDS);
		}
		else
		{
			//不是特殊牌型
			player.setScore(-1*baseScore*CardStyleMath.getOddsByTimesMode(winerCardType, config.getTimesMode()));
		}
	}
	
	private int getBetScore(int value)
	{
		switch (value)
		{
		   case 1:
			  return 1;
		   case 2:
			  return 2;
		   case 3:
			  return 4;
		   default:
			  return 1;
		}
	}
	
}
