/**
 * Juice
 * com.juice.orange.game.container
 * GameRoomImpl.java
 */
package com.linyun.bottom.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.linyun.bottom.util.OutputMessage;

/**
 * @author shaojieque 
 * 2013-3-22
 */
public class GameRoomImpl extends ScheduledThreadPoolExecutor 
						  implements GameRoom 
{
	public static final int GAME_TYPE_BARRACAT = 0;
	
	public static final int ROOM_TYPE_NORMAL = 1;
	public static final int ROOM_TYPE_ADVANCED = 2;
	public static final int ROOM_TYPE_VISITANT = 3;
	
	protected int roomId;//房间Id,也就是房间号
	
	protected HashMap<String, GameSession> userSessionMap;
	protected Map<String, Object> roomMap;
	protected ReadWriteLock rwLock = new ReentrantReadWriteLock(); 
	
	/**
	 * */
	public GameRoomImpl(int _roomId) 
	{
		super(ROOM_POOL_SIZE);
		this.roomId = _roomId;
		userSessionMap = new HashMap<String, GameSession>(100);
		this.roomMap = new HashMap<String, Object>(10);
	}

	@Override
	public void addSession(String userId, GameSession session) 
	{
		rwLock.writeLock().lock();
		try
		{
			this.userSessionMap.put(userId, session);
		}
		finally
		{
			rwLock.writeLock().unlock();
		}
	}
	
	@Override
	public List<GameSession> getSessionList() 
	{
		rwLock.readLock().lock();
		try
		{
			List<GameSession> sessionList = new ArrayList<GameSession>();
			for (Entry<String, GameSession> entry : userSessionMap.entrySet()) 
			{
				sessionList.add(entry.getValue());
			} 
			return sessionList;
		}
		finally
		{
			rwLock.readLock().unlock();
		}
	}
	
	@Override
	public GameSession getSessionByUserId(String userId)
	{	
		rwLock.readLock().lock();
		try
		{
			return userSessionMap.get(userId);
		}
		finally
		{
			rwLock.readLock().unlock();
		}
	}
	
	@Override
	public void sendMessage(short flag, OutputMessage message) 
	{
		rwLock.readLock().lock();
		try
		{
			checkSession();
			Set<Entry<String, GameSession>> entrySet = userSessionMap.entrySet();
			for (Entry<String, GameSession> entry : entrySet) 
			{
				entry.getValue().sendMessage(flag, message);
			}
		}
		finally
		{
			rwLock.readLock().unlock();
		}
	}
	
	@Override
	public void sendMessage(short flag, OutputMessage message, GameSession session) 
	{
		rwLock.readLock().lock();
		try
		{
			checkSession();
			for (Entry<String, GameSession> entry : userSessionMap.entrySet()) 
			{
				if (session==null || !entry.getValue().getSessionId().equals(session.getSessionId()))
				{
					entry.getValue().sendMessage(flag, message);
				}
			}
		}
		finally
		{
			rwLock.readLock().unlock();
		}
	}
	
	@Override
	public void sendMessage(short flag,  OutputMessage message, List<String> userIdList)
	{
		rwLock.readLock().lock();
		try
		{
			checkSession();
			Set<String> set = userSessionMap.keySet();
			for (String userId : set) 
			{
				if (!userIdList.contains(userId))
				{
					userSessionMap.get(userId).sendMessage(flag, message);
				}
			}
		}
		finally
		{
			rwLock.readLock().unlock();
		}
	}
	
	private void checkSession()
	{
		Iterator<Entry<String, GameSession>> iterator = userSessionMap.entrySet().iterator();
		while (iterator.hasNext())
		{
			Entry<String, GameSession> entry = iterator.next();
			GameSession session = entry.getValue();
			if (session.getStatus() == GameSession.STATUS_UNCONN)
			{
				iterator.remove();
			}
		}
	}
	
	@Override
	public int getRoomId() {
		return roomId;
	}

	@Override
	public void removeSession(String userId) {
		rwLock.writeLock().lock();
		try
		{
			userSessionMap.remove(userId);
		}
		finally
		{
			rwLock.writeLock().unlock();
		}
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ScheduledFuture scheduleWithFixedDelay(Runnable command, long initialDelay, long period, TimeUnit unit) 
	{
		return super.scheduleWithFixedDelay(command, initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) 
	{
		return super.schedule(command, delay, unit);
	}

	@Override
	public boolean ContainUserId(String userId)
	{
		rwLock.readLock().lock();
		try
		{
			return userSessionMap.containsKey(userId);
		}
		finally
		{
			rwLock.readLock().unlock();
		}
	}
	
	public Set<String> getUserIdList()
	{
		rwLock.readLock().lock();
		try
		{
			Set<String> set = new HashSet<String>();
			for (String str:userSessionMap.keySet())
			{
				set.add(str);
			}
			return set;
		}
		finally
		{
			rwLock.readLock().unlock();
		}
	}

	@Override
	public void clear() 
	{
		this.userSessionMap.clear();
		this.roomId = 0;
		this.roomMap.clear();
	}

}
