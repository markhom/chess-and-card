/**
 * Juice
 * com.juice.orange.game.container
 * GameRoom.java
 */
package com.linyun.bottom.container;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.linyun.bottom.util.OutputMessage;


/**
 * @author shaojieque 2013-3-22
 */
public interface GameRoom {
	//
	public static final int ROOM_POOL_SIZE = 2;
	
	/**
	 * get GameRoom id
	 */
	int getRoomId();

	/**
	 * add GameSession
	 */
	void addSession(String userId, GameSession session);
	/**
	 * get GameSession by userId
	 * */
	GameSession getSessionByUserId(String userId);

	boolean ContainUserId(String userId); 
	
	/**
	 * remove GameSession
	 */
	void removeSession(String userId);

	/** get room sessions list*/
	List<GameSession> getSessionList();
	
	/**
	 * send a message to client, flag is identify what message type
	 */
	void sendMessage(short flag, OutputMessage message);
	
	/**
	 * send a message to client, flag is identify what message type
	 */
	void sendMessage(short flag, OutputMessage message, GameSession session);
	
	/**
	 * send a message to client, flag is identify what message type
	 */
	void sendMessage(short flag, OutputMessage message, List<String> userIdList);

	/**
	 * clear GameRoom
	 * */
	void clear();
	 
	/**
	 * add a schedule for GameRoom
	 */
	@SuppressWarnings("rawtypes")
	ScheduledFuture scheduleWithFixedDelay(Runnable runnable, long initialDelay, long period,
			TimeUnit unit);
	
	/**
	 * 定时器任务
	 */
	@SuppressWarnings("rawtypes")
	ScheduledFuture schedule(Runnable runnable, long delay, TimeUnit unit);
}
