/**
 * 
 */
package com.linyun.club.taurus.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.linyun.bottom.log.LoggerFactory;
import com.linyun.club.taurus.engine.GameEngine;
import com.linyun.club.taurus.engine.HundredsTaurusEngine;
import com.linyun.club.taurus.task.GameTimer;
import com.linyun.common.entity.FieldConfig;
import com.linyun.middle.common.taurus.room.HundredsTaurusRoom;
import com.linyun.middle.common.taurus.table.HundredsTaurusTable;

/**
 * @author liangbingbing
 */
public class GameRoomManager
{
	private static Logger logger = LoggerFactory.getLogger(GameRoomManager.class);
	private static ConcurrentHashMap<String, HundredsTaurusTable> userTableMap = new ConcurrentHashMap<String, HundredsTaurusTable>();
	private static Map<Integer, HundredsTaurusRoom> roomMap = new HashMap<Integer, HundredsTaurusRoom>();

	private static GameRoomManager gameManager =  new GameRoomManager();
	/** 私有构造函数，禁止实例化 */
	private GameRoomManager()
	{
		CreateAllRoom();
	}

	public static GameRoomManager getInstance()
	{
		return gameManager;
	}

	public void bindUserTable(String userId, HundredsTaurusTable table)
	{
		if(table != null)
		{
			userTableMap.put(userId, table);	
		}
	}
	

	public void unbindUserTable(String userId)
	{
		userTableMap.remove(userId);
	}

	
	
	public HundredsTaurusTable getUserTable(String userId)
	{
		return userTableMap.get(userId);
	}
	


	public void bindRoom(int tableId, HundredsTaurusRoom room)
	{
		roomMap.put(tableId, room);
	}

	public HundredsTaurusRoom getRoom(int tableId)
	{
		
		return roomMap.get(tableId);
	}
	
	public void removeRoom(int tableId)
	{
		roomMap.remove(tableId);
	}
	
	private void CreateAllRoom()
	{
		List<FieldConfig> configs = FieldConfigManager.getInstance().configs;
		for(FieldConfig config : configs)
		{
			
			HundredsTaurusRoom gameRoom = new HundredsTaurusRoom(config.getTypeId(), config.getTypeId());
			HundredsTaurusTable table = new HundredsTaurusTable(config.getTypeId());
			GameTableManager.getInstance().addTable(table);
			roomMap.put(gameRoom.getRoomId(), gameRoom);
			GameEngine engine = new HundredsTaurusEngine(table, config, gameRoom);
			gameRoom.put(GameEngine.GAME_ENGINE, engine);
			GameTimer.getInstance().addEngine(engine);
				
		}
		
	}
	
	public List<HundredsTaurusRoom> getAllRooms()
	{
		List<HundredsTaurusRoom> rooms = new ArrayList<HundredsTaurusRoom>();
		
		for (Entry<Integer, HundredsTaurusRoom> entry: roomMap.entrySet())
		{
			HundredsTaurusRoom room = entry.getValue();
			rooms.add(room);
		}

		return rooms;
	}
}
