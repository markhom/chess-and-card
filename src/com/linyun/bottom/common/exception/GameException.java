package com.linyun.bottom.common.exception;

import com.linyun.bottom.exception.JuiceException;

public class GameException extends JuiceException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2168160349379298791L;

	/**=========================以下为通用的错误码========================**/
	public static final short USER_IS_FROZEN = 100;//用户被冻结
	public static final short USER_NOT_EXIST = 101;//用户不存在
	public static final short ROOM_NOT_EXIST = 102;//房间不存在
	public static final short TABLE_ENTER_ERROR_SET_START_NO_ENTER = 103;//玩家加入桌子时，桌子不能加入，设置了游戏开始禁止加入
	public static final short TABLE_ENTER_ERROR_ROTATE_BANKER_START = 104;//玩家加入桌子时，桌子不能加入，游戏开始了，轮庄模式，禁止加入
	public static final short TABLE_ENTER_ERROR_TABLE_IS_FULL = 105;//玩家加入桌子时，桌子不能加入，牌桌已满
	public static final short TABLE_ENTER_ERROR_DIAMOND_NOT_ENOUGH = 106;//玩家加入桌子时，AA支付，玩家钻石不足不够支付
	public static final short USERGAMELOG_IS_NULL=107;//玩家游戏记录对象为空
	public static final short WAYBILL_IS_NULL=108;//房间的牌局结果记录对象为空
	public static final short PRIVATE_ROOM_IS_NOT_EXIST = 109 ;//加入房间时，房间不存在
	public static final short GET_ROUND_LIST_IS_NULL = 110 ;//拉取战绩一级详情（即是局数的详情列表），拉取的集合为空
	public static final short GET_HAND_CARD_LOG_IS_NULL = 111 ;//拉取战绩二级详情（拉取每个玩家每局的手牌信息），拉取的对象为空
	public static final short GIVEN_DIAMOND_CONFIG_IS_NOT_EXIST = 112 ;//赠送玩家钻石的活动配置的对象不存在
	public static final short INVITECODE_IS_NULL = 113; //绑定邀请码时，邀请码不能为null,必须填写
	public static final short INVITECODE_LIST_LENGTH_IS_ZERO = 114 ;//绑定邀请码时，拉取的邀请码的集合配置信息不存在
	public static final short BIND_INVITECODE_IS_NOT_EXIST = 115 ;//绑定邀请码时，玩家填写的邀请码，在邀请码的配置表的集合中找不到
	public static final short USER_DIAMOND_NOT_ENOUGH = 116 ;//房主支付时，玩家的钻石不足创建房间
	public static final short MARQUEES_IS_NULL = 117 ;//拉取跑马灯时，拉取的跑马灯集合为空
	public static final short INVITECODE_REGULAR_ERROR = 118 ;//绑定邀请码时，玩家输入的邀请码不符合规则，只能输入数字
	public static final short USERGAEMLOG_Not_Exist = 119 ;//通过房间号找到的TaurusLog对象不存在
	public static final short refCode_Is_Myself_InvitationCode = 120 ;//用户填写的邀请码是自身的推广码，自己不能做自己的代理
	public static final short NOTICE_NO_EXITST=121;//系统公告不存在
	public static final short REFCODE_IS_ALREADY_EXIST = 122 ;//玩家已经绑定邀请码，不可重复绑定
	public static final short ERROR_UP_BANKER_MODE = 123 ;//错误的上庄模式	
	public static final short CUSTOMER_SERVICE_IS_NOT_EXIST = 124 ;//拉取的客户服务不存在
	public static final short USER_HAVE_ROOMS_OUT_OF_20 = 125 ;//玩家最多拥有20个房间，达到20个房间时，不允许玩家创建房间或者进入房间
	public static final short GIFT_CODE_ERROR_EMPTY = 130;//玩家填写的礼品码为空
	public static final short GIFT_CODE_ERROR_HAVE_BIND = 131;//当前玩家已经领取过礼品码对应的礼品了
	public static final short GIFT_CODE_ERROR_NOT_FOUND = 132;//玩家填写的礼品码在礼品码列表中找不到
	public static final short SESSIONID_IS_DISABLE = 133 ;//通过sessionId登录时，sessionId已经过期
	public static final short PHONENEM_IS_NOT_MATCH = 134 ;//手机号格式填写有误
	public static final short NICK_NAME_IS_NOT_MATCH = 135;//设置昵称时不符合规则
	/** -------------------------end------------------------------*/
	
	/**=========================以下为游戏相关的错误码========================**/
	public static final short ROOM_DISSOLUTION_ERROR_GAME_START = 150;//房主解散房间失败，牌局已经开始
	public static final short ROOM_DISSOLUTION_ERROR_ISNOT_OWNER = 151;//房主解散房间失败，房主不是当前房间的房主
	public static final short START_GAME_ERROR_ROOM_NOT_EXIST = 153;//房主开始游戏失败， 找不到房间
	public static final short START_GAME_ERROR_TABLE_NOT_EXIST = 154;//房主开始游戏失败， 找不到桌子
	public static final short START_GAME_ERROR_IS_NOT_OWNER = 155;//房主开始游戏失败，房主不是当前房间的房主
	public static final short START_GAME_ERROR_PLAYER_COUNT_ERROR = 156;//房主开始游戏失败，桌子上的玩家数量少于2人
	public static final short GAME_ERROR_IN_DISSOLUTION_STAGE = 157;//玩家申请解散房间失败，房间正在解散阶段 
	public static final short GAME_ERROR_NOT_IN_DISSOLUTION_STAGE = 158;//玩家选择解散房间失败，房间不处于解散阶段
	public static final short GAME_ERROR_IN_DISSOLUTION_STAGE_OTHER = 159;//房间处于解散阶段,禁止操作 
	public static final short GAME_ERROR_USER_NOT_EXIST = 160;//在桌子内找不到玩家
	public static final short GAME_ERROR_USER_IS_NOT_BANKER = 161;//庄家选择底分时，不是当前桌的庄家
	public static final short GAME_ERROR_ROOM_BASESCORE_IS_NOT_BANKER = 162;//庄家选择底分时，当前房间的底分选项不是庄家选择
	public static final short GAME_ERROR_CHOOSE_BASE_COIN = 163;// 庄家选择游戏底分，错误的游戏押注底分选项
	public static final short GAME_ERROR_DISSOLUTION_GAME_IS_NOT_START = 164;//游戏未开始，不能申请解散房间 
	public static final short GAME_ERROR_BET_ERROR_BET_COIN = 165;//玩家押注时，错误的押注筹码
	public static final short GAME_ERROR_Game_Statred_Not_Exit = 166;//游戏已经开始，不能退出房间
	public static final short SEAT_NOT_EXIST = 167;//座位不存在
	public static final short SEAT_INDEX_ERROR = 168;//错误的位置索引
	public static final short ROOM_REPEAT_JOIN = 169;// 重复加入房间
	public static final short TABLE_REPEAT_JOIN = 170;//重复加入桌子
	public static final short ROOM_ENTER_UNFINISHED_GAME = 171;//加入房间/桌子时，有未完成的牌局
	public static final short GAME_ERROR_ROOM_OWNER_START_GAME_STARTED = 172;//房主开始游戏时，游戏已经开始
	public static final short GAME_ERROR_REPEAT_READY = 173;//重复准备
	public static final short GAME_ERROR_REPEAT_ROB_BANKER = 174;//重复抢庄
	public static final short GAME_ERROR_REPEAT_CHOOSE_BASESCORE = 175;//庄家重复选择底分
	public static final short GAME_ERROR_REPEAT_BET = 176;//重复押注
	public static final short GAME_ERROR_REPEAT_OPENCARDS = 177;//重复开牌
	public static final short GAME_ERROR_BANKER_MODE_NOT_FIXED = 181;//固定庄家下庄时，房间模式不是固定庄家模式
	public static final short GAME_ERROR_BANKER_ROND_NOT_ENOUGH = 182;//固定庄家下庄时，牌局未满三局	
	public static final short GAME_ERROR_CLUB_DISSOLUTION_GAME_IS_START = 183;//俱乐部房间游戏开始后禁止解散游戏	
	public static final short GAME_ERROR_SEAT_CAN_NOT_SITDOWN = 184;
	public static final short GAME_DOWNBANKER_ERROR_GAME_START = 185;//固定庄家下庄时，牌局已开始
	
	/**=========================以下为俱乐部相关的错误码========================**/
	public static final short TABLE_NOT_EXIST = 201;//找不到桌子
	public static final short ENGINE_NOT_EXIST = 228;//找不到游戏引擎
	public static final short ENTER_ROOM_COIN_LACK = 229;//入场金币低于限制
	public static final short NO_LASTROUND = 230; //没有上局回顾
	public static final short BET_COIN_ERROR = 231;//下注时传入的金额有误
	public static final short BET_COIN_LACK = 232;//下注金币不足
	public static final short ROOM_UNFINISHED_GAME = 202;//有未完成的牌局 
	public static final short NICK_NAME_IS_NOT_STANDARD = 203 ; //俱乐部昵称长度不合乎标准
	public static final short CLUB_INTRODUCE_TOO_LONG = 204; //俱乐部介绍字数过多
	public static final short USER_ALREADY_CREATE_CLUB = 205 ;//玩家已经创建了俱乐部
	public static final short CLUB_IS_NOT_EXIST = 206; //玩家申请加入,离开,删除该俱乐部时，该俱乐部不存在
	public static final short CLUB_USER_IS_NOT_CLUB_CREATOR = 207;//玩家不是俱乐部群主
	public static final short CLUB_USER_ALREADY_JOIN_CLUB = 208;//玩家不是俱乐部成员
	public static final short CLUB_HAS_OTHER_MEMBERS = 209 ;//俱乐部还有其他的成员，无法删除
	public static final short CLUB_USER_IS_NOT_MEMBER = 210;//玩家不属于该俱乐部
	public static final short CLUB_CREATOR_CAN_NOT_EXIT = 211; //俱乐部群主无法退出自己创建的俱乐部
	public static final short CLUB_COUNTS_REACH_LIMIT = 212 ;//玩家的俱乐部达到上限
	public static final short PUBLIC_CLUB_DO_NOT_NEED_DETAIL = 213 ;//公共俱乐部不需要详情
	public static final short CLUB_PEOPLE_REACH_LIMIT = 214 ;//俱乐部人数达到上限
	public static final short CLUB_CREATOR_DIAMOND_IS_NOT_ENOUGH = 215 ;//俱乐部群主钻石不足
	public static final short USER_REMAINING_AMOUNT_NOT_ENOUGH = 216 ;//玩家当日的可用额度不足
	public static final short USER_ALREADY_APPLY_JOIN_CLUB = 217 ;//玩家已经申请加入了该俱乐部
	public static final short CLUB_ROOMS_REACH_LIMIT = 218 ;//俱乐部房间数达到允许的最大值
	public static final short CLUB_MESSAGE_NOT_EXIST = 219 ; //读取消息时，消息不存在
	public static final short PUB_CLUB_DOESNOT_NEED_APPLY_JOIN = 220;//公共俱乐部不需要申请加入，点击直接进入
	public static final short CLUB_CREATOR_SET_MEMBER_DIAMOND_LIMIT = 221;//俱乐部群主设置成员额度时，设置钻石超过群主钻石
	public static final short MARQUEE_ABLE_REMAINING_ONE = 222 ;//启动状态的跑马灯最少保留一条
	public static final short CLUB_MEMBER_ALREADY_CREATE_ROOM = 223;//群主踢出成员时，成员已在俱乐部开房间，不可踢出
	public static final short USER_SIT_CURRENT_CLUB_TABLE = 224;//群主踢出成员时，成员已在该俱乐部的房间坐下，不可踢出
	public static final short CLUB_CREATEROOM_BASESCORE_ERROR = 225;//俱乐部创建房间时传入底分有误
	public static final short CLUB_BUYSCORE_BELOW_LIMIT = 226; //俱乐部成员买入积分低于房间最低底分
	public static final short CLUB_BUYSCORE_ABOVE_CURRENTSCORE = 227;//俱乐部成员买入积分高于其现有总积分
	public static final short CLUB_SCOREPOOL_LACK = 250; //俱乐部积分池不够
	public static final short CLUB_RECYCLING_SCORE_LACK = 251;//群主回收俱乐部成员积分时成员积分不足
	public static final short CLUB_COINPOOL_LACK = 252; //俱乐部的金币池不足
	public static final short CLUB_RECYCLING_COIN_LACK = 253;//群主回收群成员的金币时，玩家的金币数不足
	
	public static final short LOGIN_ERROR = 301;//用户登录失败
	public static final short LOGIN_ERROR_CHECK_NOT_LOGIN = 302;//用户未登录
	public static final short VERIFYCODE_FOMAT_ERROR = 310;//短信验证码不符合规则
	public static final short VERIFYCODE_CHECK_ERROR = 311;//短信验证码不正确
	
	public static final short USER_LOGIN_BY_OTHERWAY = 911 ;//该玩家在其他设备上登录
	public static final short GAME_ERROR_WATCH_PALYER_RECONNECT = 912;//观战玩家重连失败，观战玩家房间内断网，过了三个心跳周期(15s)后重连时，此时服务器已经清理了该玩家信息，需要抛出此错误码
	
	public static final short ERROR_CODE_SER_INSIDE_ERROR = 500;
	
	
	private short id;
	public GameException(String message)
	{
		super(message);
	}

	public GameException(short id, String message)
	{
		super(message);
		this.id = id;
	}

	public GameException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public GameException(Throwable cause)
	{
		super(cause);
	}

	public short getId()
	{
		return id;
	}
}
