package com.linyun.middle.common.taurus.protocol;

public class TaurusProtocol
{
	/**---------------------------- 以下协议为客户端发送给服务器的 -------------------------------------*/
	/** 加入房间 */
	public static final short PROTOCOL_Cli_Room_Enter = 7001;
	/** 退出房间 */
	public static final short PROTOCOL_Cli_Room_Exit = 7002;
	/** 进入桌子，坐下*/
	public static final short PROTOCOL_Cli_Table_SitDown = 7003;
	/** 退出桌子，离开 */
	public static final short PROTOCOL_Cli_Table_Exit = 7004;
	/** 申请解散，某个玩家申请解散房间 */
	public static final short PROTOCOL_Cli_Room_Dissolution_Apply = 7005;
	/** 解散选择，其他玩家对申请解散房间做出选择 */
	public static final short PROTOCOL_Cli_Room_Dissolution_Choice = 7006;
	/** 游戏结果，房间解散的时候的总的游戏结果 */
	public static final short PROTOCOL_Cli_Room_Result_Info = 7007;
	/** 开始游戏，只有房主点击开始游戏 */
	public static final short PROTOCOL_Cli_Start = 7008;
	/** 准备,在一局游戏完成后、下一局游戏开始之前可进行的操作 */
	public static final short PROTOCOL_Cli_Ready = 7009;
	/** 庄家选择押注底分情况，在创建房间时，底分模式选择了"庄家选择"时触发 */
	public static final short PROTOCOL_Cli_Banker_Choose_BaseCoin = 7010;
	/** 押注，玩家一局的下注分数 */
	public static final short PROTOCOL_Cli_Bet_Coin = 7011;
	/** 亮牌，客户端选择确认亮牌 */
	public static final short PROTOCOL_Cli_Open_Cards = 7012;
	/** 玩家抢庄，在庄家模式为自由抢庄和明牌抢庄的时候使用 */
	public static final short PROTOCOL_Cli_Rob_Banker = 7013;
	/** 房主解散房间，由客户端发起 */
	public static final short PROTOCOL_Cli_Owner_Dissolution = 7014;
	/** 玩家进行托管操作 */
	public static final short PROTOCOL_Cli_Auto_Action = 7015;
	/** 玩家拉取上局局号 */
	public static final short PROTOCOL_Cli_Last_Round_Index = 7016;
	/** 固定庄家下庄协议 */
	public static final short PROTOCOL_Cli_FIXED_Banker_Close_Game = 7017;
	
	public static final short PROTOCOL_Ser_ReserveSeat = 7024;//保座离桌推送
	
	public static final short PROTOCOL_Ser_Game_Log_Send = 7023;//每局游戏结束推送战绩给前端
	
	/**
	 * ----------------------------------------end------------------------------
	 * ----------------
	 */
	/**
	 * ---------------------------- 以下协议为服务器发送给客户端的
	 * -------------------------------------
	 */
	/** 进入桌子，坐下 */
	public static final short PROTOCOL_Ser_Table_SitDown = 8001;
	/** 退出桌子，离开 */
	public static final short PROTOCOL_Ser_Table_Exit = 8002;
	/** 申请解散，某个玩家申请解散房间 */
	public static final short PROTOCOL_Ser_Room_Dissolution_Apply = 8003;
	/** 解散选择，其他玩家对申请解散房间做出选择 */
	public static final short PROTOCOL_Ser_Room_Dissolution_Choice = 8004;
	/** 解散结果，对一个解散申请最后的处理结果 */
	public static final short PROTOCOL_Ser_Room_Dissolution_Result = 8005;
	/** 游戏开始，房主开始游戏 */
	public static final short PROTOCOL_Ser_Game_Start = 8006;
	/** 游戏状态，当游戏状态发生变化的时候发送客户端 */
	public static final short PROTOCOL_Ser_Game_Status_Changed = 8007;
	/** 准备,在一局游戏完成后、下一局游戏开始之前可进行的操作 */
	public static final short PROTOCOL_Ser_Ready = 8008;
	/** 玩家抢庄，在庄家模式为自由抢庄和明牌抢庄的时候使用 */
	public static final short PROTOCOL_Ser_Rob_Banker = 8009;
	/** 庄家确认，在庄家确定之后通知客户端庄家信息 */
	public static final short PROTOCOL_Ser_Banker_Info = 8010;
	/** 庄家选择押注底分情况，在创建房间时，底分模式选择了"庄家选择"时触发 */
	public static final short PROTOCOL_Ser_Banker_Choose_BaseCoin = 8011;
	/** 押注，玩家一局的下注分数 */
	public static final short PROTOCOL_Ser_Bet_Coin = 8012;
	/** 发牌，将最多6个位置上的玩家的牌信息分别发送给玩家 */
	public static final short PROTOCOL_Ser_Deal_Cards = 8013;
	/** 亮牌，客户端选择确认亮牌 */
	public static final short PROTOCOL_Ser_Open_Cards = 8014;
	/** 比牌，进行玩家的牌型计算，将比牌结果<也就是游戏结果>发送给客户端,一局的比牌结果 */
	public static final short PROTOCOL_Ser_Compare_Cards_Result = 8015;
	/** 房主解散房间，通知房间内的其他人 */
	public static final short PROTOCOL_Ser_Owner_Dissolution_Notice = 8016;
	/** 房间超时被解散，通知房间内的所有人 */
	public static final short PROTOCOL_Ser_Timeout_Dissolution_Notice = 8017;
	/** 观战玩家收到发牌协议 */
	public static final short PROTOCOL_Ser_Deal_Cards_Watch = 8018;
	/** 明牌抢庄发牌，将最多6个位置上的玩家的牌信息分别发送给玩家 */
	public static final short PROTOCOL_Ser_Rob_Banker_Deal_Cards = 8021;
	/** 观战玩家收到明牌抢庄发牌协议 */
	public static final short PROTOCOL_Ser_ROB_Banker_Deal_Cards_Watch = 8022;
	/** 固定庄家下庄通知客户端 */
	public static final short PROTOCOL_Ser_FIXED_Banker_Close_Game = 8023;
	/** 固定庄家庄家分数不足通下庄通知客户端 */
	public static final short PROTOCOL_Ser_FIXED_Banker_No_Enough_Coin_Close_Game = 8024;
	
	
	
}
