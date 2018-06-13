package com.linyun.common.taurus.eum;

public enum BaseScoreType
{
	MODE_1_2((byte)1),//押注底分为 1/2  默认值
	MODE_2_4((byte)2),//押注底分为 2/4
	MODE_4_8((byte)3),//押注底分为 4/8
	MODE_BANKER_CHOICE((byte)4), //庄家选择
	MODE_SCROLL_SELECTED((byte)5);//滚动条自由选择
	
	public byte value;
	private BaseScoreType(byte _value)
	{
		this.value = _value;
	}
	
	/**n的取值为1-4, 不在此范围的将被转化为为默认值 */
	public static BaseScoreType ValueOf(int n)
	{
		switch (n)
		{
		case 1:
			return MODE_1_2;
		case 2:
			return MODE_2_4;
		case 3:
			return MODE_4_8;
		case 4:
			return MODE_BANKER_CHOICE;
		case 5:
			return MODE_SCROLL_SELECTED;
		default:
			return MODE_1_2;
		}
	}
}
