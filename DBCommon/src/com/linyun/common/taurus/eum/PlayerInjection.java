package com.linyun.common.taurus.eum;

/**
 * 闲家推注
 * */
public enum PlayerInjection
{
	PLAYER_INJECTION_NONE((byte)0), //闲家不推注， 默认值
	PLAYER_INJECTION_5((byte)5),// 闲家推注 5倍
	PLAYER_INJECTION_10((byte)10),// 闲家推注 10倍
	PLAYER_INJECTION_20((byte)20);// 闲家推注 20倍
	
	public byte value;
	private PlayerInjection(byte _value)
	{
		this.value = _value;
	}
	
	/**n的取值为0、5、10、20, 不在此范围的将被转化为为默认值 */
	public static PlayerInjection ValueOf(int n)
	{
		switch (n)
		{
		case 0:
			return PLAYER_INJECTION_NONE;
		case 5:
			return PLAYER_INJECTION_5;
		case 10:
			return PLAYER_INJECTION_10;
		case 20:
			return PLAYER_INJECTION_20;
		default:
			return PLAYER_INJECTION_NONE;
		}
	}
	
}
