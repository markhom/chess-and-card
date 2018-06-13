package com.linyun.common.action;

public interface ClubRoomAction
{
	void bindRoomClub(int roomId, int clubId);
	
	void unBindRoomClub(int roomId);
	
	int getRoomClub(int roomId);
}
