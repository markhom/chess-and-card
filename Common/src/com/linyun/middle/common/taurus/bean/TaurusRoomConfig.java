package com.linyun.middle.common.taurus.bean;

import com.linyun.common.entity.PrivateRoom;
import com.linyun.common.taurus.eum.AllCompareBaseScoreType;
import com.linyun.common.taurus.eum.BankerMode;
import com.linyun.common.taurus.eum.BaseScoreType;
import com.linyun.common.taurus.eum.MostRobBanker;
import com.linyun.common.taurus.eum.PlayerInjection;
import com.linyun.common.taurus.eum.RoomPayMode;
import com.linyun.common.taurus.eum.TimesMode;
import com.linyun.common.taurus.eum.UpBankerScore;


public class TaurusRoomConfig 
{
	/**上庄模式*/
	private BankerMode bankerMode;
	/** 游戏底分,通比牛牛时，不用此选项 */
	private BaseScoreType baseScore;
	/** 俱乐部建房时选择的游戏底分*/
	private int clubRoomBaseScore;
	/** 通比牛牛的游戏底分 */
	private AllCompareBaseScoreType allCompareBaseScore;
	/** 游戏局数 */
	private byte roundNum;
	/** 房费支付方式 */
	private RoomPayMode payMode;
	/** 翻倍规则 */
	private TimesMode timesMode;
	/** 特殊牌型的配置选择 */
	private RoomConfigSpecConfig specConfig;
	/** 闲家推注 , 当庄家模式为通比牛牛的时候，此选项的值没有意义，客户端不展示*/
	private PlayerInjection playerInjection;
	/** 高级选项 */
	private AdvancedOptions advancedOptions;
	/** 最大抢庄, 当庄家模式为明牌抢庄的时候所特有，其他模式时，此选项值没有意义，客户端不展示  */
	private MostRobBanker mostRobBanker;
	/** 上庄分数，当庄家模式为固定庄家的时候所特有，其他模式时，此选项值没有意义，客户端不展示 */
	private UpBankerScore upBankerScore;
	
	/*新版H5新加的房间配置*/
	private int gameTime;//游戏时长
	private int joinGameScoreLimit;//俱乐部房间加入游戏最低积分
	
	public TaurusRoomConfig(PrivateRoom db_room)
	{
		this.bankerMode = BankerMode.ValueOf(db_room.getUpBankerMode());
		this.allCompareBaseScore = AllCompareBaseScoreType.ValueOf(db_room.getAllCompareBaseScore());
		this.playerInjection = PlayerInjection.ValueOf(db_room.getPlayerInjection());
		this.roundNum = db_room.getRoundNum();
		this.payMode = RoomPayMode.ValueOf(db_room.getPayMode());
		this.timesMode = TimesMode.ValueOf(db_room.getTimesMode());
		this.specConfig = new RoomConfigSpecConfig();
		this.specConfig.setAllFace(db_room.getAllFace()==1);
		this.specConfig.setBomb(db_room.getBomb()==1);
		this.specConfig.setAllSmall(db_room.getAllSmall()==1);
		this.advancedOptions = new AdvancedOptions();
		this.advancedOptions.setNoEnter(db_room.getNoEnter()==1);
		this.advancedOptions.setNoShuffle(db_room.getNoShuffle()==1);
		this.mostRobBanker = MostRobBanker.ValueOf(db_room.getMostRobBanker());
		this.upBankerScore = UpBankerScore.ValueOf(db_room.getUpBankerScore());
		this.gameTime = db_room.getGameTime();
		this.joinGameScoreLimit = db_room.getJoinGameScoreLimit();
		this.baseScore = this.gameTime == 0 ? BaseScoreType.ValueOf(db_room.getBaseScore()) : BaseScoreType.MODE_SCROLL_SELECTED;
		this.clubRoomBaseScore = db_room.getBaseScore();
	}
	
	public void init(PrivateRoom db_room)
	{
		this.bankerMode = BankerMode.ValueOf(db_room.getUpBankerMode());
		this.allCompareBaseScore = AllCompareBaseScoreType.ValueOf(db_room.getAllCompareBaseScore());
		this.playerInjection = PlayerInjection.ValueOf(db_room.getPlayerInjection());
		this.roundNum = db_room.getRoundNum();
		this.payMode = RoomPayMode.ValueOf(db_room.getPayMode());
		this.timesMode = TimesMode.ValueOf(db_room.getTimesMode());
		this.specConfig.setAllFace(db_room.getAllFace()==1);
		this.specConfig.setBomb(db_room.getBomb()==1);
		this.specConfig.setAllSmall(db_room.getAllSmall()==1);
		this.advancedOptions.setNoEnter(db_room.getNoEnter()==1);
		this.advancedOptions.setNoShuffle(db_room.getNoShuffle()==1);
		this.mostRobBanker = MostRobBanker.ValueOf(db_room.getMostRobBanker());
		this.upBankerScore = UpBankerScore.ValueOf(db_room.getUpBankerScore());
		this.gameTime = db_room.getGameTime();
		this.joinGameScoreLimit = db_room.getJoinGameScoreLimit();
		this.baseScore = this.gameTime == 0 ? BaseScoreType.ValueOf(db_room.getBaseScore()) : BaseScoreType.MODE_SCROLL_SELECTED;
		this.clubRoomBaseScore = db_room.getBaseScore();
	}
	
	public void clear()
	{
		this.bankerMode = null;
		this.allCompareBaseScore = null;
		this.baseScore = null;
		this.playerInjection = null;
		this.roundNum = 0;
		this.payMode = null;
		this.timesMode = null;
		this.specConfig.clear();
		this.advancedOptions.clear();
		this.mostRobBanker = null;
		this.upBankerScore = null;
		this.gameTime = 0;
		this.joinGameScoreLimit = 0;
	}
	
	public BankerMode getBankerMode()
	{
		return bankerMode;
	}

	public BaseScoreType getBaseScore()
	{
		return baseScore;
	}
	
	public byte getRoundNum()
	{
		return roundNum;
	}
	public void setRoundNum(byte roundNum)
	{
		this.roundNum = roundNum;
	}
	
	public RoomPayMode getPayMode()
	{
		return payMode;
	}
	
	public TimesMode getTimesMode()
	{
		return timesMode;
	}
	
	public RoomConfigSpecConfig getSpecConfig()
	{
		return specConfig;
	}

	public PlayerInjection getPlayerInjection()
	{
		return playerInjection;
	}

	public AdvancedOptions getAdvancedOptions()
	{
		return advancedOptions;
	}
	
	public MostRobBanker getMostRobBanker()
	{
		return mostRobBanker;
	}

	public UpBankerScore getUpBankerScore()
	{
		return upBankerScore;
	}
	
	public AllCompareBaseScoreType getAllCompareBaseScore()
	{
		return allCompareBaseScore;
	}

	public int getGameTime() {
		return gameTime;
	}

	public void setGameTime(int gameTime) {
		this.gameTime = gameTime;
	}

	public int getJoinGameScoreLimit() {
		return joinGameScoreLimit;
	}

	public void setJoinGameScoreLimit(int joinGameScoreLimit) {
		this.joinGameScoreLimit = joinGameScoreLimit;
	}

	public int getClubRoomBaseScore() {
		return clubRoomBaseScore;
	}

	public void setClubRoomBaseScore(int clubRoomBaseScore) {
		this.clubRoomBaseScore = clubRoomBaseScore;
	}
	
	
}
