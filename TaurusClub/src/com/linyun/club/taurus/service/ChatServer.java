package com.linyun.club.taurus.service;

import org.apache.log4j.Logger;

import com.linyun.bottom.common.exception.GameException;
import com.linyun.bottom.container.GameRoom;
import com.linyun.bottom.handler.SocketRequest;
import com.linyun.bottom.handler.SocketResponse;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.util.InputMessage;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.middle.common.taurus.club.BaseClubServer;
import com.linyun.middle.common.taurus.club.TaurusClub;

public class ChatServer extends BaseClubServer
{
	private static Logger logger = LoggerFactory.getLogger(ChatServer.class);
	/**
	 * 俱乐部房间聊天协议
	 * */
	public static final short PROTOCOL_Cli_Room_Send_Msg = 2001;
	public static final short PROTOCOL_Ser_Room_Send_Msg = 2011;
	/**
	 * 俱乐部聊天协议---整个俱乐部可见
	 * */
	public static final short PROTOCOL_Cli_Club_Send_Msg = 2021;
	public static final short PROTOCOL_Ser_Club_Send_Msg = 2031;
	
	public void sendChatMsg(SocketRequest request, SocketResponse response)throws GameException
	{
		InputMessage in = request.getInputMessage();
		try
		{
			String userId = in.getUTF();
			byte type = in.getByte();
			String msg = in.getUTF();
			OutputMessage om = new OutputMessage(true);
			GameRoom room = getUserRoom(userId);
			if (room == null)
			{
				throw new GameException(GameException.ROOM_NOT_EXIST, "俱乐部，在游戏内聊天的时候找不到房间，userId =" + userId);
			}
			synchronized (room)
			{
				om.putString(userId);
				om.putByte(type);
				om.putString(msg);
				room.sendMessage(PROTOCOL_Ser_Room_Send_Msg, om);
			}
		} 
		catch (GameException e)
		{
			logger.error(e.getMessage(),e);
		}
		catch (Exception e) 
		{
			logger.error(e.getStackTrace());
		}
	}
	
	public void sendClubChatMsg(SocketRequest request, SocketResponse response)throws GameException
	{
		InputMessage in = request.getInputMessage();
		try
		{
			String userId = in.getUTF();
			byte type = in.getByte();
			String msg = in.getUTF();
			OutputMessage om = new OutputMessage(true);
			TaurusClub taurusClub = getUserClub(userId);
			if (taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST, "俱乐部内聊天时，找不到对应的俱乐部,userId =" + userId);
			}
			
			synchronized (taurusClub)
			{
				om.putString(userId);
				om.putByte(type);
				om.putString(msg);
				taurusClub.sendMessage(PROTOCOL_Ser_Room_Send_Msg, om);
			}
		} 
		catch (GameException e)
		{
			logger.error(e.getMessage(),e);
		}
		catch (Exception e) 
		{
			logger.error(e.getStackTrace());
		}
	}
}
