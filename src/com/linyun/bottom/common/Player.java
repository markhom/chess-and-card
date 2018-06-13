package com.linyun.bottom.common;

public class Player
{
	protected String userId;
	protected String tableId;
	
	protected Player(String tableId, String userId)
	{
		this.userId = userId;
		this.tableId = tableId;
	}
	public String getPlayerId()
	{
		return userId;
	}

	public String getGameTableId()
	{
		return this.tableId;
	}
}
