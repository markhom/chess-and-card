package com.linyun.common.taurus.eum;

/**
 * 最大抢庄
 * */

public enum MostRobBanker
{
	MOST_ROB_BANKER_1((byte)1),//1倍
	MOST_ROB_BANKER_2((byte)2),//2倍
	MOST_ROB_BANKER_3((byte)3),//3倍
	MOST_ROB_BANKER_4((byte)4); //4倍，默认值
	public byte value;
	private MostRobBanker(byte _value)
	{
		this.value = _value;
	}
	
	/**n的取值为1-3, 不在此范围的将被转化为为默认值 */
	public static MostRobBanker ValueOf(int n)
	{
		switch (n)
		{
		case 1:
			return MOST_ROB_BANKER_1;
		case 2:
			return MOST_ROB_BANKER_2;
		case 3:
			return MOST_ROB_BANKER_3;
		case 4:
			return MOST_ROB_BANKER_4;
		default:
			return MOST_ROB_BANKER_4;
		}
	}
}
