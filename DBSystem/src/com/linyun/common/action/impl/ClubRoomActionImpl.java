package com.linyun.common.action.impl;

import com.linyun.bottom.cached.RedisResource;
import com.linyun.common.action.ClubRoomAction;

public class ClubRoomActionImpl implements ClubRoomAction
{
	private static final String PREFIX_ROOM_CLUB = "room_club_";
	@Override
	public void bindRoomClub(int roomId, int clubId)
	{
		RedisResource.set(generateId(roomId), String.valueOf(clubId));
	}

	@Override
	public void unBindRoomClub(int roomId)
	{
		RedisResource.del(generateId(roomId));
	}

	@Override
	public int getRoomClub(int roomId)
	{
		return Integer.parseInt(RedisResource.get(generateId(roomId)));
	}
	
	private String generateId(int roomId)
	{
		return PREFIX_ROOM_CLUB + roomId;
	}
	
}
