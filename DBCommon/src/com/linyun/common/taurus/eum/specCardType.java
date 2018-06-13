package com.linyun.common.taurus.eum;

public enum specCardType
{
	CARD_TYPE_NONE((byte)0),//没牛，默认值
	CARD_TYPE_ONE((byte)1),//牛一
	CARD_TYPE_TWO((byte)2),//牛二
	CARD_TYPE_THREE((byte)3),//牛三
	CARD_TYPE_FOUR((byte)4),//牛四
	CARD_TYPE_FIVE((byte)5),//牛五
	CARD_TYPE_SIX((byte)6),//牛六
	CARD_TYPE_SEVEN((byte)7),//牛七
	CARD_TYPE_EIGHT((byte)8),//牛八
	CARD_TYPE_NINE((byte)9),//牛九
	CARD_TYPE_ALL((byte)10),//牛牛
	CARD_TYPE_ALL_FACE((byte)11),//五花
	CARD_TYPE_BOMB((byte)12),//炸弹
	CARD_TYPE_ALL_SMALL((byte)13);//五小牛
		
	public byte value;
	specCardType(byte _value)
	{
		this.value = _value;
	}
	
	public static final specCardType ValueOf(int n)
	{
		switch (n)
		{
		case 0:
			return CARD_TYPE_NONE;
		case 1:
			return CARD_TYPE_ONE;
		case 2:
			return CARD_TYPE_TWO;
		case 3:
			return CARD_TYPE_THREE;
		case 4:
			return CARD_TYPE_FOUR;
		case 5:
			return CARD_TYPE_FIVE;
		case 6:
			return CARD_TYPE_SIX;
		case 7:
			return CARD_TYPE_SEVEN;
		case 8:
			return CARD_TYPE_EIGHT;
		case 9:
			return CARD_TYPE_NINE;
		case 10:
			return CARD_TYPE_ALL;
		case 11:
			return CARD_TYPE_ALL_FACE;
		case 12:
			return CARD_TYPE_BOMB;
		case 13:
			return CARD_TYPE_ALL_SMALL;
		default:
			return CARD_TYPE_NONE;
		}
	}
}
