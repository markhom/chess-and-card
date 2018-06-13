package com.linyun.common.taurus.club.eum;

//俱乐部职位
public enum ClubPosition
{
	POSITION_NONE((byte)-1),
	POSITION_CREATOR((byte)0),//俱乐部创建者
	POSITION_ADMIN((byte)1),//俱乐部管理员，预留
	POSITION_MEMBER((byte)2),//俱乐部成员
	POSITION_NOT_MEMBER((byte)3);//非俱乐部成员 -- 申请加入俱乐部的时候会用到 ，其他时候不会用到
	
	public byte value;
	private ClubPosition(byte _value)
	{
		this.value = _value;
	}
	
	public static ClubPosition ValueOf(int n)
	{
		switch (n)
		{
		case 0:
			return POSITION_CREATOR;
		case 1:
			return POSITION_ADMIN;
		case 2:
			return POSITION_MEMBER;
		case 3:
			return POSITION_NOT_MEMBER;
		default:
			return POSITION_NONE;
		}
	}
}
