package com.linyun.common.taurus.eum;

public enum AllCompareBaseScoreType
{
	MODE_1((byte)1),//押注底分为 1  默认值
	MODE_2((byte)2),//押注底分为 2
	MODE_4((byte)3);//押注底分为 4
	
	public byte value;
	private AllCompareBaseScoreType(byte _value)
	{
		this.value = _value;
	}
	
	/**n的取值为1-4, 不在此范围的将被转化为为默认值 */
	public static AllCompareBaseScoreType ValueOf(int n)
	{
		switch (n)
		{
		case 1:
			return MODE_1;
		case 2:
			return MODE_2;
		case 3:
			return MODE_4;
		default:
			return MODE_1;
		}
	}
}
