package com.linyun.common.action.impl;

import com.linyun.bottom.container.Container;
import com.linyun.bottom.container.GameSession;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.common.action.TaurusGameAction;

import com.linyun.game.taurus.service.GameServer;
import com.linyun.middle.common.taurus.room.TaurusRoom;
import com.linyun.middle.common.taurus.service.BaseServer;
import com.linyun.middle.common.taurus.table.TaurusTable;

public class TaurusGameActionImpl implements TaurusGameAction {

	@Override
	public boolean isNeedReconnect(String userId) 
	{
		if (userId == null || userId.isEmpty())
		{
			return false;
		}
		//进入了房间但是没有坐下的玩家，如同一userId在不同设备上登录，踢出此时在房间的玩家
		if(BaseServer.getUserRoom(userId) != null && BaseServer.getUserTable(userId) == null)
		{   
			try
			{
				OutputMessage om = new OutputMessage(true);
				TaurusRoom game_room = BaseServer.getUserRoom(userId);
				
				GameSession session = BaseServer.getUserSession(userId);
				if (session != null)
				{
					session.sendMessage(GameServer.PROTOCOL_Ser_Outconnect, om);
					//BaseServer相关
					BaseServer.removeUser(session);
					Container.removeSessionChannel(session.getSessionId());
				}
				BaseServer.unbindUserRoom(userId);
				//房间
				game_room.removeSession(userId); 
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return  false ;
		}
		
		boolean isNeedReconnect = BaseServer.getUserTable(userId) != null;
		//进入房间，并且坐下的玩家
		if (isNeedReconnect)
		{
			try
			{
				//是否掉线玩家,如果是空则是掉线玩家，否则不是掉线玩家
				if (BaseServer.getUserSession(userId) != null)
				{
					//找到玩家对应的session 以及channel  通过session通知前一个登录的用户被踢出 移除session  
					GameSession session = BaseServer.getUserSession(userId);
					if (session != null)
					{
						OutputMessage om = new OutputMessage(true);
						session.sendMessage(GameServer.PROTOCOL_Ser_Outconnect, om);
						//移除baseServer相关
						BaseServer.removeUser(session);
						Container.removeSessionChannel(session.getSessionId());
					}
					//移除房间相关
					TaurusRoom game_room = BaseServer.getUserRoom(userId);
					game_room.removeSession(userId);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return isNeedReconnect;
	}

	@Override
	public void bindUserTable(String userId)
	{
	      
	    if(userId == null || userId.isEmpty())
	    {
		    return ;
	    }
	    
	    GameSession session = BaseServer.getUserSession(userId);
	    if(session != null)
	    {
	    	OutputMessage om = new OutputMessage(true);
	    	session.sendMessage(GameServer.PROTOCOL_Ser_Frozen, om);
	    	
	    	//进入房间坐下的玩家，冻结时托管处理
	    	TaurusTable taurusTable = BaseServer.getUserTable(userId);
	    	if(taurusTable != null)
	    	{
	    		taurusTable.getUserSeat(userId).setAutoAction(true);
	    	}
	    	else
	    	{   
	    		TaurusRoom game_room = BaseServer.getUserRoom(userId);
	    		if(game_room != null)
	    		{
	    			game_room.removeSession(userId);
	    			BaseServer.unbindUserRoom(userId);
	    		}
	    		BaseServer.removeUser(session);
	    	}
	    	
	    }
	    
	}

	@Override
	public int addOnlineCount()
	{
		return BaseServer.userSessionMap.size();
	}

}
