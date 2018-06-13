package com.linyun.data.mapper;

import java.util.HashMap;
import java.util.List;

import com.linyun.common.entity.PrivateRoom;

public interface PrivateRoomMapper {
	
	//创建房间
	void  createPrivateRoom(PrivateRoom room);
	 
	
	PrivateRoom getPrivateRoom(int roomNum);
	
	//删除房间
	void deletePrivateRoom(int roomNum);
	
	//启动服务器清空房间
	void deleteAllRooms();
	
	//开始游戏
	void startGame(int roomNum);
	
	//获取超时的房间号的集合
	List<String> getTimeoutList();
	
	//获取俱乐部里面的超时房间号集合
	List<Integer> getClubTimeOutRoomList();
	
	void updateRoundNum(HashMap<String, Object> map);
	
	void addOnePlayer(int roomNum);
	
	void removeOnePlayer(int roomNum);
	
	//获取俱乐部房间列表集合
	List<PrivateRoom> getClubRoomList(int clubId);
}
