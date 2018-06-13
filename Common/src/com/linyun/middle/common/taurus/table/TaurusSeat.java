package com.linyun.middle.common.taurus.table;

import com.linyun.bottom.container.GameSeat;
import com.linyun.common.entity.User;
import com.linyun.common.taurus.eum.DissolutionStatus;
import com.linyun.middle.common.taurus.card.HandCard;
import com.linyun.middle.common.taurus.player.TaurusPlayer;

public class TaurusSeat extends GameSeat
{
	public static final int SEAT_STATUS_OPEN = 0;  //打开
	public static final int SEAT_STATUS_OCCUPY = 1;  //被占有，未准备
	public static final int SEAT_STATUS_READY = 2;  //准备
	/** 玩家 */
	private TaurusPlayer player;
	/** 手牌信息 */
	private final HandCard cards;
	/** 玩家的详细信息 */
	private User user;
	
	//下面的都是游戏流程性馆的状态变量， 在增加游戏记录时，不做为游戏记录的参考依据
	//---------------------------游戏内流程相关变量------------------------
	/** 椅子准备状态 */
	private volatile boolean isReady;
	/** 椅子上玩家是否抢庄 */
	private volatile boolean isRobBanker;
	/** 椅子上玩家是否押注 */
	private volatile boolean isBet;
	/** 椅子上玩家是否亮牌 */
	private volatile boolean isOpenCards;
	//---------------------------游戏内流程变量------------------------end
	
	/** 椅子上的玩家对解散申请作出的选择   0-未选择 1-选择不同意 2-选择同意*/
	private DissolutionStatus dissolutionStatus;
	
	/** 椅子上的玩家是否在线 */
	private boolean isOnline;
	
	/** 是否处于托管状态 */
	private volatile boolean isAutoAction;
	/** 是否参加游戏 */
	private volatile boolean isJoinGame;
	/** 是否上局庄家 */
	private volatile boolean isLastRoundBanker;
	/** 椅子上玩家上局是否推注 */
	private volatile boolean isLastRoundInjection;
	/** 当前局的可推注分数，用于判断当前局是否推注 */
	private int curRoundInjectionScore;
	/**该椅子上的玩家是否保座离桌*/
	private boolean isKeepSeatStage;
	/**该座位保座离桌倒计时*/
	private int keepSeatTimer;
	
	
	public TaurusSeat(int _id)
	{
		super(_id);
		this.cards = new HandCard();
		
		this.isReady = false;
		this.isRobBanker = false;
		this.isBet = false;
		this.isOpenCards = false;
		
		this.dissolutionStatus = DissolutionStatus.DISSOLUTION_UNSELECTED;
		this.isLastRoundInjection = false;
		this.isAutoAction = false;
		this.isJoinGame = false;
		this.isLastRoundBanker = false;
		this.setOnline(true);
		this.isKeepSeatStage = false;
		this.keepSeatTimer = 0;
	}
	
	public void Init(int _id)
	{
		this.id = _id;
		this.isCanSitDown = true;
		
		this.cards.Init();
		this.isReady = false;
		this.isRobBanker = false;
		this.isBet = false;
		this.isOpenCards = false;
		
		this.dissolutionStatus = DissolutionStatus.DISSOLUTION_UNSELECTED;
		this.isLastRoundInjection = false;
		this.isAutoAction = false;
		this.isJoinGame = false;
		this.isLastRoundBanker = false;
		this.setOnline(true);
		this.isKeepSeatStage = false;
		this.keepSeatTimer = 0;
	}
	
	public void resetGame()
	{//重置游戏相关变量
		player.reset();
		cards.reset();
		
		this.isBet = false;
		this.isRobBanker = false;
		this.isOpenCards = false;
		this.isReady = false;
		this.curRoundInjectionScore = 0;
		
	}
	
	public void resetDissolution()
	{//重置解散相关的变量
		this.dissolutionStatus = DissolutionStatus.DISSOLUTION_UNSELECTED; 
	}
	
	public TaurusPlayer getPlayer()
	{
		return player;
	}
	public void setPlayer(TaurusPlayer player)
	{
		this.player = player;
	}
	
	public void destory()
	{
		player = null;
	}
	
	public void clear()
	{   
		this.cards.clear();
		this.player.clear();
		this.user = null ;
	}

	public HandCard getCards()
	{
		return cards;
	}

	public boolean isBet()
	{
		return isBet;
	}
	public void bet()
	{
		this.isBet = true;
	}

	public boolean isOpenCards()
	{
		return isOpenCards;
	}
	public void openCards()
	{
		this.isOpenCards = true;
	}

	public boolean isRobBanker()
	{
		return isRobBanker;
	}
	public void robBanker()
	{
		this.isRobBanker = true;
	}

	public DissolutionStatus getDissolutionStatus()
	{
		return dissolutionStatus;
	}
	public void setDissolutionStatus(DissolutionStatus dissolutionStatus)
	{
		this.dissolutionStatus = dissolutionStatus;
	}

	public boolean isLastRoundInjection()
	{
		return isLastRoundInjection;
	}
	public void setLastRoundInjection(boolean isLastRoundInjection)
	{
		this.isLastRoundInjection = isLastRoundInjection;
	}

	public User getUser()
	{
		return user;
	}
	public void setUser(User user)
	{
		this.user = user;
	}

	public boolean isAutoAction()
	{
		return isAutoAction;
	}
	public void setAutoAction(boolean isAutoAction)
	{
		this.isAutoAction = isAutoAction;
	}

	public boolean isJoinGame()
	{
		return isJoinGame;
	}
	public void setJoinGame(boolean isJoinGame)
	{
		this.isJoinGame = isJoinGame;
	}

	public boolean isLastRoundBanker()
	{
		return isLastRoundBanker;
	}
	public void setLastRoundBanker(boolean isLastRoundBanker)
	{
		this.isLastRoundBanker = isLastRoundBanker;
	}
	public void ready()
	{
		this.isReady = true;
	}
	public boolean isReady()
	{
		return this.isReady;
	}

	public int getCurRoundInjectionScore()
	{
		return curRoundInjectionScore;
	}
	public void setCurRoundInjectionScore(int curRoundInjectionScore)
	{
		this.curRoundInjectionScore = curRoundInjectionScore;
	}

	public boolean isOnline() 
	{
		return isOnline;
	}
	public void setOnline(boolean isOnline) 
	{
		this.isOnline = isOnline;
	}

	public boolean isKeepSeatStage() {
		return isKeepSeatStage;
	}

	public void setKeepSeatStage(boolean isKeepSeatStage) {
		this.isKeepSeatStage = isKeepSeatStage;
	}

	public int getKeepSeatTimer() {
		return keepSeatTimer;
	}
	public void setKeepSeatTimer(int time)
	{
		this.keepSeatTimer = time;
	}

	public void addKeepSeatTimerCount(int count)
	{
		this.keepSeatTimer += count;
	}
	
}
