package com.linyun.common.taurus.eum;

public enum BankerMode 
{
	BANKER_MODE_TAURUS((byte)1), //牛牛上庄 ,  默认值
	BANKER_MODE_FIXED((byte)2), //固定庄家 
	BANKER_MODE_FREE((byte)3), //自由抢庄
	BANKER_MODE_BRIGHT_ROB((byte)4), //明牌抢庄
	BANKER_MODE_ALL_COMPARE((byte)5), //通比牛牛
	BANKER_MODE_ROTATE((byte)6); //轮庄  此模式下游戏开始后，其他玩家不能进入此房间，房间内进行游戏的玩家，不得随意退出房间
	
	public byte value;
	BankerMode(byte _value)
	{
		this.value = _value;
	}
	
	public static final BankerMode ValueOf(int n)
	{
		switch (n)
		{
		case 1:
			return BANKER_MODE_TAURUS;
		case 2:
			return BANKER_MODE_FIXED;
		case 3:
			return BANKER_MODE_FREE;
		case 4:
			return BANKER_MODE_BRIGHT_ROB;
		case 5:
			return BANKER_MODE_ALL_COMPARE;
		case 6:
			return BANKER_MODE_ROTATE;
		default:
			return BANKER_MODE_TAURUS;
		}
	}

}
