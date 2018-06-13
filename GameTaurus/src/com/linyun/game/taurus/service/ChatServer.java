package com.linyun.game.taurus.service;

import org.apache.log4j.Logger;

import com.linyun.bottom.common.exception.GameException;
import com.linyun.bottom.container.GameRoom;
import com.linyun.bottom.handler.SocketRequest;
import com.linyun.bottom.handler.SocketResponse;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.util.InputMessage;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.middle.common.taurus.service.BaseServer;


public class ChatServer extends BaseServer
{
	private static Logger logger = LoggerFactory.getLogger(ChatServer.class);
	
	public static final short PROTOCOL_Cli_Send_Msg = 2001;
	public static final short PROTOCOL_Ser_Send_Msg = 2011;

	public ChatServer()
	{
	}

	//
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
				throw new GameException(GameException.ROOM_NOT_EXIST, "聊天的时候找不到房间，userId =" + userId);
			}
			synchronized (room)
			{
				om.putString(userId);
				om.putByte(type);
				om.putString(msg);
				room.sendMessage(PROTOCOL_Ser_Send_Msg, om);
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
