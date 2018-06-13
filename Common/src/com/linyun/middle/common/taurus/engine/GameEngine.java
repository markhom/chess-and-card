package com.linyun.middle.common.taurus.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.linyun.bottom.cached.RedisResource;
import com.linyun.bottom.container.GameSession;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.common.entity.ClubMember;
import com.linyun.common.entity.GamePlayerLog;
import com.linyun.common.entity.GameRoundLog;
import com.linyun.common.entity.TaurusLog;
import com.linyun.common.entity.TaurusRoundLog;
import com.linyun.common.entity.User;
import com.linyun.common.taurus.eum.BankerChooseBaseScore;
import com.linyun.common.taurus.eum.BankerMode;
import com.linyun.common.taurus.eum.BaseScoreType;
import com.linyun.common.taurus.eum.GameStatus;
import com.linyun.common.taurus.eum.PlayerInjection;
import com.linyun.common.taurus.eum.RoomPayMode;
import com.linyun.common.taurus.eum.RoundNum;
import com.linyun.common.taurus.eum.UpBankerScore;
import com.linyun.common.utils.IndexUtil;
import com.linyun.middle.common.taurus.bean.TaurusRoomConfig;
import com.linyun.middle.common.taurus.card.CardStyleMath;
import com.linyun.middle.common.taurus.card.Cards;
import com.linyun.middle.common.taurus.card.HandCard;
import com.linyun.middle.common.taurus.club.BaseClubServer;
import com.linyun.middle.common.taurus.club.TaurusClub;
import com.linyun.middle.common.taurus.club.TaurusClubMember;
import com.linyun.middle.common.taurus.manager.EngineManager;
import com.linyun.middle.common.taurus.manager.RoomManager;
import com.linyun.middle.common.taurus.manager.TableManager;
import com.linyun.middle.common.taurus.player.TaurusPlayer;
import com.linyun.middle.common.taurus.protocol.TaurusProtocol;
import com.linyun.middle.common.taurus.room.TaurusRoom;
import com.linyun.middle.common.taurus.server.ActionAware;
import com.linyun.middle.common.taurus.service.BaseServer;
import com.linyun.middle.common.taurus.table.TaurusSeat;
import com.linyun.middle.common.taurus.table.TaurusTable;
import com.linyun.middle.common.taurus.task.GameTimer;
import com.linyun.middle.common.taurus.utils.BetUtils;
import com.linyun.middle.common.taurus.utils.HandCardComparator;
import com.linyun.middle.common.taurus.utils.TaurusEngineMathUtils;
import com.linyun.middle.common.taurus.utils.Utils;


public abstract class GameEngine
{
	private static Logger logger = LoggerFactory.getLogger(GameEngine.class);
	
	public static final int BARRACAT_ONEHANDCARD_NUMS = 2;
	public static final String GAME_ENGINE = "GameEngine";
	//------------------------------------倒计时时间限定-------------------------------------------
	protected static final int COUNT_DOWN_TIME_SECONDS_READY = 1000*10; //准备倒计时时间，单位：秒
	protected static final int COUNT_DOWN_TIME_SECONDS_BANKER_SURE_CARTOON = 1000*2; //庄家确定播动画的时间，单位：秒
	protected static final int COUNT_DOWN_TIME_SECONDS_BANKER_CHOOSE_BASECOIN = 1000*5; //庄家选择底分倒计时时间，单位：秒
	protected static final int COUNT_DOWN_TIME_SECONDS_BET = 1000*10; //押注倒计时时间，单位：秒
	protected static final int COUNT_DOWN_TIME_SECONDS_OPEN_CARDS = 1000*15; //亮牌倒计时时间，单位：秒
	protected static final int COUNT_DOWN_TIME_SECONDS_OPEN_CARDS_CARTOON = 1000*2; //分数结算倒计时，单位：秒
	protected static final int COUNT_DOWN_TIME_SECONDS_GAME_FINISH_CARTOON = 1000*4; //分数结算倒计时，单位：秒
	
	protected static final int COUNT_DOWN_TIME_SECONDS_AUTO_ACTION = 1000*5;//托管后，自动动作的时间
	
	protected static final int COUNT_DOWN_TIME_SECONDS_DISSOLUTION_ROOM = 1000*30;//有玩家提出解散申请，没有表决出结果，三十秒后解散房间
	
	protected static final int COUNT_DOWN_TIME_SECONDS_KEEPSEAT = 1000*60; //新版H5中玩家中途积分不足时，保座离桌倒计时
	//------------------------------------end-------------------------------------------
	
	//------------------------------------游戏流程步骤-----------------------------------------
	protected static final int GAME_STEP_BEGIN = 0; //游戏开始
	protected static final int GAME_STEP_READY = 1; //准备阶段
	protected static final int GAME_STEP_BANKER_SURE = 2; //庄家确定
	protected static final int GAME_STEP_BANKER_SURE_CARTTON = 3; //庄家确定之后播动画的时间
	protected static final int GAME_STEP_BANKER_CHOOSE_BASECOIN = 4; //庄家选择底分阶段，创建房间时如果底分选项选的是庄家选择，则有这一步，否则没有这一步
	protected static final int GAME_STEP_BET = 5; //押注阶段
	protected static final int GAME_STEP_DEAL_CARDS = 6; //发牌阶段
	protected static final int GAME_STEP_OPEN_CARDS = 7; //亮牌阶段
	protected static final int GAME_STEP_BANKER_OPEN_CARDS = 8; //亮牌阶段
	protected static final int GAME_STEP_OPEN_CARDS_CARTOON = 9; //开牌动画播放
	protected static final int GAME_STEP_COMPARE_CARDS = 10; //比牌
	protected static final int GAME_STEP_FINISH = 11; //判断整场牌局是否结束
	protected static final int GAME_STEP_FINISH_CARTOON = 12; //金币动画播放
	protected static final int GAME_STEP_END = 100; //游戏结束
	//------------------------------------end-----------------------------------------
	
	protected static final int SEAT_NUM = 6; //座位数
	/** 桌子 */
	protected TaurusTable m_table;
	protected TaurusRoom m_room;
	protected String m_engineId;
	protected TaurusRoomConfig m_roomConfig;
	protected volatile int m_cur_round;//当前局数
	protected TaurusLog m_gameLog;
	protected boolean oneRoundIsEnd;
	protected ActionAware m_action;
	protected boolean isClubCard;
	
	protected boolean isNewH5 ;//是否为新版H5
	
	/**
	 * 请勿单独设置m_step的值，所有设置m_step的值请使用setGameEngineStep
	 * */
	protected int m_engineTimer = 0; //引擎计时器
	protected volatile int m_step;
	
	protected int m_gameTimer = 0;
	//上一局的索引号，在断线重连的时候的上局回顾功能里面用到
	protected String m_prevRoundIndex;
	protected String[] m_roundIndex;
	
	public GameEngine(TaurusTable table, TaurusRoom room)
	{
		this.m_table = table;
		this.m_engineId = String.valueOf(table.getTableId());
		this.m_room = room ;
		this.m_roomConfig = room.getConfig();
		this.m_cur_round = 1;
		this.m_engineTimer = 0;
		this.m_step = GAME_STEP_READY;
		
		this.m_gameLog = new TaurusLog();
		this.oneRoundIsEnd = false;
		this.m_action = new ActionAware();
		this.isNewH5 = this.m_roomConfig.getGameTime() == 0 ? false : true;
		this.isClubCard = this.isNewH5;
		//根据总局数，生成局号索引
		if(!isNewH5)
		{
		   this.m_roundIndex = IndexUtil.getInstance().getDiffRoundIndex(m_roomConfig.getRoundNum(), room.getRoomId());
		   this.m_table.setRoundIndex(m_roundIndex);
		}
		this.m_gameTimer = 0;
	}
	
	public GameEngine(TaurusTable table, TaurusRoom room, boolean isClubCard)
	{
		this.m_table = table;
		this.m_engineId = String.valueOf(table.getTableId());
		this.m_room = room ;
		this.m_roomConfig = room.getConfig();
		this.m_cur_round = 1;
		this.m_engineTimer = 0;
		this.m_step = GAME_STEP_READY;
		
		this.m_gameLog = new TaurusLog();
		this.oneRoundIsEnd = false;
		this.m_action = new ActionAware();
		this.isClubCard = isClubCard;
		this.isNewH5 = this.m_roomConfig.getGameTime()==0 ? false : true;
		//根据总局数，生成局号索引
		if(!isNewH5)
		{
		  this.m_roundIndex = IndexUtil.getInstance().getDiffRoundIndex(m_roomConfig.getRoundNum(), room.getRoomId());
		  this.m_table.setRoundIndex(m_roundIndex);
		}
		this.m_gameTimer = 0;
	}
	
	public void InitEngine(TaurusTable table, TaurusRoom room)
	{
		this.m_table = table;
		this.m_engineId = String.valueOf(table.getTableId());
		this.m_room = room ;
		this.m_roomConfig = room.getConfig();
		this.m_cur_round = 1;
		this.m_engineTimer = 0;
		this.m_step = GAME_STEP_READY;
		
		this.m_gameLog = new TaurusLog();
		this.oneRoundIsEnd = false;
		this.m_action = new ActionAware();
		
		this.isNewH5 = this.m_roomConfig.getGameTime()==0 ? false : true;
		this.isClubCard = this.isNewH5;
		this.m_gameTimer = 0;
		if(!isNewH5)
		{
		 //根据总局数，生成局号索引
		 this.m_roundIndex = IndexUtil.getInstance().getDiffRoundIndex(m_roomConfig.getRoundNum(), room.getRoomId());
		 this.m_table.setRoundIndex(m_roundIndex);
		}
	}
	
	public void InitEngine(TaurusTable table, TaurusRoom room, boolean isClubCard)
	{
		this.m_table = table;
		this.m_engineId = String.valueOf(table.getTableId());
		this.m_room = room ;
		this.m_roomConfig = room.getConfig();
		this.m_cur_round = 1;
		this.m_engineTimer = 0;
		this.m_step = GAME_STEP_READY;
		
		this.m_gameLog = new TaurusLog();
		this.oneRoundIsEnd = false;
		this.m_action = new ActionAware();
		this.isClubCard = isClubCard;
		this.isNewH5 = this.m_roomConfig.getGameTime()==0 ? false : true; 
		this.m_gameTimer = 0;
		if(!isNewH5)
		{
		 //根据总局数，生成局号索引
		 this.m_roundIndex = IndexUtil.getInstance().getDiffRoundIndex(m_roomConfig.getRoundNum(), room.getRoomId());
		 this.m_table.setRoundIndex(m_roundIndex);
		}
	}
	
	public String getEngineId()
	{
		return m_engineId;
	}
	
	/**
	 * 游戏主线程 --- 轮庄牛牛/牛牛上庄 可以公用，抢庄模式/通比牛牛/固定庄家 需要覆盖此方法
	 */
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
					
					logger.info("房间" + m_room.getRoomId() + "[公共方法]解散成功");
					
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
					 logger.info("游戏中止时引擎所在的阶段为："+m_step);
					 notifyAllGameStatus(GameStatus.GAME_STATUS_PAUSE);
					 m_table.setCurBankerSeatNum(-1);//将当前庄家位置置为0，重新随机确定庄家
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
								
								setGameEngineStep(GAME_STEP_BANKER_SURE);
								m_room.setGameStatus(GameStatus.GAME_STATUS_TABLE_ROB_BANKER);
								logger.info(m_cur_round + "准备完成，进入庄家确定阶段");
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
								logger.info(m_cur_round + "押注阶段");
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
								logger.info(m_cur_round + "发牌阶段");
							}
						}
						break;
					case GAME_STEP_DEAL_CARDS:
						{
							//需要通知客户端玩家的手牌数据
							DealPocker();
							notifyAllGameStatus(GameStatus.GAME_STATUS_TABLE_OPEN_CARDS);
							setGameEngineStep(GAME_STEP_OPEN_CARDS);
							logger.info(m_cur_round + "比牌阶段");
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
								logger.info(m_cur_round + "进入庄家开牌阶段");
							}
						}
						break;
					case GAME_STEP_BANKER_OPEN_CARDS:
						{
							notifyAllGameStatus(GameStatus.GAME_STATUS_TABLE_BANKER_OPEN_CARDS);
							setGameEngineStep(GAME_STEP_OPEN_CARDS_CARTOON);
							logger.info(m_cur_round + "发送庄家开牌阶段状态变化，进入比牌阶段");
						}
						break;
					case GAME_STEP_OPEN_CARDS_CARTOON:
						{
							if (m_engineTimer >= COUNT_DOWN_TIME_SECONDS_OPEN_CARDS_CARTOON)
							{
								setGameEngineStep(GAME_STEP_COMPARE_CARDS);
								logger.info(m_cur_round + "比牌阶段");
							}
							
						}
						break;
					case GAME_STEP_COMPARE_CARDS:
						{
							//比牌计算结果，需要通知客户端计算结果相关
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
							logger.info(m_cur_round + "游戏结果");
						}
						break;	
					case GAME_STEP_FINISH:
					{
						if (Finish())
						{ 
							return;
						}
						setGameEngineStep(GAME_STEP_FINISH_CARTOON);
						logger.info(m_cur_round + "播放金币动画");
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
							logger.info(m_cur_round + "准备阶段");
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
	
	protected void handleUserTableNum()
	{
		TaurusSeat[] seats = m_table.getSeats();
		synchronized (seats)
		{
			for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
			{
				if (!seats[i].isJoinGame())
				{
					continue;
				}
				TaurusPlayer player = seats[i].getPlayer();
				if(player != null)
				{
					String playerId = player.getPlayerId();
					m_action.userAction().updateUserTableNum(playerId, String.valueOf(m_room.getRoomId()));
				}
				
			}
		}
	}
    protected void returnClubMemberCoin()
    {
    	//解散房间后将积分返回俱乐部成员账户
		for (int i=0; i<SEAT_NUM; ++i)
		{
			TaurusSeat seat = m_table.getSeat(i);
			if (seat.isCanSitDown())
			{
				continue;
			}
		    TaurusPlayer player = seat.getPlayer();
		    if(player != null)
		    {
		        int playerId = Integer.valueOf(player.getPlayerId());
				TaurusClub club = BaseClubServer.getClub(m_room.getClubId());
				TaurusClubMember member = club.getMember(playerId);
				    
				if(player.getScoreTotal()>0)
				{
					m_action.clubMemberAction().updateClubMemberCurrentScore(m_room.getClubId(), playerId, member.getCurrentScore()+ player.getScoreTotal());
				    member.setCurrentScore(member.getCurrentScore()+ player.getScoreTotal());
				}
		    }
		   
		}
    }
	protected void handleKeepSeatExistTable()
	{
		if ((m_cur_round>1) || oneRoundIsEnd) 
		{
			TaurusSeat[] seats = m_table.getSeats();
			for(int i=0; i<seats.length; ++i)
			{
				if(seats[i].isKeepSeatStage())
				{
				   seats[i].addKeepSeatTimerCount(GameTimer.DEFAULT_TIME);
				   
				   if(seats[i].getKeepSeatTimer()>= COUNT_DOWN_TIME_SECONDS_KEEPSEAT)
					{
					    TaurusPlayer player = seats[i].getPlayer();
					    String playerId	=player.getPlayerId();
					    if(player.getScoreTotal()>0)
					    {
					    	TaurusClub club = BaseClubServer.getClub(m_room.getClubId());
							TaurusClubMember member = club.getMember(Integer.valueOf(playerId));
						    m_action.clubMemberAction().updateClubMemberCurrentScore(m_room.getClubId(),Integer.valueOf(playerId), member.getCurrentScore()+player.getScoreTotal());
							member.setCurrentScore(member.getCurrentScore()+player.getScoreTotal());
					    }
						m_action.roomAction().subOnePlayer(Integer.valueOf(m_table.getTableId()));
						m_table.removePlayer(playerId);
						BaseServer.unbindUserTable(playerId);
					
						OutputMessage om = new OutputMessage(true);
						om.putString(playerId);
						om.putByte((byte)seats[i].getId());
						m_room.sendMessage(TaurusProtocol.PROTOCOL_Ser_Table_Exit, om);
						
					}
				}
				
			}
		}
		
	}
	/**
	 * 押注  --- 公用方法
	 * */
	protected void Bet( )
	{
		m_room.setGameStatus(GameStatus.GAME_STATUS_TABLE_BET);
		//庄家默认已经押注
		if (m_cur_round != 0)
		{
			m_table.getSeat(m_table.getCurBankerSeatNum()-1).bet();
		}
		
		String bankerId = m_table.getSeat(m_table.getCurBankerSeatNum()-1).getPlayer().getPlayerId();
		OutputMessage om;
		if ((m_roomConfig.getBaseScore() == BaseScoreType.MODE_BANKER_CHOICE) || (m_roomConfig.getPlayerInjection() == PlayerInjection.PLAYER_INJECTION_NONE))
		{//1.庄家选择底分模式 或者 2.非庄家选择底分模式 2-1.未开启闲家推注模式
			om = TaurusEngineMathUtils.getBetOM(m_cur_round, m_roomConfig, m_table.getChooseBaseCoin(), m_roomConfig.getPlayerInjection());
			m_table.sendMessage(TaurusProtocol.PROTOCOL_Ser_Game_Status_Changed, om);
		}
		else
		{//2-2 开启闲家推注模式
			int injectionScore = 0;
			for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
			{
				TaurusSeat seat = m_table.getSeat(i);
				if (!seat.isJoinGame())
				{//未加入游戏的位置跳过
					continue;
				}
				TaurusPlayer player = seat.getPlayer();
				//庄家跳过
				if (player.getPlayerId().equals(bankerId))
				{
					
					GameSession session = m_room.getSessionByUserId(bankerId); 
					if (session!= null)
					{
						OutputMessage banker_om = new OutputMessage(true);
						banker_om.putInt(this.m_cur_round);
						banker_om.putByte(GameStatus.GAME_STATUS_TABLE_BET.value);
						session.sendMessage(TaurusProtocol.PROTOCOL_Ser_Game_Status_Changed, banker_om);
					}
					continue;
				}
				om = new OutputMessage(true);
				om.putInt(this.m_cur_round);
				om.putByte(GameStatus.GAME_STATUS_TABLE_BET.value);
				
				logger.info("玩家上一局得分："+ player.getLastRoundScore() + "上局推注情况" + seat.isLastRoundInjection() + "上局是否庄家" + seat.isLastRoundBanker());
				if ((player.getLastRoundScore() > 0) && (!seat.isLastRoundInjection()) && (!seat.isLastRoundBanker()))
				{//1.不是庄家，是闲家 2. 上一局赢了 3.并且上一局没有推注 4.不是上局庄家
					//最大推注分数 
					int maxInjectionScore = TaurusEngineMathUtils.getMaxInjectionScore(m_roomConfig.getBaseScore(), m_roomConfig);
					//玩家上局赢的分数加上底分的最大值
					int playerInjectionScore = TaurusEngineMathUtils.getMaxBaseScore(m_roomConfig) + player.getLastRoundScore();
					//取上面两者中的最小值
					injectionScore = (playerInjectionScore>maxInjectionScore)?maxInjectionScore:playerInjectionScore;
					//设置玩家当前局的可推注分数
					seat.setCurRoundInjectionScore(injectionScore);
					om.putByte((byte)3);//推注个数
					logger.info("player" + player.getPlayerId() + "闲家推注，发送筹码个数3, injectionScore = " + injectionScore);
					TaurusEngineMathUtils.setBaseScoreOM(om, m_roomConfig);
					om.putInt(injectionScore);
				}
				else
				{
					logger.info("player" + player.getPlayerId() + "非闲家推注，发送筹码个数2");
					om.putByte((byte)2);
					TaurusEngineMathUtils.setBaseScoreOM(om, m_roomConfig);
				}
				GameSession session = m_room.getSessionByUserId(player.getPlayerId()); 
				if (session!= null)
				{
					session.sendMessage(TaurusProtocol.PROTOCOL_Ser_Game_Status_Changed, om);
				}
				
				if (seat.isLastRoundInjection())
				{
					seat.setLastRoundInjection(false);
				}
			}
			
		}
	}
	
	//结算一局相关
	void concludeOneRound()
	{
		if(isNewH5)
		{
			concludeOneRoundScoreForNewH5();
		}else
		{
			concludeOneRoundScore();
		}
		
		concludeOneRoundPay();
		calcBanker();
		this.oneRoundIsEnd = true;
	}
	
	/**
	 * 发牌 -- 公用方法      明牌抢庄需要覆盖此方法
	 * */
	protected void DealPocker()
	{
		//得到六副手牌
		HandCard[] handCards = Cards.getAllHandCard(String.valueOf(m_room.getRoomId()));
		GameSession session = null;
		
		List<String> userIdList = new ArrayList<String>();
		for (int i=0; i<SEAT_NUM; ++i)
		{//需要注意玩家分散坐的情况
			TaurusSeat seat = m_table.getSeat(i);
			if (!seat.isJoinGame())
			{//未加入游戏的玩家
				continue;
			}
			
			userIdList.add(seat.getPlayer().getPlayerId());
			seat.getCards().setCards(handCards[i].getCards());//设置玩家的牌信息
			seat.getCards().calcCardType(m_roomConfig.getSpecConfig());//计算牌的类型
			
			logger.info("locationId:"+i + ",cards:" + seat.getCards().toString());
			
			session = m_room.getSessionByUserId(seat.getPlayer().getPlayerId());
			if (session != null)
			{
				OutputMessage om = new OutputMessage(true);
				om.putShort(seat.getCards().getCards()[0]);
				om.putShort(seat.getCards().getCards()[1]);
				om.putShort(seat.getCards().getCards()[2]);
				om.putShort(seat.getCards().getCards()[3]);
				om.putShort(seat.getCards().getCards()[4]);
				om.putByte(seat.getCards().getSpecType().value);
				//通知客户端玩家自己的牌信息
				session.sendMessage(TaurusProtocol.PROTOCOL_Ser_Deal_Cards, om);
			}
		}

		m_room.sendMessage(TaurusProtocol.PROTOCOL_Ser_Deal_Cards_Watch, new OutputMessage(true), userIdList);
	}
	
	/**计算一局庄家*/
	public abstract void calcBanker();
	
	/**
	 * 结算一局分数相关<记录数据库，通知客户端得分情况> --- 公用方法
	 * */
	protected void concludeOneRoundScoreForNewH5()
	{//结算一局分数相关，通知客户端，记录数据库
		/** 先找出参加游戏的玩家，然后再将其数据发给客户端  */
		List<TaurusPlayer> playerList = new ArrayList<TaurusPlayer>();
		for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
		{
			TaurusSeat seat = m_table.getSeat(i);
			if (!seat.isJoinGame())
			{//空位置 跳过
				continue;
			}
			playerList.add(seat.getPlayer());
		}
		OutputMessage om = new OutputMessage(true);
		int playerCount = playerList.size();
		om.putByte((byte)playerCount);
		
		for (int i=0; i<playerCount; ++i)
		{
			TaurusPlayer player = playerList.get(i);
			
			om.putString(player.getPlayerId());
			om.putString(String.valueOf(player.getScoreTotal()));
			om.putString(String.valueOf(player.getScore()));
		}
		m_room.sendMessage(TaurusProtocol.PROTOCOL_Ser_Compare_Cards_Result, om);
		
		//游戏记录----------------------------------------------------
		TaurusPlayer player = null;
		User user = null;
		int roomNum = m_room.getRoomId();
		m_action.clubRoomLogAction().updatePlayedRound(roomNum, m_cur_round);
		
		//刷新每个玩家的积分
		for(int i=0;i<TaurusTable.TABLE_SEAT_NUM; ++i)
		{
			if (m_table.getSeat(i).isJoinGame())
			{
				player = m_table.getSeat(i).getPlayer();
				int playerId = Integer.parseInt(player.getPlayerId());
				
				m_action.gamePlayerLogAction().updatePlayerLog(player.getScoreTotal(),player.getScore(), m_room.getClubId(), roomNum, playerId);
			}
				
		}
		//推送每局的战绩结果给前端
		List<GamePlayerLog> allPlayerLog = m_action.gamePlayerLogAction().getAllPlayerLog(m_room.getClubId(), roomNum);
		OutputMessage result =new OutputMessage(true);
		result.putInt(allPlayerLog.size());
		for(GamePlayerLog g : allPlayerLog)
		{
			result.putString(g.getNickName());
			result.putInt(g.getBuyScore());
			result.putInt(g.getResult());
		}
		m_room.sendMessage(TaurusProtocol.PROTOCOL_Ser_Game_Log_Send, result);
		
		//-----------------------------一局详情记录
		GameRoundLog roundLog = new GameRoundLog();
		roundLog.setClubId(m_room.getClubId());
		roundLog.setRoomNum(roomNum);
		roundLog.setRound(m_cur_round);
		if (m_roomConfig.getBankerMode() != BankerMode.BANKER_MODE_ALL_COMPARE)
		{
			roundLog.setBankerId(m_table.getSeat(m_table.getCurBankerSeatNum()-1).getPlayer().getPlayerId());
		}
		
		HandCard handCard = null;
		TaurusSeat seat = null;
		if (m_table.getSeat(0).isJoinGame())
		{
			seat = m_table.getSeat(0);
			player = seat.getPlayer();
			handCard = seat.getCards();
			roundLog.setPlayer1(player.getPlayerId());
			roundLog.setCards1(handCard.toString());
			roundLog.setCardType1(handCard.getSpecType().value);
			roundLog.setBaseScore1(player.getBetCoin());
			roundLog.setGetScore1(player.getScore());
			roundLog.setRobBankerNum1((byte)player.getRobBankerNum());
			roundLog.setIsRobBanker1((byte)(player.isRobBanker()?1:0));
			roundLog.setGetScoreTotal1(player.getScoreTotal());
		}
		if (m_table.getSeat(1).isJoinGame())
		{
			seat = m_table.getSeat(1);
			player = seat.getPlayer();
			handCard = seat.getCards();
			roundLog.setPlayer2(player.getPlayerId());
			roundLog.setCards2(handCard.toString());
			roundLog.setCardType2(handCard.getSpecType().value);
			roundLog.setBaseScore2(player.getBetCoin());
			roundLog.setGetScore2(player.getScore());
			roundLog.setRobBankerNum2((byte)player.getRobBankerNum());
			roundLog.setIsRobBanker2((byte)(player.isRobBanker()?1:0));
			roundLog.setGetScoreTotal2(player.getScoreTotal());
		}
		if (m_table.getSeat(2).isJoinGame())
		{
			seat = m_table.getSeat(2);
			player = seat.getPlayer();
			handCard = seat.getCards();
			roundLog.setPlayer3(player.getPlayerId());
			roundLog.setCards3(handCard.toString());
			roundLog.setCardType3(handCard.getSpecType().value);
			roundLog.setBaseScore3(player.getBetCoin());
			roundLog.setGetScore3(player.getScore());
			roundLog.setRobBankerNum3((byte)player.getRobBankerNum());
			roundLog.setIsRobBanker3((byte)(player.isRobBanker()?1:0));
			roundLog.setGetScoreTotal3(player.getScoreTotal());
		}
		if (m_table.getSeat(3).isJoinGame())
		{
			seat = m_table.getSeat(3);
			player = seat.getPlayer();
			handCard = seat.getCards();
			roundLog.setPlayer4(player.getPlayerId());
			roundLog.setCards4(handCard.toString());
			roundLog.setCardType4(handCard.getSpecType().value);
			roundLog.setBaseScore4(player.getBetCoin());
			roundLog.setGetScore4(player.getScore());
			roundLog.setRobBankerNum4((byte)player.getRobBankerNum());
			roundLog.setIsRobBanker4((byte)(player.isRobBanker()?1:0));
			roundLog.setGetScoreTotal4(player.getScoreTotal());
		}
		if (m_table.getSeat(4).isJoinGame())
		{
			seat = m_table.getSeat(4);
			player = seat.getPlayer();
			handCard = seat.getCards();
			roundLog.setPlayer5(player.getPlayerId());
			roundLog.setCards5(handCard.toString());
			roundLog.setCardType5(handCard.getSpecType().value);
			roundLog.setBaseScore5(player.getBetCoin());
			roundLog.setGetScore5(player.getScore());
			roundLog.setRobBankerNum5((byte)player.getRobBankerNum());
			roundLog.setIsRobBanker5((byte)(player.isRobBanker()?1:0));
			roundLog.setGetScoreTotal5(player.getScoreTotal());
		}
		if (m_table.getSeat(5).isJoinGame())
		{
			seat = m_table.getSeat(5);
			player = seat.getPlayer();
			handCard = seat.getCards();
			roundLog.setPlayer6(player.getPlayerId());
			roundLog.setCards6(handCard.toString());
			roundLog.setCardType6(handCard.getSpecType().value);
			roundLog.setBaseScore6(player.getBetCoin());
			roundLog.setGetScore6(player.getScore());
			roundLog.setRobBankerNum6((byte)player.getRobBankerNum());
			roundLog.setIsRobBanker6((byte)(player.isRobBanker()?1:0));
			roundLog.setGetScoreTotal6(player.getScoreTotal());
		}
		//增加一局游戏的详细记录
		m_action.gameRoundLogAction().addGameRoundLogAction(roundLog);
		//-----------
		
		//--增加用户表 用户局数加一
		for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
		{
			if (m_table.getSeat(i).isJoinGame())
			{
				user = m_table.getSeat(i).getUser();
				user.setRoundNum(user.getRoundNum()+1);
				m_action.userAction().addUserOneRound(user.getUserId().toString());
			}
		}
		//推送保座离桌玩家信息
		for(int i=0; i<playerCount; ++i)
		{
			TaurusPlayer player2 = playerList.get(i);
			
			if(player2.getScoreTotal()< m_roomConfig.getClubRoomBaseScore()*2)
			{
				OutputMessage os = new OutputMessage(true);
				os.putString(player2.getPlayerId());
				int playerId = Integer.parseInt(player2.getPlayerId());
				ClubMember member = m_action.clubMemberAction().getClubMemberByClubId(m_room.getClubId(), playerId);
				os.putInt(member.getCurrentScore());
				GamePlayerLog playerLog = m_action.gamePlayerLogAction().getPlayerLogById(m_room.getClubId(), m_room.getRoomId(), playerId);
				os.putInt(playerLog.getBuyScore());
				m_room.sendMessage(TaurusProtocol.PROTOCOL_Ser_ReserveSeat, os);
				
				TaurusSeat seat2 = m_table.getUserSeat(player2.getPlayerId());
				seat2.setKeepSeatStage(true);
				seat2.setJoinGame(false);
				
			}
		}
		
		
	}
	/**
	 * 结算一局分数相关<记录数据库，通知客户端得分情况> --- 公用方法
	 * */
	protected void concludeOneRoundScore()
	{//结算一局分数相关，通知客户端，记录数据库
		/** 先找出参加游戏的玩家，然后再将其数据发给客户端  */
		List<TaurusPlayer> playerList = new ArrayList<TaurusPlayer>();
		for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
		{
			TaurusSeat seat = m_table.getSeat(i);
			if (!seat.isJoinGame())
			{//空位置 跳过
				continue;
			}
			playerList.add(seat.getPlayer());
		}
		OutputMessage om = new OutputMessage(true);
		int playerCount = playerList.size();
		om.putByte((byte)playerCount);
		
		for (int i=0; i<playerCount; ++i)
		{
			TaurusPlayer player = playerList.get(i);
			om.putString(player.getPlayerId());
			om.putString(String.valueOf(player.getScoreTotal()));
			om.putString(String.valueOf(player.getScore()));
		}
		m_room.sendMessage(TaurusProtocol.PROTOCOL_Ser_Compare_Cards_Result, om);
		
		//游戏记录----------------------------------------------------
		TaurusPlayer player = null;
		User user = null;
		int roomNum = m_room.getRoomId();
		m_gameLog.setPlayedRound(m_cur_round);
		if (m_cur_round == 1)
		{//第一局结束，增加游戏记录信息
			int clubId = m_room.getClubId();//俱乐部Id,好友场是0，俱乐部游戏时是俱乐部Id
			m_gameLog.setClubId(clubId);
			m_gameLog.setRoomNum(m_room.getRoomId());
			m_gameLog.setRoundTotal(m_roomConfig.getRoundNum());
			m_gameLog.setBankerMode(m_roomConfig.getBankerMode().value);
			m_gameLog.setBaseScore(m_roomConfig.getBaseScore().value);
			m_gameLog.setAllCompareBaseScore(m_roomConfig.getAllCompareBaseScore().value);
			m_gameLog.setRoomOwnerId(m_room.getRoomOwnerId());
			m_gameLog.setPayMode(m_roomConfig.getPayMode().value);
			if (m_table.getSeat(0).isJoinGame())
			{
				player = m_table.getSeat(0).getPlayer();
				m_gameLog.setPlayerId1(player.getPlayerId());
				m_gameLog.setScore1(player.getScoreTotal());
				user = m_table.getSeat(0).getUser();
				m_gameLog.setHeadImgUrl1(user.getHeadImgUrl());
				m_gameLog.setNickName1(user.getNickName());
			}
			if (m_table.getSeat(1).isJoinGame())
			{
				player = m_table.getSeat(1).getPlayer();
				m_gameLog.setPlayerId2(player.getPlayerId());
				m_gameLog.setScore2(player.getScoreTotal());
				user = m_table.getSeat(1).getUser();
				m_gameLog.setHeadImgUrl2(user.getHeadImgUrl());
				m_gameLog.setNickName2(user.getNickName());
			}
			if (m_table.getSeat(2).isJoinGame())
			{
				player = m_table.getSeat(2).getPlayer();
				m_gameLog.setPlayerId3(player.getPlayerId());
				m_gameLog.setScore3(player.getScoreTotal());
				user = m_table.getSeat(2).getUser();
				m_gameLog.setHeadImgUrl3(user.getHeadImgUrl());
				m_gameLog.setNickName3(user.getNickName());
			}
			if (m_table.getSeat(3).isJoinGame())
			{
				player = m_table.getSeat(3).getPlayer();
				m_gameLog.setPlayerId4(player.getPlayerId());
				m_gameLog.setScore4(player.getScoreTotal());
				user = m_table.getSeat(3).getUser();
				m_gameLog.setHeadImgUrl4(user.getHeadImgUrl());
				m_gameLog.setNickName4(user.getNickName());
			}
			if (m_table.getSeat(4).isJoinGame())
			{
				player = m_table.getSeat(4).getPlayer();
				m_gameLog.setPlayerId5(player.getPlayerId());
				m_gameLog.setScore5(player.getScoreTotal());
				user = m_table.getSeat(4).getUser();
				m_gameLog.setHeadImgUrl5(user.getHeadImgUrl());
				m_gameLog.setNickName5(user.getNickName());
			}
			if (m_table.getSeat(5).isJoinGame())
			{
				player = m_table.getSeat(5).getPlayer();
				m_gameLog.setPlayerId6(player.getPlayerId());
				m_gameLog.setScore6(player.getScoreTotal());
				user = m_table.getSeat(5).getUser();
				m_gameLog.setHeadImgUrl6(user.getHeadImgUrl());
				m_gameLog.setNickName6(user.getNickName());
			}
			m_gameLog.setRoundIndex1(m_roundIndex[0]);	
			//增加第一局的
			m_action.taurusLogAction().addGameRecord(m_gameLog);
			
			TaurusLog logTemp = m_action.taurusLogAction().getOneRecord(m_room.getRoomId());
			m_gameLog.setId(logTemp.getId());
		}
		else
		{//非第一局，更新
			switch (m_cur_round)
			{
			case 2:	m_gameLog.setRoundIndex2(m_roundIndex[m_cur_round-1]);	break;
			case 3:	m_gameLog.setRoundIndex3(m_roundIndex[m_cur_round-1]);	break;
			case 4:	m_gameLog.setRoundIndex4(m_roundIndex[m_cur_round-1]);	break;
			case 5:	m_gameLog.setRoundIndex5(m_roundIndex[m_cur_round-1]);	break;
			case 6:	m_gameLog.setRoundIndex6(m_roundIndex[m_cur_round-1]);	break;
			case 7:	m_gameLog.setRoundIndex7(m_roundIndex[m_cur_round-1]);	break;
			case 8:	m_gameLog.setRoundIndex8(m_roundIndex[m_cur_round-1]);	break;
			case 9:	m_gameLog.setRoundIndex9(m_roundIndex[m_cur_round-1]);	break;
			case 10:m_gameLog.setRoundIndex10(m_roundIndex[m_cur_round-1]);	break;
			case 11:m_gameLog.setRoundIndex11(m_roundIndex[m_cur_round-1]);	break;
			case 12:m_gameLog.setRoundIndex12(m_roundIndex[m_cur_round-1]);	break;
			case 13:m_gameLog.setRoundIndex13(m_roundIndex[m_cur_round-1]);	break;
			case 14:m_gameLog.setRoundIndex14(m_roundIndex[m_cur_round-1]);	break;
			case 15:m_gameLog.setRoundIndex15(m_roundIndex[m_cur_round-1]);	break;
			case 16:m_gameLog.setRoundIndex16(m_roundIndex[m_cur_round-1]);	break;
			case 17:m_gameLog.setRoundIndex17(m_roundIndex[m_cur_round-1]);	break;
			case 18:m_gameLog.setRoundIndex18(m_roundIndex[m_cur_round-1]);	break;
			case 19:m_gameLog.setRoundIndex19(m_roundIndex[m_cur_round-1]);	break;
			case 20:m_gameLog.setRoundIndex20(m_roundIndex[m_cur_round-1]);	break;
			default:	break;
			}
			
			if (m_table.getSeat(0).isJoinGame())
			{
				player = m_table.getSeat(0).getPlayer();
				if (m_gameLog.getPlayerId1() == null)
				{
					m_gameLog.setPlayerId1(player.getPlayerId());
					user = m_table.getSeat(0).getUser();
					m_gameLog.setHeadImgUrl1(user.getHeadImgUrl());
					m_gameLog.setNickName1(user.getNickName());
				}
				m_gameLog.setScore1(player.getScoreTotal());
			}
			if (m_table.getSeat(1).isJoinGame())
			{
				player = m_table.getSeat(1).getPlayer();
				if (m_gameLog.getPlayerId2() == null)
				{
					m_gameLog.setPlayerId2(player.getPlayerId());
					user = m_table.getSeat(1).getUser();
					m_gameLog.setHeadImgUrl2(user.getHeadImgUrl());
					m_gameLog.setNickName2(user.getNickName());
				}
				m_gameLog.setScore2(player.getScoreTotal());
			}
			if (m_table.getSeat(2).isJoinGame())
			{
				player = m_table.getSeat(2).getPlayer();
				if (m_gameLog.getPlayerId3() == null)
				{
					m_gameLog.setPlayerId3(player.getPlayerId());
					user = m_table.getSeat(2).getUser();
					m_gameLog.setHeadImgUrl3(user.getHeadImgUrl());
					m_gameLog.setNickName3(user.getNickName());
				}
				m_gameLog.setScore3(player.getScoreTotal());
			}
			if (m_table.getSeat(3).isJoinGame())
			{
				player = m_table.getSeat(3).getPlayer();
				if (m_gameLog.getPlayerId4() == null)
				{
					m_gameLog.setPlayerId4(player.getPlayerId());
					user = m_table.getSeat(3).getUser();
					m_gameLog.setHeadImgUrl4(user.getHeadImgUrl());
					m_gameLog.setNickName4(user.getNickName());
				}
				m_gameLog.setScore4(player.getScoreTotal());
			}
			if (m_table.getSeat(4).isJoinGame())
			{
				player = m_table.getSeat(4).getPlayer();
				if (m_gameLog.getPlayerId5() == null)
				{
					m_gameLog.setPlayerId5(player.getPlayerId());
					user = m_table.getSeat(4).getUser();
					m_gameLog.setHeadImgUrl5(user.getHeadImgUrl());
					m_gameLog.setNickName5(user.getNickName());
				}
				m_gameLog.setScore5(player.getScoreTotal());
			}
			if (m_table.getSeat(5).isJoinGame())
			{
				player = m_table.getSeat(5).getPlayer();
				if (m_gameLog.getPlayerId6() == null)
				{
					m_gameLog.setPlayerId6(player.getPlayerId());
					user = m_table.getSeat(5).getUser();
					m_gameLog.setHeadImgUrl6(user.getHeadImgUrl());
					m_gameLog.setNickName6(user.getNickName());
				}
				m_gameLog.setScore6(player.getScoreTotal());
			}
			//更新后面的
			m_action.taurusLogAction().updateRecord(m_gameLog);
		}
		//---------
		//-----------------------------一局详情记录
		TaurusRoundLog roundLog = new TaurusRoundLog();
		roundLog.setRoundIndex(m_roundIndex[m_cur_round-1]);
		roundLog.setRoomNum(roomNum);
		roundLog.setRound((byte)m_cur_round);
		if (m_roomConfig.getBankerMode() != BankerMode.BANKER_MODE_ALL_COMPARE)
		{
			roundLog.setBankerId(m_table.getSeat(m_table.getCurBankerSeatNum()-1).getPlayer().getPlayerId());
		}
		
		HandCard handCard = null;
		TaurusSeat seat = null;
		if (m_table.getSeat(0).isJoinGame())
		{
			seat = m_table.getSeat(0);
			player = seat.getPlayer();
			handCard = seat.getCards();
			roundLog.setPlayerId1(player.getPlayerId());
			roundLog.setCards1(handCard.toString());
			roundLog.setCardType1(handCard.getSpecType().value);
			roundLog.setBaseScore1(player.getBetCoin());
			roundLog.setGetScore1(player.getScore());
			roundLog.setRobBankerNum1((byte)player.getRobBankerNum());
			roundLog.setIsRobBanker1((byte)(player.isRobBanker()?1:0));
			roundLog.setGetScoreTotal1(player.getScoreTotal());
		}
		if (m_table.getSeat(1).isJoinGame())
		{
			seat = m_table.getSeat(1);
			player = seat.getPlayer();
			handCard = seat.getCards();
			roundLog.setPlayerId2(player.getPlayerId());
			roundLog.setCards2(handCard.toString());
			roundLog.setCardType2(handCard.getSpecType().value);
			roundLog.setBaseScore2(player.getBetCoin());
			roundLog.setGetScore2(player.getScore());
			roundLog.setRobBankerNum2((byte)player.getRobBankerNum());
			roundLog.setIsRobBanker2((byte)(player.isRobBanker()?1:0));
			roundLog.setGetScoreTotal2(player.getScoreTotal());
		}
		if (m_table.getSeat(2).isJoinGame())
		{
			seat = m_table.getSeat(2);
			player = seat.getPlayer();
			handCard = seat.getCards();
			roundLog.setPlayerId3(player.getPlayerId());
			roundLog.setCards3(handCard.toString());
			roundLog.setCardType3(handCard.getSpecType().value);
			roundLog.setBaseScore3(player.getBetCoin());
			roundLog.setGetScore3(player.getScore());
			roundLog.setRobBankerNum3((byte)player.getRobBankerNum());
			roundLog.setIsRobBanker3((byte)(player.isRobBanker()?1:0));
			roundLog.setGetScoreTotal3(player.getScoreTotal());
		}
		if (m_table.getSeat(3).isJoinGame())
		{
			seat = m_table.getSeat(3);
			player = seat.getPlayer();
			handCard = seat.getCards();
			roundLog.setPlayerId4(player.getPlayerId());
			roundLog.setCards4(handCard.toString());
			roundLog.setCardType4(handCard.getSpecType().value);
			roundLog.setBaseScore4(player.getBetCoin());
			roundLog.setGetScore4(player.getScore());
			roundLog.setRobBankerNum4((byte)player.getRobBankerNum());
			roundLog.setIsRobBanker4((byte)(player.isRobBanker()?1:0));
			roundLog.setGetScoreTotal4(player.getScoreTotal());
		}
		if (m_table.getSeat(4).isJoinGame())
		{
			seat = m_table.getSeat(4);
			player = seat.getPlayer();
			handCard = seat.getCards();
			roundLog.setPlayerId5(player.getPlayerId());
			roundLog.setCards5(handCard.toString());
			roundLog.setCardType5(handCard.getSpecType().value);
			roundLog.setBaseScore5(player.getBetCoin());
			roundLog.setGetScore5(player.getScore());
			roundLog.setRobBankerNum5((byte)player.getRobBankerNum());
			roundLog.setIsRobBanker5((byte)(player.isRobBanker()?1:0));
			roundLog.setGetScoreTotal5(player.getScoreTotal());
		}
		if (m_table.getSeat(5).isJoinGame())
		{
			seat = m_table.getSeat(5);
			player = seat.getPlayer();
			handCard = seat.getCards();
			roundLog.setPlayerId6(player.getPlayerId());
			roundLog.setCards6(handCard.toString());
			roundLog.setCardType6(handCard.getSpecType().value);
			roundLog.setBaseScore6(player.getBetCoin());
			roundLog.setGetScore6(player.getScore());
			roundLog.setRobBankerNum6((byte)player.getRobBankerNum());
			roundLog.setIsRobBanker6((byte)(player.isRobBanker()?1:0));
			roundLog.setGetScoreTotal6(player.getScoreTotal());
		}
		//增加一局游戏的详细记录
		m_action.taurusRoundLogAction().addOneRoundRecord(roundLog);
		//-----------
		
		//--增加用户表 用户局数加一
		for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
		{
			if (m_table.getSeat(i).isJoinGame())
			{
				user = m_table.getSeat(i).getUser();
				user.setRoundNum(user.getRoundNum()+1);
				m_action.userAction().addUserOneRound(user.getUserId().toString());
			}
		}
	}
	
	/**
	 * 结算一局支付相关 
	 * */
	protected void concludeOneRoundPay()
	{
		if (isClubCard)
		{
			return;
		}
		if (m_roomConfig.getPayMode() == RoomPayMode.PAY_MODE_ALL)
		{//AA支付
			for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
			{
				TaurusSeat seat = m_table.getSeat(i);
				if (!seat.isJoinGame())
				{//空位置 跳过
					continue;
				}
				TaurusPlayer player = seat.getPlayer();
				if (player.isNeedDeductionDiamond())
				{
					player.setNeedDeductionDiamond(false);
					//在坐下的时候进行过校验，直接扣除即可
					//1.轮庄模式或者 2.非轮庄模式局数为10局的时候 --- 需要支付1个钻石
					int needPayDiamond = 1;
					if ((m_roomConfig.getBankerMode() != BankerMode.BANKER_MODE_ROTATE) && (m_roomConfig.getRoundNum() == RoundNum.ROUND_NUM_20))
					{//不是轮庄模式，并且局数为20局的时候 --- 需要支付2个钻石
						needPayDiamond = 2;
					}
					//扣除钻石
					User user = seat.getUser();
					m_action.userAction().deductDiamond(player.getPlayerId(), needPayDiamond);
					user.setDiamond(user.getDiamond()-needPayDiamond);
				}
			}
		}
	}
	
	/**
	 * 确定第一局的庄家
	 * */
	public void sureFirstRoundBanker()
	{
		if (this.m_cur_round == 1 || m_table.getCurBankerSeatNum() == -1)
		{//第一局或者游戏中止后重新开始需要随机庄
			
			int bankerLocation = Utils.getRandomInt(m_table.getRealPlayer());
			int j = 0;
			for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
			{
				TaurusSeat seat = m_table.getSeat(i);
				if (!seat.isJoinGame())
				{//未加入游戏的玩家，跳过
					continue;
				}
				
				if (j == bankerLocation)
				{
					m_table.setCurBankerSeatNum(seat.getId());
					seat.getPlayer().setIsBanker(true);
					break;
				}
				++j;
			}
		}
	}
	
	/**
	 * 庄家确认 ---  通比牛牛的时候没有庄家
	 * */
	public void BankerSure()
	{
		//第一局的庄家需要根据不同的方式进行确认
		sureFirstRoundBanker();
		
		//通知客户端庄家确定
		String bankerId = m_table.getSeat(m_table.getCurBankerSeatNum()-1).getPlayer().getPlayerId();
		OutputMessage om = new OutputMessage(true);
		om.putString(bankerId);
		m_room.sendMessage(TaurusProtocol.PROTOCOL_Ser_Banker_Info, om);
		setGameEngineStep(GAME_STEP_BANKER_SURE_CARTTON);
		logger.info("庄家确定，进入播放庄家动画阶段");
	}
	
	public void BankerSureCartoonEnd()
	{
		if (m_roomConfig.getBaseScore() == BaseScoreType.MODE_BANKER_CHOICE)
		{//底分为庄家选择模式，通知客户端 1.庄家选择底分 2.其他玩家等待庄家选择底分
			notifyAllGameStatus(GameStatus.GAME_STATUS_TABLE_BANKER_CHOOSE_BASECOIN);
			setGameEngineStep(GAME_STEP_BANKER_CHOOSE_BASECOIN);
			logger.info(m_cur_round + "庄家确定，进入庄家选择底分阶段");
		}
		else
		{//底分为其他模式，进入押注阶段 通知客户端 1.进入押注阶段
			Bet();
			setGameEngineStep(GAME_STEP_BET);
			logger.info(m_cur_round + "庄家确定，押注阶段");
		}
	}

	/**
	 * 重置一局的数据  公用方法
	 * */
	public void Reset()
	{
		m_table.reset();
		this.oneRoundIsEnd = false;
	}
	
	public void ComparePockerForNewH5()
	{
		//庄家的一局得分情况
		int banker_score_round = 0;
		int banker_score_win = 0;
		
		/**
		 *  先分别计算闲家的得分情况，最后在计算庄家的得分情况
		 * */
		final TaurusPlayer banker = m_table.getSeat(m_table.getCurBankerSeatNum()-1).getPlayer();
		final HandCard bankerCard = m_table.getSeat(m_table.getCurBankerSeatNum()-1).getCards();
		List<TaurusSeat> loseSeatlist = new ArrayList<TaurusSeat>();
		for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
		{
			TaurusSeat seat = m_table.getSeat(i);
			if (!seat.isJoinGame())
			{//没有加入游戏的玩家，跳过
				continue;
			}
			TaurusPlayer player = seat.getPlayer();
			if (player.IsBanker())
			{//庄家，跳过
				continue;
			}
			int scoreTotal = banker.getScoreTotal();//庄家上庄积分
			CardStyleMath.clacPlayerScore(seat.getPlayer(), bankerCard, seat.getCards(), m_roomConfig);
			if (m_roomConfig.getBankerMode() == BankerMode.BANKER_MODE_BRIGHT_ROB)
			{//明牌抢庄模式下，需要通知
				logger.info("[明牌抢庄]player.score is " + player.getScore() + " RObBankerNum is " + m_table.getBrightRobBankerNum());
				player.setScore(player.getScore()*m_table.getBrightRobBankerNum());
			}
			
			if ((player.getBetCoin() > 0) && (seat.getCurRoundInjectionScore()>0) && (player.getBetCoin() == seat.getCurRoundInjectionScore()))
			{//设置上局推注
				seat.setLastRoundInjection(true);
			}
			if(player.getScore() > 0)
			{
				loseSeatlist.add(seat);
				continue;
						
			}
			if(scoreTotal<Math.abs(player.getScore()))
			{
				player.setScore(-scoreTotal);
				
			}
			
			if(Math.abs(player.getScore())>player.getScoreTotal())
			{
				player.setScore(-player.getScoreTotal());
			}
			
			player.setLastRoundScore(player.getScore());
			player.setScoreTotal(player.getScoreTotal()+player.getScore());
			banker_score_win -= player.getScore(); 
			banker_score_round-= player.getScore();
					
		}
		
		if(loseSeatlist.size() != 0)
		{
			HandCardComparator comparator = new HandCardComparator();
			comparator.setConfig(m_roomConfig);
			Collections.sort(loseSeatlist,comparator);
			
			int remain = banker_score_win+banker.getScoreTotal();
			for(TaurusSeat seat : loseSeatlist)
			{
				TaurusPlayer player = seat.getPlayer();
				if(player.getScore()<= remain)
				{
					player.setLastRoundScore(player.getScore());
					player.setScoreTotal(player.getScoreTotal()+player.getScore());
					
				}else
				{
					player.setScore(remain);
					player.setLastRoundScore(remain);
					player.setScoreTotal(player.getScoreTotal()+remain);
				}
				remain -=player.getScore();
				banker_score_round-=player.getScore();	
			}
		}
		
		//计算庄家得分
		banker.setLastRoundScore(banker.getScore());
		banker.setScore(banker_score_round);
		banker.setScoreTotal(banker.getScoreTotal() + banker_score_round);
		
	}
	/**
	 * 比牌 --- 公用方法
	 * */
	public void ComparePocker()
	{
		//庄家的一局得分情况
		int banker_score_round = 0;
		/**
		 *  先分别计算闲家的得分情况，最后在计算庄家的得分情况
		 * */
		final TaurusPlayer banker = m_table.getSeat(m_table.getCurBankerSeatNum()-1).getPlayer();
		final HandCard bankerCard = m_table.getSeat(m_table.getCurBankerSeatNum()-1).getCards();
		for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
		{
			TaurusSeat seat = m_table.getSeat(i);
			if (!seat.isJoinGame())
			{//没有加入游戏的玩家，跳过
				continue;
			}
			TaurusPlayer player = seat.getPlayer();
			if (player.IsBanker())
			{//庄家，跳过
				continue;
			}
			
			CardStyleMath.clacPlayerScore(seat.getPlayer(), bankerCard, seat.getCards(), m_roomConfig);
			
			if (m_roomConfig.getBankerMode() == BankerMode.BANKER_MODE_BRIGHT_ROB)
			{//明牌抢庄模式下，需要通知
				logger.info("[明牌抢庄]player.score is " + player.getScore() + " RObBankerNum is " + m_table.getBrightRobBankerNum());
				player.setScore(player.getScore()*m_table.getBrightRobBankerNum());
			}
			player.setLastRoundScore(player.getScore());
			player.setScoreTotal(player.getScoreTotal()+player.getScore());
			banker_score_round-= seat.getPlayer().getScore();
			
			if ((player.getBetCoin() > 0) && (seat.getCurRoundInjectionScore()>0) && (player.getBetCoin() == seat.getCurRoundInjectionScore()))
			{//设置上局推注
				seat.setLastRoundInjection(true);
			}
		}
		
		//计算庄家得分
		banker.setLastRoundScore(banker.getScore());
		banker.setScore(banker_score_round);
		banker.setScoreTotal(banker.getScoreTotal() + banker_score_round);
	}
	
	/**
	 * 通知客户端游戏状态发生变化 --- 公用方法
	 * */
	protected void notifyAllGameStatus(GameStatus status)
	{
		m_room.setGameStatus(status);
		OutputMessage om = new OutputMessage(true);
		om.putInt(this.m_cur_round);
		om.putByte(status.value);
		m_room.sendMessage(TaurusProtocol.PROTOCOL_Ser_Game_Status_Changed, om);
	}
	
	/**
	 * 设置游戏当前状态 --- 公用方法
	 * */
	protected void setGameEngineStep(int step)
	{
		m_engineTimer = 0; //不管什么时候进入的下一步骤，计时器需要清0
		m_step = step;
	}
	
	/**
	 * 销毁相关资源 --- 公用方法
	 * */
	protected void destroy()
	{
		Set<String> userIdList = m_room.getUserIdList();
		for (String userId : userIdList)
		{
			BaseServer.unbindUserRoom(userId);
			m_room.removeSession(userId);
		}
		
		for (int i=0; i<SEAT_NUM; ++i)
		{
			TaurusSeat seat = m_table.getSeat(i);
			if (seat.isCanSitDown())
			{
				continue;
			}
			String userId = seat.getPlayer().getPlayerId();
			BaseServer.unbindUserRoom(userId);
			BaseServer.unbindUserTable(userId);
			
			//一局游戏结束，俱乐部游戏房间桌子坐下的玩家，被冻结时处理  clear session
			TaurusClub taurusClub = BaseClubServer.getUserClub(userId);
			if(taurusClub != null && seat.getUser().getIsFrozen() == User.STATE_FROZEN)
			{
				//club 移除
				taurusClub.removeUserSession(userId);
				BaseClubServer.unbindUserClub(userId);
				//baseSever 移除
				BaseServer.removeUser(BaseServer.getUserSession(userId));
			}
		}
		int roomNum = m_room.getRoomId();
		BaseServer.removeTable(m_table);
		BaseServer.removeRoom(String.valueOf(m_room.getRoomId()));
		//判断房间是在好友场还是在俱乐部
		if(m_room.getClubId() == 0)
		{   
			List<GameSession> sessions = m_room.getSessionList();
			for (int i=0; i<sessions.size(); ++i)
			{
				GameSession session = sessions.get(i);
				BaseServer.removeUser(session);
			}
			//清除玩家的进入房间记录
			RedisResource.deleteDataFromRedis(String.valueOf(roomNum));
		}
		else
		{   
			int clubId = m_room.getClubId();
			TaurusClub taurusClub = BaseClubServer.getClub(clubId);
			taurusClub.removeRoom(m_room.getRoomId());
			m_action.clubRoomAction().unBindRoomClub(m_room.getRoomId());
		}
		
		//1.销毁房间
		m_room.clear();
		RoomManager.addRoom(m_room);
		
		//2.销毁桌子
		//m_table.destroy();
		m_table.clear();
		TableManager.addTaurusTable(m_table);
		
		//3.销毁引擎
		GameTimer.getInstance().removeEngine(this);
		clearGameEngine();
		EngineManager.addGameEngine(this); 
		 
		
		//删除数据库中的房间记录
		m_action.roomAction().deletePrivateRoom(roomNum);
		
		this.m_action = null;
	}
	
	//一场游戏结束，将引擎资源返还到引擎资源池
	public void clearGameEngine()
	{
		 this.m_engineId = null ;
		 this.m_engineTimer = 0 ;
		 this.m_gameLog = null;
		 this.m_prevRoundIndex = null ;
		 this.m_roundIndex = null ;
		 this.m_cur_round = 0 ;
	}
	
	/** * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * 根据一局结束判断是否游戏结束        
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	public boolean Finish()
	{
		if ((m_roomConfig.getBankerMode() == BankerMode.BANKER_MODE_FIXED) &&
			(m_roomConfig.getUpBankerScore() != UpBankerScore.UP_BANKER_SCORE_NONE) &&
			(m_table.getSeat(0).getPlayer().getScoreTotal() <= 0))
		{
			handleUserTableNum();
			m_room.sendMessage(TaurusProtocol.PROTOCOL_Ser_FIXED_Banker_No_Enough_Coin_Close_Game, new OutputMessage(true));
			logger.info("房间"+m_room.getRoomId()+"[固定庄家]庄家分数不足，房间自动解散");
			destroy();
			return true;
		}
		
		if((isNewH5 && m_gameTimer >= m_roomConfig.getGameTime()*60*1000) || m_cur_round == m_roomConfig.getRoundNum())
		{
			if(isNewH5 && m_gameTimer >= m_roomConfig.getGameTime()*60*1000)
			{
				returnClubMemberCoin();//游戏结束返还玩家金币
			}
			//通知所有房间内的客户端，牌局结束，解散房间
			handleUserTableNum();
			notifyAllGameStatus(GameStatus.GAME_STATUS_END);
			logger.info("房间"+m_room.getRoomId()+"牌局完成，房间解散");
			
			destroy();
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/** ******************************************************************************
	 * ----------------------以下为对托管玩家  和   即未托管也未操作的玩家的处理-------------------------
	 *********************************************************************************/
	/** 托管玩家的步骤处理 -- 准备*/
	protected void autoActionReady()
	{
		synchronized (m_table.getSeats())
		{
			TaurusSeat[] seats = m_table.getSeats();
			for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
			{
				if ((!seats[i].isJoinGame()) || seats[i].isReady())
				{//空位置，跳过
					continue;
				}
				if (seats[i].isAutoAction())
				{//设置为准备，通知房间内所有人
					seats[i].ready();
					OutputMessage om = new OutputMessage(true);
					om.putString(seats[i].getPlayer().getPlayerId());
					m_room.sendMessage(TaurusProtocol.PROTOCOL_Ser_Ready, om);
				}
			}
		}
	}
	/** 托管玩家的步骤处理 -- 押注*/
	protected void autoActionBet()
	{
		synchronized (m_table.getSeats())
		{
			TaurusSeat[] seats = m_table.getSeats();
			for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
			{
				if ((!seats[i].isJoinGame()) || seats[i].isBet())
				{//未加入游戏的玩家，跳过
					continue;
				}
				if ((m_table.getCurBankerSeatNum()!=0) && (m_table.getCurBankerSeatNum()==i+1))
				{//跳过庄家位置
					continue;
				}
				
				if (seats[i].isAutoAction())
				{//设置为押注，通知房间内所有人
					seats[i].bet();
					int leastBetCoin = BetUtils.getLeastBet(m_roomConfig);
					seats[i].getPlayer().setBetCoin(leastBetCoin);
					OutputMessage om = new OutputMessage(true);
					om.putString(seats[i].getPlayer().getPlayerId());
					om.putInt(leastBetCoin);
					m_room.sendMessage(TaurusProtocol.PROTOCOL_Ser_Bet_Coin, om);
				}
			}
		}
	}
	/** 托管玩家的步骤处理 -- 亮牌*/
	protected void autoActionOpenCards()
	{
		synchronized (m_table.getSeats())
		{
			TaurusSeat[] seats = m_table.getSeats();
			
			for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
			{
				if ((!seats[i].isJoinGame()) || seats[i].isOpenCards())
				{//未加入游戏的玩家，跳过
					continue;
				}
				if (seats[i].isAutoAction())
				{//设置为亮牌，通知房间内所有人
					seats[i].openCards();
					
					OutputMessage om = new OutputMessage(true);
					om.putString(seats[i].getPlayer().getPlayerId());
					om.putShort(seats[i].getCards().getCards()[0]);
					om.putShort(seats[i].getCards().getCards()[1]);
					om.putShort(seats[i].getCards().getCards()[2]);
					om.putShort(seats[i].getCards().getCards()[3]);
					om.putShort(seats[i].getCards().getCards()[4]);
					om.putByte(seats[i].getCards().getSpecType().value);
					m_room.sendMessage(TaurusProtocol.PROTOCOL_Ser_Open_Cards, om);
				}
			}
		}
	}
	/** 托管玩家的步骤处理 -- 抢庄*/
	protected void autoActionRobBanker()
	{
		synchronized (m_table.getSeats())
		{
			TaurusSeat[] seats = m_table.getSeats();
			for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
			{
				if ((!seats[i].isJoinGame()) || seats[i].isRobBanker())
				{//未加入游戏的玩家，跳过
					continue;
				}
				if (seats[i].isAutoAction())
				{//设置为抢庄
					seats[i].robBanker();
					//托管玩家默认不抢庄
				}
			}
			
		}
	}
	
	/** 未托管未操作玩家的步骤处理 -- 准备 */
	protected void HandleNoOperateReady()
	{
		synchronized (m_table.getSeats())
		{
			TaurusSeat[] seats = m_table.getSeats();
			for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
			{
				if (seats[i].isCanSitDown())
				{//空位置，跳过
					continue;
				}
				if (!seats[i].isReady())
				{//设置为准备
					seats[i].ready();
				}
			}
			
		}
	}
	/** 未托管未操作玩家的步骤处理 -- 押注 */
	protected void HandleNoOperateBet()
	{
		synchronized (m_table.getSeats())
		{
			TaurusSeat[] seats = m_table.getSeats();
			for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
			{
				if (!seats[i].isJoinGame())
				{//未加入游戏的玩家，跳过
					continue;
				}
				if (!seats[i].isBet())
				{//设置为押注，通知房间内所有人
					seats[i].bet();
					int leastBetCoin = BetUtils.getLeastBet(m_roomConfig);
					seats[i].getPlayer().setBetCoin(leastBetCoin);
					
					OutputMessage om = new OutputMessage(true);
					om.putString(seats[i].getPlayer().getPlayerId());
					om.putInt(leastBetCoin);
					m_room.sendMessage(TaurusProtocol.PROTOCOL_Ser_Bet_Coin, om);
				}
			}
			
		}
	}
	/** 未托管未操作玩家的步骤处理 -- 亮牌 */
	protected void HandleNoOperateOpenCards()
	{
		synchronized (m_table.getSeats())
		{
			TaurusSeat[] seats = m_table.getSeats();
			for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
			{
				if (!seats[i].isJoinGame())
				{//未加入游戏的玩家，跳过
					continue;
				}
				if (!seats[i].isOpenCards())
				{//设置为亮牌，通知房间内所有人
					seats[i].openCards();
					
					OutputMessage om = new OutputMessage(true);
					om.putString(seats[i].getPlayer().getPlayerId());
					om.putShort(seats[i].getCards().getCards()[0]);
					om.putShort(seats[i].getCards().getCards()[1]);
					om.putShort(seats[i].getCards().getCards()[2]);
					om.putShort(seats[i].getCards().getCards()[3]);
					om.putShort(seats[i].getCards().getCards()[4]);
					om.putByte(seats[i].getCards().getSpecType().value);
					m_room.sendMessage(TaurusProtocol.PROTOCOL_Ser_Open_Cards, om);
				}
			}
			
		}
	}
	/** 未托管未操作玩家的步骤处理 -- 抢庄 */
	protected void HandleNoOperateRobBanker()
	{
		synchronized (m_table.getSeats())
		{
			TaurusSeat[] seats = m_table.getSeats();
			for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
			{
				if (!seats[i].isJoinGame())
				{//未加入游戏的玩家，跳过
					continue;
				}
				if (seats[i].isRobBanker())
				{//设置为抢庄
					seats[i].robBanker();
					//不操作玩家默认不抢庄
				}
			}
			
		}
	}
}
