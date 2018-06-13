package com.linyun.common.taurus.club.eum;

public enum ClubOperateType
{
	TYPE_NONE((byte)-1),
	TYPE_APPLY_JOIN((byte)0),//申请加入俱乐部
	TYPE_EXIT((byte)1),//退出俱乐部
	TYPE_CREATE((byte)10),//创建俱乐部
	TYPE_DELETE((byte)11),//删除俱乐部
	TYPE_HANDLE_AGREE_JOIN((byte)12), //审批加入申请通过   
	TYPE_HANDLE_REFUSE_JOIN((byte)13),//审批拒绝
	TYPE_INVITE_NEW_MEMBER((byte)14),//邀请新成员
	TYPE_KICK_MEMBER((byte)15);//踢出成员 
	
	public byte value;
	private ClubOperateType(byte _value)
	{
		this.value = _value;
	}
	
	public static ClubOperateType ValueOf(int n)
	{
		switch (n)
		{
		case 0:
			return TYPE_APPLY_JOIN;
		case 1:
			return TYPE_EXIT;
		case 10:
			return TYPE_CREATE;
		case 11:
			return TYPE_DELETE;
		case 12:
			return TYPE_HANDLE_AGREE_JOIN;
		case 13:
			return TYPE_HANDLE_REFUSE_JOIN;
		case 14:
			return TYPE_INVITE_NEW_MEMBER;
		case 15:
			return TYPE_KICK_MEMBER;
		default:
			return TYPE_NONE;
		}
	}
}
