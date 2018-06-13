package com.linyun.middle.common.taurus.player;

import com.linyun.bottom.common.Player;

public class TaurusPlayer extends Player 
{	
	/** 玩家一局的押注底分 */
	private volatile int betCoin;
	/** 玩家一局的抢庄倍率,明牌抢庄时用到,值为不抢 抢2 抢3 抢4*/
	private volatile int robBankerNum;
	/** 是否抢庄，自由抢庄时用到 */
	private volatile boolean isRobBanker;
	/** 是否庄家 */
	private boolean isBanker;
	/** 玩家的一局得分 */
	private int score;
	/** 玩家的上一局得分,闲家推注模式时用到 */
	private int lastRoundScore;
	/** 玩家在一个桌子内的总得分 */
	private int scoreTotal;
	/** 玩家的上庄次数，轮庄牛牛的时候使用 */
	private int upBankerTime;
	/** 玩家是否需要扣除房费，AA支付时，在玩家第一局打完的时候需要扣除 */
	private boolean isNeedDeductionDiamond;
	/** 玩家退出房间时是否需要返还钻石 给群主*/
	private boolean isNeedReturnDiamond;

	public TaurusPlayer(String tableId, String userId)
	{
		super(tableId, userId);
		this.betCoin = 0;
		this.robBankerNum = 0;
		this.isBanker = false;
		this.score = 0;
		this.scoreTotal = 0;
		this.upBankerTime = 0;
		this.isNeedDeductionDiamond = true;
		this.isNeedReturnDiamond = true;
		this.setLastRoundScore(0);
	}
	
	public TaurusPlayer(String tableId, String userId, int entryScore)
	{
		super(tableId, userId);
		this.betCoin = 0;
		this.robBankerNum = 0;
		this.isBanker = false;
		this.score = 0;
		this.scoreTotal = entryScore;
		this.upBankerTime = 0;
		this.isNeedDeductionDiamond = false;
		this.isNeedReturnDiamond = true;
		this.setLastRoundScore(0);
	}
	
	public void Init(String tableId, String userId)
	{   
		this.userId = userId;
		this.tableId = tableId;
		this.betCoin = 0;
		this.robBankerNum = 0;
		this.isBanker = false;
		this.score = 0;
		this.scoreTotal = 0;
		this.upBankerTime = 0;
		this.isNeedDeductionDiamond = true;
		this.isNeedReturnDiamond = true;
		this.setLastRoundScore(0);
	}
	
	public void reset()
	{
		this.betCoin = 0;
		this.robBankerNum = 0;
		this.isRobBanker = false;
		this.isBanker = false;
		this.score = 0;
	}
	public void destory()
	{
		
	}
	
	public void clear()
	{
		this.userId = null;
		this.tableId = null;
	}

	public int getScore()
	{
		return score;
	}
	public void setScore(int _score)
	{
		this.score = _score;
	}

	public int getRobBankerNum()
	{
		return robBankerNum;
	}
	public void setRobBankerNum(int robBankerNum)
	{
		this.robBankerNum = robBankerNum;
	}

	public int getBetCoin()
	{
		return betCoin;
	}
	public void setBetCoin(int betCoin)
	{
		this.betCoin = betCoin;
	}

	public boolean IsBanker()
	{
		return isBanker;
	}
	public void setIsBanker(boolean isBanker)
	{
		this.isBanker = isBanker;
	}

	public int getScoreTotal()
	{
		return scoreTotal;
	}
	public void setScoreTotal(int scoreTotal)
	{
		this.scoreTotal = scoreTotal;
	}

	public boolean isRobBanker()
	{
		return isRobBanker;
	}
	public void RobBanker()
	{
		this.isRobBanker = true;
	}

	public int getUpBankerTime()
	{
		return upBankerTime;
	}
	public void setUpBankerTime(int upBankerTime)
	{
		this.upBankerTime = upBankerTime;
	}

	public boolean isNeedDeductionDiamond()
	{
		return isNeedDeductionDiamond;
	}
	public void setNeedDeductionDiamond(boolean isNeedDeductionDiamond)
	{
		this.isNeedDeductionDiamond = isNeedDeductionDiamond;
	}

	public int getLastRoundScore()
	{
		return lastRoundScore;
	}
	public void setLastRoundScore(int lastRoundScore)
	{
		this.lastRoundScore = lastRoundScore;
	}

	public boolean isNeedReturnDiamond() 
	{
		return isNeedReturnDiamond;
	}

	public void setNeedReturnDiamond(boolean isNeedReturnDiamond) 
	{
		this.isNeedReturnDiamond = isNeedReturnDiamond;
	}
	
	
}
