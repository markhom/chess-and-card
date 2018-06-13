package com.linyun.middle.common.taurus.room;

import com.linyun.bottom.container.GameRoomImpl;
import com.linyun.common.entity.PrivateRoom;
import com.linyun.common.taurus.eum.GameStatus;
import com.linyun.middle.common.taurus.bean.TaurusRoomConfig;

public class TaurusRoom extends GameRoomImpl
{
	public static final int ROOM_UP_PLAYER_COUNT = 100;
	
	/** 房间内的玩家数量 -- 备用 */
	private int playerCount;
	/** 房主Id,在开始房间的时候校验 */
	private int roomOwnerId;
	/** 用于保存游戏配置信息 */
	private TaurusRoomConfig config;
	/** 游戏是否开始 */
	private GameStatus gameStatus;
	/**房间的俱乐部Id*/
	private int clubId ;
	/**/

	public TaurusRoom(PrivateRoom db_room)
	{
		super(db_room.getRoomNum());
		this.roomOwnerId = db_room.getRoomOwnerId();
		this.clubId = db_room.getClubId();
		this.config = new TaurusRoomConfig(db_room);
		this.gameStatus = GameStatus.GAME_STATUS_INIT;
	}
	
	public void Init(PrivateRoom db_room)
	{   
		this.roomId = db_room.getRoomNum();
		this.roomOwnerId = db_room.getRoomOwnerId();
		this.clubId = db_room.getClubId();
		this.config.init(db_room);
		this.gameStatus = GameStatus.GAME_STATUS_INIT;
	}

	public int getPlayerCount()
	{
		return playerCount;
	}
	public void addPlayerCount()
	{
		++playerCount;
	}
	public void subPlayerCount()
	{
		--playerCount;
	}

	public int getRoomOwnerId()
	{
		return roomOwnerId;
	}

	public TaurusRoomConfig getConfig()
	{
		return config;
	}
	public void setConfig(TaurusRoomConfig config)
	{
		this.config = config;
	}

	public GameStatus getGameStatus()
	{
		return gameStatus;
	}
	public void setGameStatus(GameStatus gameStatus)
	{
		this.gameStatus = gameStatus;
	}

	public boolean isGameStart()
	{
		return this.gameStatus != GameStatus.GAME_STATUS_INIT;
	}
	
	@Override
	public void clear()
	{   
		this.clubId = 0 ;
		this.roomOwnerId = 0;
		this.gameStatus = null;
		this.config.clear();
		
		super.clear();
	}

	public int getClubId() {
		return clubId;
	}

	public void setClubId(int clubId) {
		this.clubId = clubId;
	}
	
	
}
