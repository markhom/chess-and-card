package com.linyun.game.taurus.server;

import org.apache.log4j.Logger;

import com.linyun.bottom.container.Container;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.game.taurus.service.ChatServer;
import com.linyun.game.taurus.service.GameServer;
import com.linyun.game.taurus.service.LobbyServer;


public class Application
{
	private static Logger logger = LoggerFactory.getLogger(Application.class);

	public void init()
	{
		initServer();
	}

	public void initServer()
	{
		logger.info("Init servers......");
		//
		Container.registerServer("gameServer", new GameServer());
		Container.registerServerPath("" + GameServer.PROTOCOL_HEART, "gameServer/heart");
		
		Container.registerServerPath("" + GameServer.PROTOCOL_Cli_Room_Enter, "gameServer/enterRoom");
		Container.registerServerPath("" + GameServer.PROTOCOL_Cli_Room_Exit, "gameServer/exitRoom");
		Container.registerServerPath("" + GameServer.PROTOCOL_Cli_Table_SitDown, "gameServer/enterTable");
		Container.registerServerPath("" + GameServer.PROTOCOL_Cli_Table_Exit, "gameServer/exitTable");
		Container.registerServerPath("" + GameServer.PROTOCOL_Cli_Room_Dissolution_Apply, "gameServer/applyDissolution");
		Container.registerServerPath("" + GameServer.PROTOCOL_Cli_Room_Dissolution_Choice, "gameServer/chooseDissolution");
		Container.registerServerPath("" + GameServer.PROTOCOL_Cli_Owner_Dissolution, "gameServer/roomOwnerDissolution");
		
		Container.registerServerPath("" + GameServer.PROTOCOL_Cli_Start, "gameServer/startGame");
		Container.registerServerPath("" + GameServer.PROTOCOL_Cli_Ready, "gameServer/ready");
		
		Container.registerServerPath("" + GameServer.PROTOCOL_Cli_Banker_Choose_BaseCoin, "gameServer/bankerChooseBaseCoin");
		Container.registerServerPath("" + GameServer.PROTOCOL_Cli_Bet_Coin, "gameServer/bet");
		Container.registerServerPath("" + GameServer.PROTOCOL_Cli_Open_Cards, "gameServer/openCards");
		Container.registerServerPath("" + GameServer.PROTOCOL_Cli_Rob_Banker, "gameServer/robBanker");
		Container.registerServerPath("" + GameServer.PROTOCOL_Cli_Auto_Action, "gameServer/autoAction");
		Container.registerServerPath("" + GameServer.PROTOCOL_Cli_Last_Round_Index, "gameServer/getLastRoundIndex");
		Container.registerServerPath("" + GameServer.PROTOCOL_Cli_FIXED_Banker_Close_Game, "gameServer/fixedBankerCloseGame");
		
		Container.registerServerPath("" + GameServer.PROTOCOL_Cli_reconnect, "gameServer/reconnect");
		Container.registerServerPath("" + GameServer.PROTOCOL_Cli_reconnect_out, "gameServer/outReconnect");
		
		/**********************************************************************************/
		Container.registerServer("lobbyServer", new LobbyServer());
		Container.registerServerPath("" +LobbyServer.PROTOCOL_Cli_BindInviteCode, "lobbyServer/bindInviteCode");
		Container.registerServerPath("" +LobbyServer.PROTOCOL_Cli_CreateRoom, "lobbyServer/createRoom");
		Container.registerServerPath("" +LobbyServer.PROTOCOL_Cli_GetBroadCast, "lobbyServer/getBroadcast");
		Container.registerServerPath("" +LobbyServer.PROTOCOL_Cli_GetContactInfo, "lobbyServer/getContactInfo");
		Container.registerServerPath("" +LobbyServer.PROTOCOL_Cli_GetDetailRecord, "lobbyServer/getDetailRecord");
		Container.registerServerPath("" +LobbyServer.PROTOCOL_Cli_GetMsg, "lobbyServer/getMsg");
		Container.registerServerPath("" +LobbyServer.PROTOCOL_Cli_GetRoomList, "lobbyServer/getRoomList");
		Container.registerServerPath("" +LobbyServer.PROTOCOL_Cli_GetRoundRecord, "lobbyServer/getRoundRecord");
		Container.registerServerPath("" +LobbyServer.PROTOCOL_Cli_GetServerVersion, "lobbyServer/getSerVersion");
		Container.registerServerPath("" +LobbyServer.PROTOCOL_Cli_GetTotalRecord, "lobbyServer/getTotalRecord");
		Container.registerServerPath("" +LobbyServer.PROTOCOL_Cli_JoinRoom, "lobbyServer/joinRoom");
		Container.registerServerPath("" +LobbyServer.PROTOCOL_Cli_ReceiveDissolutionMsg, "lobbyServer/recvDissolutionMsg");
		Container.registerServerPath("" +LobbyServer.PROTOCOL_Cli_RefreshUserInfo, "lobbyServer/refreshUserInfo");
		Container.registerServerPath("" +LobbyServer.PROTOCOL_Cli_VerifyLoginInfo, "lobbyServer/verifyLoginInfo");

		
		Container.registerServer("chatServer", new ChatServer());
		Container.registerServerPath("" + ChatServer.PROTOCOL_Cli_Send_Msg, "chatServer/sendChatMsg");
	}
}
