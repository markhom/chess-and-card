package com.linyun.common.action.impl;

import com.linyun.bottom.container.Container;
import com.linyun.bottom.container.GameSession;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.club.taurus.service.ClubGameServer;
import com.linyun.common.action.TaurusClubGameAction;
import com.linyun.middle.common.taurus.club.BaseClubServer;
import com.linyun.middle.common.taurus.club.TaurusClub;
import com.linyun.middle.common.taurus.room.TaurusRoom;
import com.linyun.middle.common.taurus.service.BaseServer;

public class TaurusClubGameActionImpl implements TaurusClubGameAction 
{   
	
	@Override
	public int needReconnectClubId(String userId) 
	{
		
		int clubId = 0 ;
		if (userId == null || userId.isEmpty())
		{
			return clubId ;
		}
		
		/**进入了俱乐部大厅，没有进入俱乐部，杀进程时不需要重连,单点登录检测*/
		if(BaseClubServer.getUserClub(userId)==null)
		{
			try
			{   
				OutputMessage om = new OutputMessage(true);
				GameSession session = BaseServer.getUserSession(userId);
				if(session != null)
				{   
					//单点登录发送的协议
					session.sendMessage(ClubGameServer.PROTOCOL_Ser_Outconnect, om);
					//BaseServer相关移除
					BaseServer.removeUser(session);
					Container.removeSessionChannel(session.getSessionId());
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			} 
		}
		/**加入了俱乐部，但是没有进入房间,杀进程时不需要重连,单点登录检测*/
		else if(BaseClubServer.getUserClub(userId) != null && (BaseServer.getUserRoom(userId) == null ))
		{
			try
			{
				OutputMessage om = new OutputMessage(true);
				TaurusClub taurusClub = BaseClubServer.getUserClub(userId);
				GameSession session = BaseServer.getUserSession(userId);
				if(session != null)
				{
					//单点登录发送的协议
					session.sendMessage(ClubGameServer.PROTOCOL_Ser_Outconnect, om);
					//BaseServer 移除
					BaseServer.removeUser(session);
					//club 移除
					taurusClub.removeUserSession(userId);
					Container.removeSessionChannel(session.getSessionId());
					//club unbind
					BaseClubServer.unbindUserClub(userId);
				}
				
			}
			catch (Exception e)
			{
				e.printStackTrace();
			} 
			
		}
		/**加入了房间，但是没有加入桌子,杀进程时不需要重连,单点登录检测*/
		else if(BaseServer.getUserRoom(userId) != null && (BaseServer.getUserTable(userId) == null))
		{
			try 
			{
				OutputMessage om = new OutputMessage(true);
				TaurusClub taurusClub = BaseClubServer.getUserClub(userId);
				TaurusRoom taurusRoom = BaseServer.getUserRoom(userId);
				GameSession session = BaseServer.getUserSession(userId);
				if(session != null)
				{ 
					//单点登录发送的协议
					session.sendMessage(ClubGameServer.PROTOCOL_Ser_Outconnect, om);
					//BaseServer 移除
					BaseServer.removeUser(session);
					//club 移除
					taurusClub.removeUserSession(userId);
					//room 移除
					taurusRoom.removeSession(userId);
					Container.removeSessionChannel(session.getSessionId());
					//club unbind
					BaseClubServer.unbindUserClub(userId);
					// room unbind
					BaseServer.unbindUserRoom(userId);
				}
				
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			
		}
		/**已在桌子坐下,杀进程时需要重连,单点登录检测*/
		else if(BaseServer.getUserTable(userId) != null)
		{
			try 
			{
				OutputMessage om = new OutputMessage(true);
				TaurusClub taurusClub = BaseClubServer.getUserClub(userId);
				TaurusRoom taurusRoom = BaseServer.getUserRoom(userId);
				GameSession session = BaseServer.getUserSession(userId);
				if(session != null)
				{ 
					//单点登录发送的协议
					session.sendMessage(ClubGameServer.PROTOCOL_Ser_Outconnect, om);
					//BaseServer 移除
					BaseServer.removeUser(session);
					//club 移除
					taurusClub.removeUserSession(userId);
					//room 移除
					taurusRoom.removeSession(userId);
					Container.removeSessionChannel(session.getSessionId());
					
				}
				clubId = taurusClub.getClubId();
				
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		return clubId ;
	}

}
