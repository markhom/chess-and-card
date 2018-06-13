package com.linyun.common.taurus.eum;

/**
 * 支付方式
 * */
public enum RoomPayMode 
{
	PAY_MODE_ONE((byte)0),//房主1人支付，默认值
	PAY_MODE_ALL((byte)1);//AA支付
	
	public byte value;
	private RoomPayMode(byte _value)
	{
		this.value = _value;
	}
	
	/**n的取值为1-2, 不在此范围的将被转化为房主1人支付 */
	public static RoomPayMode ValueOf(int n)
	{
		switch (n)
		{
		case 0:
			return PAY_MODE_ONE;
		case 1:
			return PAY_MODE_ALL;
		default:
			return PAY_MODE_ONE;
		}
	}
}
