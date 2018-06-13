package com.linyun.common.taurus.eum;

public enum BankerChooseBaseScore
{
	BANKER_BASE_SCORE_0((byte)0),//没有选择押注底分
	BANKER_BASE_SCORE_2((byte)2),//押注底分为 2 
	BANKER_BASE_SCORE_4((byte)4),//押注底分为 2/4
	BANKER_BASE_SCORE_8((byte)8),//押注底分为 2/4/8
	BANKER_BASE_SCORE_16((byte)16), //押注底分为2/4/8/16
	BANKER_BASE_SCORE_32((byte)32); //押注底分为 2/4/8/16/32 

	public byte value;
	private BankerChooseBaseScore(byte _value)
	{
		this.value = _value;
	}
	
	/**n的取值为2\4\8\16\32, 不在此范围的将被转化为为默认值 */
	public static BankerChooseBaseScore ValueOf(int n)
	{
		switch (n)
		{
		case 2:
			return BANKER_BASE_SCORE_2;
		case 4:
			return BANKER_BASE_SCORE_4;
		case 8:
			return BANKER_BASE_SCORE_8;
		case 16:
			return BANKER_BASE_SCORE_16;
		case 32:
			return BANKER_BASE_SCORE_32;	
		default:
			return BANKER_BASE_SCORE_0;
		}
	}
}
