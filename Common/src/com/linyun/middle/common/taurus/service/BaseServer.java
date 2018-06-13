package com.linyun.middle.common.taurus.service;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import com.linyun.bottom.common.exception.GameException;
import com.linyun.bottom.container.Container;
import com.linyun.bottom.container.GameSession;
import com.linyun.bottom.handler.SocketRequest;
import com.linyun.bottom.handler.SocketResponse;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.common.entity.User;
import com.linyun.middle.common.taurus.room.TaurusRoom;
import com.linyun.middle.common.taurus.server.ActionAware;
import com.linyun.middle.common.taurus.table.TaurusTable;


public class BaseServer extends ActionAware
{
	public static final short PROTOCOL_ERROR = 111;
	public static final short PROTOCOL_HEART = 999;
	public static final short PROTOCOL_UPDATE_USER = 211;
	public static final short PROTOCOL_UPDATE_USER_PROP = 212;
	public static final String USER_ID = "userId";

	public static ConcurrentHashMap<String, String> userSessionMap = new ConcurrentHashMap<String, String>();//用户Id->SessinId
	public static ConcurrentHashMap<String, TaurusTable> tableMap = new ConcurrentHashMap<String, TaurusTable>();//桌子Id->桌子
	public static ConcurrentHashMap<String, TaurusTable> userTableMap = new ConcurrentHashMap<String, TaurusTable>(); //用户Id->桌子
	public static ConcurrentHashMap<String, TaurusRoom> roomMap = new ConcurrentHashMap<String, TaurusRoom>();//房间Id->房间
	public static ConcurrentHashMap<String, TaurusRoom> userRoomMap = new ConcurrentHashMap<String, TaurusRoom>();//用户Id->房间
	
	private static HashMap<String, Semaphore> userSempMap = new HashMap<String, Semaphore>();
	//================================================================
	public static Semaphore getSemp(String userId)
	{
		synchronized (userSempMap)
		{
			Semaphore semp =userSempMap.get(userId);
			if (semp == null)
			{
				semp = new Semaphore(1);
				userSempMap.put(userId, semp);
			}
			return semp;
		}
	}
	public static void clearUserSemp()
	{
		synchronized (userSempMap)
		{
			userSempMap.clear();
		}
	}
	
	//=================================================================
	//上面的桌子Id，房间Id， 为同一值，即为房间号
	// ====================================================userSessionMap
	public static void addUser(String userId, GameSession session)
	{
		userSessionMap.put(userId, session.getSessionId());
		session.put(USER_ID, userId);
	}
	public static void removeUser(GameSession session)
	{
		String userId = (String) session.getObject(USER_ID);
		if (userId != null)
		{
			userSessionMap.remove(userId);
			session.remove(USER_ID);
		}
	}
	public static GameSession getUserSession(String userId)
	{
		String sessionId = userSessionMap.get(userId);
		if (sessionId == null)
		{
			return null;
		}
		else
		{
			return Container.getSessionById(sessionId);
		}
	}
	// ======================================================tableMap
	public static TaurusTable getExistTable(String tableId) throws GameException
	{
		TaurusTable table = tableMap.get(tableId);
		if (table == null)
		{
			throw new GameException(GameException.TABLE_NOT_EXIST, "BaseServer,getTable中，桌子不存在，tableId = " + tableId);
		}
		return table;
	}
	public static TaurusTable getTable(String tableId)
	{
		return tableMap.get(tableId);
	}
	public static void addTable(TaurusTable table)
	{
		if (table == null)
		{
			throw new GameException(GameException.TABLE_NOT_EXIST, "BaseServer,addTable中，table 为null");
		}
		tableMap.put(String.valueOf(table.getTableId()), table);
	}
	public static void removeTable(TaurusTable table)
	{
		tableMap.remove(table.getTableId());
	}
	//===================================================userTableMap
	// 绑定用户和桌子关系
	public static void bindUserTable(String userId, TaurusTable table)
	{
		userTableMap.put(userId, table);
	}
	//解绑定用户和桌子关系
	public static void unbindUserTable(String userId)
	{
		userTableMap.remove(userId);
	}
	public static TaurusTable getUserTable(String userId)
	{
		return userTableMap.get(userId);
	}
	//=======================================================roomMap	
	public static void addRoom(String roomId, TaurusRoom room)
	{
		roomMap.put(roomId, room);
	}
	public static void removeRoom(String roomId)
	{		
		roomMap.remove(roomId);
	}
	public static TaurusRoom getRoom(String roomId)
	{
		return roomMap.get(roomId);
	}
	public static TaurusRoom getRoom(int roomId)
	{
		return roomMap.get(String.valueOf(roomId));
	}
	//===================================================userRoomMap
	// 绑定用户和房间关系
	public static void bindUserRoom(String userId, TaurusRoom room)
	{
		userRoomMap.put(userId, room);
	}
	//解绑定用户和房间关系
	public static void unbindUserRoom(String userId)
	{
		userRoomMap.remove(userId);
	}
	public static TaurusRoom getUserRoom(String userId)
	{
		return userRoomMap.get(userId);
	}
	//================================================================ 广播消息
	protected void boradcastMessage(Short protocolId, OutputMessage om)
	{
		for (GameSession session : Container.getSessions())
		{
			if (session.getStatus() == GameSession.STATUS_CONN)
			{
				session.sendMessage(protocolId, om);
			}
		}
	}
	//========================================other
	public User getExistUser(String userId) throws GameException
	{
		return userAction().getExistUser(userId);
	}
	
	public User getExistUserBySql(String userId) throws GameException
	{
		return userAction().getExistUserBySql(userId);
	}
	
	public User getExistUserNoCareFrozen(String userId) throws GameException
	{
		return userAction().getExistUserNoCareFrozen(userId);
	}
	
	protected void sendError(SocketResponse response, short protocoId, short errorCode)
	{
		OutputMessage om = new OutputMessage(false);
		om.putShort(errorCode);
		response.sendMessage(protocoId, om);
	}
	protected void sendError(SocketResponse response, short protocoId, short errorCode, OutputMessage om)
	{
		response.sendMessage(protocoId, om);
	}
	
	/**
	 * 心跳包
	 */
	public void heart(SocketRequest request, SocketResponse response) throws GameException
	{
		OutputMessage om = new OutputMessage();
		om.putBoolean(true);
		response.sendMessage(PROTOCOL_HEART, om);
		
		GameSession session = request.getSession();
		String userId = (String) session.getObject("userId");
		if (userId == null || userId.isEmpty())
		{
			return;
		}
	}
}
