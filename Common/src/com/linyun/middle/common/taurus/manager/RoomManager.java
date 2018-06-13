package com.linyun.middle.common.taurus.manager;

import java.util.concurrent.ConcurrentLinkedDeque;

import com.linyun.middle.common.taurus.room.TaurusRoom;

public class RoomManager 
{
	 private static ConcurrentLinkedDeque<TaurusRoom> roomList = new ConcurrentLinkedDeque<TaurusRoom>();
	
	public static void addRoom(TaurusRoom room)
	{
		if (room == null)
		{
			return;
		}
		roomList.add(room);
	}
	
	public static TaurusRoom getRoom()
	{
		if (roomList.isEmpty())
		{
			return null;
		}
		return roomList.removeFirst();
	}
}
