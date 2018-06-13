package com.linyun.common.action;

import java.util.List;

import com.linyun.common.entity.PrivateRoom;

public interface PrivateRoomAction 
{	 
	//创建房间
	void createPrivateRoom(PrivateRoom room);
	 
	//根据房间号获取房间
	PrivateRoom getPrivateRoom(int roomNum);
	//根据房间号获取房间
	PrivateRoom getPrivateRoom(String roomNum);
	
	//房间解散后，删除房间
	void deletePrivateRoom(int roomNum);
	
	//启动服务器，清空房间
	void deleteAllRooms();
	 
	//游戏开始，设置房间的状态为开始
	void startGame(int roomNum);
	
	//获取超时需要解散的房间号列表
	List<String> getTimeoutRoomList();
	
	//获取俱乐部房间的超时房间号
	List<Integer> getClubTimeOutRoomList();
	
	//设置房间的总局数，在轮庄模式开始游戏的时候会用到
	void updateRoundNum(int roomNum, int roundNum);
	
	void addOnePlayer(int roomNum);
	
	void subOnePlayer(int roomNum);
	
	//查询俱乐部所有房间列表
	List<PrivateRoom> getClubRoomList(int clubId);
	
}
