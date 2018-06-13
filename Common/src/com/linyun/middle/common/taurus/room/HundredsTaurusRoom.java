package com.linyun.middle.common.taurus.room;

import com.linyun.bottom.container.GameRoomImpl;

/**
*  @Author walker
*  @Since 2018年5月24日
**/

public class HundredsTaurusRoom extends GameRoomImpl
{

	private int roomType;
	
	private GameStatus gameStatus;
	
	public  enum GameStatus
	{
		STARTBET(0),
		WAITLOTTERY(1),
		DEALPOCKER(2);
		public int value;
		
		GameStatus(int _value)
		{
			this.value = _value;
		}
	}
	
	
	public HundredsTaurusRoom(int _roomId,int roomType) 
	{
		super(_roomId);
		this.roomType = roomType;
	}
	
	public void put(String key, Object value) {
		roomMap.put(key, value);
	}

	public Object getObject(String key) {
		return roomMap.get(key);
	}

	public void remove(String key) {
		roomMap.remove(key);
	}

	public int getRoomType() {
		return roomType;
	}

	public void setRoomType(int roomType) {
		this.roomType = roomType;
	}

	public GameStatus getGameStatus() {
		return gameStatus;
	}

	public void setGameStatus(GameStatus gameStatus) {
		this.gameStatus = gameStatus;
	}
	
	

}
