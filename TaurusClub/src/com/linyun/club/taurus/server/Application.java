package com.linyun.club.taurus.server;

import org.apache.log4j.Logger;

import com.linyun.bottom.container.Container;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.club.taurus.service.BackGroundServer;
import com.linyun.club.taurus.service.ChatServer;
import com.linyun.club.taurus.service.ClubGameServer;
import com.linyun.club.taurus.service.ClubServer;
import com.linyun.club.taurus.service.HundredsTaurusServer;
import com.linyun.middle.common.taurus.room.HundredsTaurusRoom;

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
		
		/*******************************************************ClubServer******************************************************************/
		ClubServer clubServer = new ClubServer();
		clubServer.init();
		Container.registerServer("ClubServer", clubServer);
		/**--------------------Club-----------------------*/
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Enter_Lobby_Club, "ClubServer/enterLobbyClub");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Leave_Lobby_Club, "ClubServer/leaveLobbyClub");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Enter_Club, "ClubServer/enterClub");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Leave_Club, "ClubServer/leaveClub");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Get_Club_All_Room, "ClubServer/getClubAllRoom");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Action_Create_Club, "ClubServer/actionCreateClub");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Action_Del_Club, "ClubServer/actionDelClub");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Action_Apply_Join_Club, "ClubServer/actionApplyJoinClub");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Get_Club_Data, "ClubServer/getClubDetail");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Get_All_Clubs, "ClubServer/getAllClubs");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Search_Club, "ClubServer/searchClub");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Action_Exit_Club, "ClubServer/actionExitClub");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Get_User_List, "ClubServer/getClubUserList");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Serch_User, "ClubServer/searchUser");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Action_Invite_Join_Club, "ClubServer/actionInviteJoinClub");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Action_Handle_Apply, "ClubServer/actionHandleApply");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Action_Kick_Member, "ClubServer/actionKickMember");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Action_Set_Club_ConfigInfo, "ClubServer/actionSetClubConfigInfo");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Get_Club_User_Msg, "ClubServer/getClubUserMsg");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Read_Club_User_Msg, "ClubServer/readClubUserMsg");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Check_User_New_Msg, "ClubServer/checkUserUnreadMsg");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Get_Club_Game_Record, "ClubServer/getClubGameRecord");
		Container.registerServerPath("" + ClubServer.Protocol_Cli_Record_Detail, "ClubServer/getGameRecordDetail");
		
		/**-------------------百人牛牛游戏---------------------*/
		Container.registerServer("hundredsTaurusServer", new HundredsTaurusServer());
		Container.registerServerPath("" +HundredsTaurusServer.PROTOCOL_FIELD_INFO, "hundredsTaurusServer/getFieldInfo");
		Container.registerServerPath("" +HundredsTaurusServer.PROTOCOL_ENTER_ROOM, "hundredsTaurusServer/enter");
		Container.registerServerPath("" +HundredsTaurusServer.PROTOCOL_WAYBILL, "hundredsTaurusServer/getWayBill");
		Container.registerServerPath("" +HundredsTaurusServer.PROTOCOL_LASTROUND_REPLAY, "hundredsTaurusServer/lastRoundReplay");
		Container.registerServerPath("" +HundredsTaurusServer.PROTOCOL_PLAYER_LIST_INFO, "hundredsTaurusServer/getPlayersInfo");
		Container.registerServerPath("" +HundredsTaurusServer.PROTOCOL_SELF_BET, "hundredsTaurusServer/bet");
		Container.registerServerPath("" +HundredsTaurusServer.PROTOCOL_EXIT_ROOM, "hundredsTaurusServer/exit");
		Container.registerServerPath("" +HundredsTaurusServer.PROTOCOL_RECONNECT, "hundredsTaurusServer/reconnect");
		
		
		/**--------------------游戏牌局-----------------------*/
		Container.registerServer("gameServer", new ClubGameServer());
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_HEART, "gameServer/heart");
		
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_Cli_Room_Enter, "gameServer/enterRoom");
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_Cli_Room_Exit, "gameServer/exitRoom");
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_Cli_Table_SitDown, "gameServer/enterTable");
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_Cli_Table_Exit, "gameServer/exitTable");
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_Cli_Room_Dissolution_Apply, "gameServer/applyDissolution");
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_Cli_Room_Dissolution_Choice, "gameServer/chooseDissolution");
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_Cli_Owner_Dissolution, "gameServer/roomOwnerDissolution");
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_Cli_Start, "gameServer/startGame");
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_Cli_Ready, "gameServer/ready");
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_Cli_Banker_Choose_BaseCoin, "gameServer/bankerChooseBaseCoin");
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_Cli_Bet_Coin, "gameServer/bet");
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_Cli_Open_Cards, "gameServer/openCards");
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_Cli_Rob_Banker, "gameServer/robBanker");
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_Cli_Auto_Action, "gameServer/autoAction");
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_Cli_Last_Round_Index, "gameServer/getLastRoundIndex");
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_Cli_FIXED_Banker_Close_Game, "gameServer/fixedBankerCloseGame");
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_Cli_Create_Club_Room, "gameServer/createClubRoom");
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_Cli_Get_Club_Amount, "gameServer/getClubMemberAmountDetail");
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_Cli_reconnect, "gameServer/reconnect");
		Container.registerServerPath("" + ClubGameServer.PROTOCOL_Cli_reconnect_out, "gameServer/outReconnect");
		Container.registerServerPath("" + ClubGameServer.Protocol_Cli_Club_Hall_Reconnect, "gameServer/ClubHallReconnect");
		/******************************************************************************************************************************/
		Container.registerServerPath(""+ ClubGameServer.PROTOCOL_Cli_Get_BuyScore_Info , "gameServer/getBuyScoreInfo");
		Container.registerServerPath(""+ ClubGameServer.PROTOCOL_Cli_BuyScore_SitDown , "gameServer/buyScoreAndSitDown");
		Container.registerServerPath(""+ ClubGameServer.PROTOCOL_Cli_Gaming_BuyScore , "gameServer/playerGamingBuyScore");
		Container.registerServerPath(""+ ClubGameServer.PROTOCOL_Cli_Cancel_BuyScore , "gameServer/cancelBuyScore");
		
		Container.registerServerPath(""+ ClubGameServer.PROTOCOL_Cli_GetDetailRecord, "gameServer/getDetailRecord");

		
		
		Container.registerServer("chatServer", new ChatServer());
		Container.registerServerPath("" + ChatServer.PROTOCOL_Cli_Room_Send_Msg, "chatServer/sendChatMsg");
		/*********************************************************BackGroundServer***************************************************************/
		Container.registerServer("backGroundServer", new BackGroundServer());
		
	}
}
