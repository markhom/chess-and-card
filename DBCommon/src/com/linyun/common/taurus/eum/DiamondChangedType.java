package com.linyun.common.taurus.eum;

public enum DiamondChangedType
{
	TYPE_RECHARGE((byte)0),//充值
	TYPE_DEDUCT((byte)1),//扣除房费
	TYPE_ACTIVITY((byte)2),//活动送(绑定邀请码)
	TYPE_RETURN((byte)3),//归还玩家扣除房费 钻石增加
	TYPE_CLUB_DEDUCT((byte)4),//俱乐部钻石扣除
	TYPE_CLUB_RETURN((byte)5),//俱乐部钻石归还
	TYPE_CLUB_BUYSCORE_DEDUCT((byte)6),
	TYPE_MANUAL_RECHARGE((byte)10),//后台手工充值
	TYPE_MANUAL_DEDUCT((byte)11);//后台手工扣除
	
	public byte value;
	private DiamondChangedType(byte _value)
	{
		this.value = _value;
	}
	
	/**n的取值为1-3, 不在此范围的将被转化为为默认值 */
	public static DiamondChangedType ValueOf(int n)
	{
		switch (n)
		{
			case 0:
				return TYPE_RECHARGE;
			case 1:
				return TYPE_DEDUCT;
			case 2:
				return TYPE_ACTIVITY;
			case 3:
				return TYPE_RETURN;
			case 4:
				return TYPE_CLUB_DEDUCT;
			case 5:
				return TYPE_CLUB_RETURN;
			case 6:
				return TYPE_CLUB_BUYSCORE_DEDUCT;
			case 10:
				return TYPE_MANUAL_RECHARGE;
			case 11:
				return TYPE_MANUAL_DEDUCT;
		}
		return null;
	}
}
