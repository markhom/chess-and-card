package com.linyun.common.taurus.eum;

/**
 * 上庄分数
 * */
public enum UpBankerScore
{
	UP_BANKER_SCORE_NONE((short)0),//无， 默认值
	UP_BANKER_SCORE_200((short)200),// 200
	UP_BANKER_SCORE_400((short)400),// 100
	UP_BANKER_SCORE_800((short)800);// 150
	public short value;
	private UpBankerScore(short _value)
	{
		this.value = _value;
	}
	
	/**n的取值为0、50、100、150、200, 不在此范围的将被转化为为默认值 */
	public static UpBankerScore ValueOf(int n)
	{
		switch (n)
		{
		case 0:
			return UP_BANKER_SCORE_NONE;
		case 1:
		case 200:
			return UP_BANKER_SCORE_200;
		case 2:
		case 400:
			return UP_BANKER_SCORE_400;
		case 3:
		case 800:
			return UP_BANKER_SCORE_800;
		default:
			return UP_BANKER_SCORE_NONE;
		}
	}
}
