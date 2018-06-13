package com.linyun.common.taurus.eum;

public enum DissolutionStatus
{
	DISSOLUTION_UNSELECTED((byte)0),//未选择
	DISSOLUTION_AGREE((byte)1),//同意解散
	DISSOLUTION_DISAGREE((byte)2);//不同意解散
	
	public byte value;
	private DissolutionStatus(byte _value)
	{
		this.value = _value;
	}
	
	public static DissolutionStatus ValueOf(int n)
	{
		switch (n)
		{
		case 0:
			return DISSOLUTION_UNSELECTED;
		case 1:
			return DISSOLUTION_AGREE;
		case 2:
			return DISSOLUTION_DISAGREE;
		default:
			return DISSOLUTION_UNSELECTED;
		}
	}
}
