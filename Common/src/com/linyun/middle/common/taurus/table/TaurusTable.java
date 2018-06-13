package com.linyun.middle.common.taurus.table;

import com.linyun.bottom.common.exception.GameException;
import com.linyun.bottom.container.Container;
import com.linyun.bottom.container.GameRoom;
import com.linyun.bottom.container.GameSession;
import com.linyun.bottom.container.GameTable;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.common.entity.User;
import com.linyun.common.taurus.eum.BankerChooseBaseScore;
import com.linyun.common.taurus.eum.DissolutionStatus;
import com.linyun.common.taurus.eum.GameStatus;
import com.linyun.middle.common.taurus.player.TaurusPlayer;
import com.linyun.middle.common.taurus.room.TaurusRoom;
import com.linyun.middle.common.taurus.service.BaseServer;

public class TaurusTable extends GameTable
{
	public static final int TABLE_SEAT_NUM = 6;
	
	private TaurusSeat[] seats;
	
	private String [] roundIndex;
	//庄家是否选择了底分的标志位，在底分模式为庄家选择的时候使用 
	private BankerChooseBaseScore chooseBaseCoin;
	
	/** 当前局庄家的座位号*/
	private int curBankerSeatNum;
	/** 上一局的庄家的座位号 */
	private int prevBankerSeatNum;
	
	/** 明牌抢庄时一局特有的抢庄倍数,计算分数时用到 */
	private volatile int BrightRobBankerNum;
	/** 是否处于解散的阶段状态 */
	private boolean isDissolutionStage;
	
	/** 当前局数 */
	private int curRound;
	/** 参加游戏的人数 */
	private int curPlayGamePlayerCount;
	
	/** 解散倒计时变量 */
	private int dissolutionTimer;
	
	/** 固定庄家，庄家是否下庄的标识位 */
	private volatile boolean isDownBanker; 
	
	public TaurusTable(String _tableId)
	{
		super(_tableId, TABLE_SEAT_NUM);
		this.seats = new TaurusSeat[TABLE_SEAT_NUM];
		for (int i=0; i<TABLE_SEAT_NUM; ++i)
		{
			seats[i] = new TaurusSeat(i+1);
		}
		this.chooseBaseCoin = BankerChooseBaseScore.BANKER_BASE_SCORE_0;
		this.curBankerSeatNum = 0;
		this.prevBankerSeatNum = 0;
		this.isDissolutionStage = false;
		this.curPlayGamePlayerCount = 0;
		this.BrightRobBankerNum = 0;
		this.isDownBanker = false;
	}
	
	public void Init(String _tableId)
	{
		this.tableId = _tableId ;
		this.isCanEnter = true;
		this.seatTotal = TABLE_SEAT_NUM;
		this.realPlayer = 0;
		for (int i = 0; i < TABLE_SEAT_NUM; i++)
		{
			this.seats[i].Init(i+1);
		}
		this.chooseBaseCoin = BankerChooseBaseScore.BANKER_BASE_SCORE_0;
		this.curBankerSeatNum = 0;
		this.prevBankerSeatNum = 0;
		this.isDissolutionStage = false;
		this.curPlayGamePlayerCount = 0;
		this.BrightRobBankerNum = 0;
		this.isDownBanker = false;
	}
	
	public void setAllJoinGame()
	{
		synchronized(seats)
		{
			for (int i=0; i<TABLE_SEAT_NUM; ++i)
			{
				if (seats[i].isCanSitDown() || seats[i].isKeepSeatStage())
				{
					continue;
				}
				seats[i].setJoinGame(true);
				seats[i].getPlayer().setNeedReturnDiamond(false);//表示该位置玩家已参与游戏，退出时不需要返还钻石
			}
		}
	}
	
	public TaurusSeat[] getSeats()
	{
		return this.seats;
	}
	
	public boolean addPlayer(TaurusPlayer player, User user,byte seatId, TaurusRoom room)
	{
		synchronized(seats)
		{
			if (!this.isCanEnter)
			{
				return false;
			}
			
			if (!seats[seatId].isCanSitDown())
			{
				return false;
			}
			
			seats[seatId].setCanSitDown(false);
			seats[seatId].setPlayer(player);
			seats[seatId].setUser(user);
			seats[seatId].ready();//坐下的玩家默认为准备
		    if(room.getGameStatus()==GameStatus.GAME_STATUS_TABLE_READY || room.getGameStatus() == GameStatus.GAME_STATUS_PAUSE)
			{
			   seats[seatId].setJoinGame(true);
			}
			 ++realPlayer;
				
			if (realPlayer==TABLE_SEAT_NUM)
			{
				this.isCanEnter = false;
			}
			
		  return true;
		}
	}
	
	public boolean addPlayer(TaurusPlayer player, User user, byte seatId)
	{
		synchronized(seats)
		{
			if (!this.isCanEnter)
			{
				return false;
			}
		
			if (!seats[seatId].isCanSitDown())
			{
				return false;
			}
				
			seats[seatId].setCanSitDown(false);
			seats[seatId].setPlayer(player);
			seats[seatId].setUser(user);
			seats[seatId].ready();//坐下的玩家默认为准备
				
			++realPlayer;
				
			if (realPlayer==TABLE_SEAT_NUM)
			{
				this.isCanEnter = false;
			}
			return true;
		}
	}
	
	
	public boolean removePlayer(String playerId)
	{
		synchronized(seats)
		{
			for (int i=0; i<TABLE_SEAT_NUM; ++i)
			{
				if (seats[i].isCanSitDown())
				{
					continue;
				}
				
				if (seats[i].getPlayer().getPlayerId().equals(playerId))
				{
					seats[i].setCanSitDown(true);
					seats[i].setJoinGame(false);
					seats[i].setPlayer(null);
					seats[i].setUser(null);
					seats[i].setKeepSeatStage(false);
					seats[i].setKeepSeatTimer(0);
					--realPlayer;
					
					this.isCanEnter = true;
					return true;
				}
			}
			return false;
		}
	}
	
	public void startGame()
	{
		synchronized(seats)
		{
			for (int i=0; i<TABLE_SEAT_NUM; ++i)
			{
				if (seats[i].isCanSitDown())
				{
					continue;
				}
				seats[i].setJoinGame(true);
			}
			this.curRound = 1;
		}
	}
	
	public boolean userIsExist(String userId)
	{
		synchronized(seats)
		{
			for (int i=0; i<TABLE_SEAT_NUM; ++i)
			{
				if (seats[i].isCanSitDown())
				{
					continue;
				}
				if (seats[i].getPlayer().getPlayerId().equals(userId))
				{
					return true;
				}
			}
			return false;
		}
	}
	
	public int getJoinPlayers()
	{
		synchronized(seats)
		{
			int joinPlayers = 0;
			for (int i=0; i<TABLE_SEAT_NUM; ++i)
			{
				if (!seats[i].isJoinGame())
				{
					continue;
				}
				
				++joinPlayers;
			}
			return joinPlayers;
		}
	}
	public boolean isAllBet()
	{
		synchronized(seats)
		{
			for (int i=0; i<TABLE_SEAT_NUM; ++i)
			{
				if (!seats[i].isJoinGame())
				{
					continue;
				}
				
				if (!seats[i].isBet())
				{
					return false;
				}
			}
			return true;
		}
	}
	
	public boolean isAllReady()
	{
		synchronized(seats)
		{
			for (int i=0; i<TABLE_SEAT_NUM; ++i)
			{
				if (!seats[i].isJoinGame())
				{
					continue;
				}
				if (!seats[i].isReady())
				{
					return false;
				}
			}
			return true;
		}
	}
	public boolean isAllRobBanker()
	{
		synchronized(seats)
		{
			for (int i=0; i<TABLE_SEAT_NUM; ++i)
			{
				if (!seats[i].isJoinGame())
				{
					continue;
				}
				if (!seats[i].isRobBanker())
				{
					return false;
				}
			}
			return true;
		}
	}
	
	public boolean isAllOpenCards()
	{
		synchronized(seats)
		{
			for (int i=0; i<TABLE_SEAT_NUM; ++i)
			{
				if (!seats[i].isJoinGame())
				{
					continue;
				}
				if (!seats[i].isOpenCards())
				{
					return false;
				}
			}
			return true;
		}
	}
	
	public boolean isAllAgreeDissolution()
	{
		synchronized(seats)
		{
			for (int i=0; i<TABLE_SEAT_NUM; ++i)
			{
				if (seats[i].isCanSitDown())
				{
					continue;
				}
				if (seats[i].getDissolutionStatus() != DissolutionStatus.DISSOLUTION_AGREE)
				{
					return false;
				}
			}
			return true;
		}
	}
	
	public void userReady(String userId)
	{
		synchronized(seats)
		{
			for (int i=0; i<TABLE_SEAT_NUM; ++i)
			{
				if (seats[i].isCanSitDown())
				{
					continue;
				}
				if (seats[i].getPlayer().getPlayerId().equals(userId))
				{
					seats[i].ready();
					return;
				}
			}
			throw new GameException(GameException.SEAT_NOT_EXIST, "玩家准备的时候，找不到对应的座位,userId " + userId);
		}
	}
	public void userReady(int locationId)
	{
		synchronized(seats)
		{
			if (locationId>6 || locationId<1)
			{
				throw new GameException(GameException.SEAT_INDEX_ERROR, "玩家准备的时候传入了错误的位置索引，索引值为 " + locationId);
			}
			if (seats[locationId-1].isCanSitDown())
			{
				throw new GameException(GameException.SEAT_INDEX_ERROR, "玩家准备的时候传位置索引的位置上没有玩家，索引值为 " + locationId);
			}
			seats[locationId-1].ready();
		}
	}
	
	//找到返回位置  没找到返回-1
	public int getUserLocation(String userId)
	{
		synchronized (seats)
		{
			for (int i=0; i<TABLE_SEAT_NUM; ++i)
			{
				if (seats[i].isCanSitDown())
				{
					continue;
				}
				if (seats[i].getPlayer().getPlayerId().equals(userId))
				{
					return seats[i].getId()-1;
				}
			}
			return -1;
		}
	}
	
	public TaurusSeat getUserSeat(String userId)
	{
		synchronized (seats)
		{
			for (int i=0; i<TABLE_SEAT_NUM; ++i)
			{
				if (seats[i].isCanSitDown())
				{
					continue;
				}
				if (seats[i].getPlayer().getPlayerId().equals(userId))
				{
					return seats[i];
				}
			}
			return null;
		}
	}
	public TaurusSeat getSeat(int locationId)
	{
		synchronized(seats)
		{
			if (locationId>5 || locationId<0)
			{
				return null;
			}
			return seats[locationId];
		}
	}
	
	public void clearDissolution()
	{
		synchronized (seats)
		{
			this.isDissolutionStage = false;
			for (int i=0; i<TABLE_SEAT_NUM; ++i)
			{
				seats[i].resetDissolution();
			}
			this.dissolutionTimer = 0;
		}
	}
	
	@Override
	public void reset()
	{
		synchronized (seats)
		{
			for (int i=0; i<TABLE_SEAT_NUM; ++i)
			{
				if (seats[i].isCanSitDown())
				{
					continue;
				}
				seats[i].resetGame();
			}
		}
		this.chooseBaseCoin = BankerChooseBaseScore.BANKER_BASE_SCORE_0;
		this.BrightRobBankerNum = 0;
	}
	
	public void destory()
	{
		for (TaurusSeat seat:seats)
		{
			seat.destory();
		}
		seats = null;
	}
	
	public void clear()
	{   
		for (TaurusSeat seat:seats)
		{   
			if(seat.isCanSitDown())
			{
				continue ;
			}
			seat.clear();
		}
		this.roundIndex = null ;
		this.curRound = 0 ;
		this.dissolutionTimer = 0 ;
		this.tableId = null ;
	}

	public boolean isChooseBaseCoin()
	{
		return chooseBaseCoin != BankerChooseBaseScore.BANKER_BASE_SCORE_0;
	}
	public void setChooseBaseCoin(BankerChooseBaseScore chooseBaseCoin)
	{
		this.chooseBaseCoin = chooseBaseCoin;
	}
	public BankerChooseBaseScore getChooseBaseCoin()
	{
		return chooseBaseCoin;
	}

	public boolean isBanker(String userId)
	{
		return getSeat(curBankerSeatNum-1).getPlayer().getPlayerId().equals(userId);
	}
	
	public int getCurBankerSeatNum()
	{
		return curBankerSeatNum;
	}
	public void setCurBankerSeatNum(int curBankerSeatNum)
	{
		this.curBankerSeatNum = curBankerSeatNum;
	}

	public int getPrevBankerSeatNum()
	{
		return prevBankerSeatNum;
	}

	public void setPrevBankerSeatNum(int prevBankerSeatNum)
	{
		this.prevBankerSeatNum = prevBankerSeatNum;
	}

	public boolean isDissolutionStage()
	{
		return isDissolutionStage;
	}
	public void setDissolutionStage(boolean isDissolutionStage)
	{
		this.isDissolutionStage = isDissolutionStage;
	}
	
	public boolean isInterruptStage() 
	{
		int realPlayer2 = getRealPlayer();
		synchronized (seats)
		{
			int keepSeats = 0;
			for (int i=0; i<TABLE_SEAT_NUM; ++i)
			{
				if (seats[i].isCanSitDown())
				{
					continue;
				}
				if (seats[i].isKeepSeatStage())
				{
					++keepSeats;
				}
			}
			
			if(realPlayer2-keepSeats<=1)
			{
				return true;
			}
			
			return false;
		}
	}

	public int getCurRound()
	{
		return curRound;
	}
	public void setCurRound(int curRound)
	{
		this.curRound = curRound;
	}

	public int getCurPlayGamePlayerCount()
	{
		return curPlayGamePlayerCount;
	}
	public void setCurPlayGamePlayerCount(int curPlayGamePlayerCount)
	{
		this.curPlayGamePlayerCount = curPlayGamePlayerCount;
	}
	
	public void addDissolutionTimerCount(int count)
	{
		this.dissolutionTimer += count;
	}
	public int getDissolutionTimer()
	{
		return this.dissolutionTimer;
	}
	
	public boolean containsUser(String userId)
	{
		synchronized (seats)
		{
			for (int i=0; i<seats.length; ++i)
			{
				if (seats[i].isCanSitDown())
				{
					continue;
				}
				
				if (seats[i].getPlayer().getPlayerId().equals(userId))
				{
					return true;
				}
			}
			return false;
		}
	}

	public String getLastRoundIndex() 
	{
		if (curRound <= 1)
		{
			return "";
		}
		else
		{
			return roundIndex[curRound-2];
		}
	}
	public void setRoundIndex(String [] roundIndex) 
	{
		this.roundIndex = roundIndex;
	}

	public int getBrightRobBankerNum()
	{
		return BrightRobBankerNum;
	}

	public void setBrightRobBankerNum(int brightRobBankerNum)
	{
		BrightRobBankerNum = brightRobBankerNum;
	}

	public boolean isDownBanker()
	{
		return isDownBanker;
	}
	public void downBanker()
	{
		this.isDownBanker = true;
	}
	
	public void sendMessage(short protocolId, OutputMessage message)
	{
		String playerId;
		GameSession session;
		String sessionId;
		for (int i=0; i<TABLE_SEAT_NUM; ++i)
		{
			if (seats[i].isCanSitDown())
			{
				continue;
			}
			playerId = seats[i].getPlayer().getPlayerId();
			sessionId = BaseServer.userSessionMap.get(playerId);
			if (sessionId != null)
			{
				session = Container.getSessionById(sessionId);
				if (session != null)
				{
					session.sendMessage(protocolId, message);
				}
			}
		}
	}
}
