package com.linyun.middle.common.taurus.table;

import java.io.Serializable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.linyun.common.entity.User;

/**
*  @Author walker
*  @Since 2018年5月23日
**/

public class HundredsTaurusTable implements Serializable
{

	private static final long serialVersionUID = 1L;
	
	private final ReadWriteLock rwlock = new ReentrantReadWriteLock(); 
	
	/**  桌号 */
	private int tableId;
	
	/** 庄家座位  可能为机器人 可能为玩家*/
	private HundredsTaurusSeat bankerSeat;
	
	//private HundredsTaurusSeat[] seats ;
	
	//private int realPlayer; //在桌上占据位置的玩家
	
	//private boolean isCanEnter; //桌子是否坐满人
	
	/** 房间类型 */
	private int type;
	
	private int player1_bet_total;
	
	private int player2_bet_total;
	
	private int player3_bet_total;
	

	
	public HundredsTaurusTable(int _roomNum)
	{
		this.tableId = _roomNum;
		/**
		 * 0号位置固定为庄家   另外八个位置1-8闲家   其余玩家位置用-1标记
		 * */
		this.bankerSeat = new HundredsTaurusSeat(0);
		
	}
	
	/*public boolean addPlayer(User user)
	{
		synchronized(seats)
		{
			if(!this.isCanEnter)
			{
				return false;
			}
			for(int i=0; i<TABLE_SEAT_NUM; ++i)
			{
				if (!seats[i].isCanSitDown())
				{
					continue;
				}
					
				seats[i].setCanSitDown(false);
				seats[i].setUser(user);
					
				++realPlayer;
					
				if (realPlayer==TABLE_SEAT_NUM)
				{
					this.isCanEnter = false;
				}
				return true;
			}
			
			return false;
			
		}
	}
	
	
	public boolean removePlayer(String playerId)
	{
		synchronized(seats)
		{
			for (int i=0; i<TABLE_SEAT_NUM; ++i)
			{
				if (seats[i].isCanSitDown())
				{
					continue;
				}
				
				if (seats[i].getUser().getUserId().equals(playerId))
				{
					seats[i].setCanSitDown(true);
					seats[i].setUser(null);
					--realPlayer;
					
					this.isCanEnter = true;
					return true;
				}
			}
			return false;
		}
	}
	
	//找到返回位置  没找到返回-1
	public int getUserLocation(String userId)
	{
		synchronized (seats)
		{
			for (int i=0; i<TABLE_SEAT_NUM; ++i)
			{
				if (seats[i].isCanSitDown())
				{
					continue;
				}
				if (seats[i].getUser().getUserId().equals(userId))
				{
					return seats[i].getId();
				}
			}
			return -1;
		}
	}
		
	public HundredsTaurusSeat getUserSeat(String userId)
	{
		synchronized (seats)
		{
			for (int i=0; i<TABLE_SEAT_NUM; ++i)
			{
				if (seats[i].isCanSitDown())
				{
					continue;
				}
				if (seats[i].getUser().getUserId().equals(userId))
				{
					return seats[i];
				}
				}
			return null;
		}
	}
	public HundredsTaurusSeat getSeat(int locationId)
	{
		synchronized(seats)
		{
			if (locationId>8 || locationId<1)
			{
				return null;
			}
			return seats[locationId-1];
		}
	}
	
	public HundredsTaurusSeat[] getSeats()
	{
		return seats;
	}
		*/

	public int getTableId() 
	{
		return tableId;
	}

	public void setTableId(int tableNum) 
	{
		this.tableId = tableNum;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}
	
	public void addBankerUser(User user)
	{
		synchronized (this) 
		{
			this.bankerSeat.setUser(user);
		}
	}
	
	public void removeBankerUser()
	{
		synchronized (this) 
		{
			this.bankerSeat.clear();
		}
	}
	public User getBankerUser()
	{
		return this.bankerSeat.getUser();
	}
	public boolean isBanker(String userId)
	{
		User userBanker = this.bankerSeat.getUser();
		if (userBanker == null)
		{
			return false;
		}
		
		return userBanker.getUserId().equals(userId);
	}

	public int getPlayer1_bet_total() 
	{
		rwlock.readLock().lock();
		try
		{
		   return player1_bet_total;
		}finally
		{
			rwlock.readLock().unlock();
		}
		
	}

	public void setPlayer1_bet_total(int player1_bet_total) 
	{
		rwlock.writeLock().lock();
		try
		{
			this.player1_bet_total += player1_bet_total;
		}finally
		{
			rwlock.writeLock().unlock();
		}
		
	}

	public int getPlayer2_bet_total() 
	{
		rwlock.readLock().lock();
		try
		{
			return player2_bet_total;
		}finally
		{
			rwlock.readLock().unlock();
		}
		
	}

	public void setPlayer2_bet_total(int player2_bet_total) 
	{
		rwlock.writeLock().lock();
		try
		{
			this.player2_bet_total += player2_bet_total;
		}finally
		{
			rwlock.writeLock().unlock();
		}
		
	}

	public int getPlayer3_bet_total() 
	{
		rwlock.readLock().lock();
		try
		{
			return player3_bet_total;
		}finally
		{
			rwlock.readLock().unlock();
		}
	}

	public void setPlayer3_bet_total(int player3_bet_total) 
	{
		rwlock.writeLock().lock();
		try
		{
			this.player3_bet_total += player3_bet_total;
		}finally
		{
			rwlock.writeLock().unlock();
		}
		
	}

	public  void reset()
	{
		this.player1_bet_total=0;
		this.player2_bet_total=0;
		this.player3_bet_total=0;
	}

}
