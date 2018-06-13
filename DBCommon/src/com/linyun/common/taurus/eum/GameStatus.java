package com.linyun.common.taurus.eum;

public enum GameStatus
{
	GAME_STATUS_INIT((byte)0),//初始状态，游戏未开始
	GAME_STATUS_TABLE_READY((byte)1),//准备阶段，房主已经点击了开始游戏，游戏中
	GAME_STATUS_TABLE_ROB_BANKER((byte)2),//抢庄阶段
	GAME_STATUS_TABLE_BANKER_CHOOSE_BASECOIN((byte)3),//庄家选择底分阶段
	GAME_STATUS_TABLE_BET((byte)4),//押注阶段
	GAME_STATUS_TABLE_OPEN_CARDS((byte)5),//亮牌阶段
	GAME_STATUS_TABLE_BANKER_OPEN_CARDS((byte)6),//庄家亮牌动画阶段
	GAME_STATUS_END((byte)9),//游戏结束，打完所有局数
	GAME_STATUS_PAUSE((byte)10);
	
	public byte value;
	private GameStatus(byte _value)
	{
		this.value = _value;
	}
	
	/**n的取值为1-4, 不在此范围的将被转化为为默认值 */
	public static GameStatus ValueOf(int n)
	{
		switch (n)
		{
		case 1:
			return GAME_STATUS_TABLE_READY;
		case 2:
			return GAME_STATUS_TABLE_ROB_BANKER;
		case 3:
			return GAME_STATUS_TABLE_BANKER_CHOOSE_BASECOIN;
		case 4:
			return GAME_STATUS_TABLE_BET;
		case 5:
			return GAME_STATUS_TABLE_OPEN_CARDS;
		case 6:
			return GAME_STATUS_TABLE_BANKER_OPEN_CARDS;
		case 9:
			return GAME_STATUS_END;
		case 10:
			return GAME_STATUS_PAUSE;
		default:
			return GAME_STATUS_INIT;
		}
	}
}
