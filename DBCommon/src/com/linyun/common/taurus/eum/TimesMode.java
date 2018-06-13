package com.linyun.common.taurus.eum;

/**
 *  翻倍模式
 * */
public enum TimesMode 
{
	TIMES_MODE_TAURUS_3((byte)1),// 牛牛X3 牛九X2 牛八X2 
	TIMES_MODE_TAURUS_4((byte)2);// 牛牛X4 牛九X3 牛八X2 牛七X2    默认值
	
	public byte value;
	private TimesMode(byte _value)
	{
		this.value = _value;
	}
	
	/**n的取值为10、20, 不在此范围的将被转化为10 */
	public static TimesMode ValueOf(int n)
	{
		switch (n)
		{
		case 1:
			return TIMES_MODE_TAURUS_3;
		case 2:
			return TIMES_MODE_TAURUS_4;
		default:
			return TIMES_MODE_TAURUS_4;
		}
	}
}
