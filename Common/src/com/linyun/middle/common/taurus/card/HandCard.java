package com.linyun.middle.common.taurus.card;

import com.linyun.common.taurus.eum.specCardType;
import com.linyun.middle.common.taurus.bean.RoomConfigSpecConfig;

public class HandCard
{
	/**牛牛的一副手牌的数量 5张*/
	public static final int CARDS_NUM = 5;
	//牌的类型  是否有牛 没牛的话为 0 有牛值为从1-11
	private specCardType specType;
	private boolean isSpecType;
	
	private short[] cards = new short[CARDS_NUM];
	/**
	* 下面两个用于保存手牌的两个组合   ，主要用于前端客户端展示牌型和游戏记录
	* 1.无牛时，都在第二个数组存放 
	* 2.有牛时， 前面一个保存前三张组成牛的牌， 后面一个保存用于的计算牛的点数的两张牌型
	* 3.炸弹牛时，前面一个保存构成炸弹的牌，后面一个用于保存一张散牌
	* 4.五小牛时，前面一个保存所有的牌 
	*/
	private short[] cardsOne = new short[CARDS_NUM];
	private short[] cardsTwo = new short[CARDS_NUM];
	
	public HandCard()
	{
		this.specType = specCardType.CARD_TYPE_NONE;
		this.isSpecType = false;
	}
	
	public void Init()
	{
		this.specType = specCardType.CARD_TYPE_NONE;
		this.isSpecType = false;
	}
	
	public void clear()
	{   
		for (int i = 0; i < cards.length; i++)
		{
			this.cards[i] = 0 ;
			this.cardsOne[i] = 0;
			this.cardsTwo[i] = 0;
		}
		
	}
	
	/**
	 * @param int[] _cards  长度需要等于五  表示五张扑克牌
	 * */
	public void setCards(final short[] _cards)
	{
		for (int i=0; i<CARDS_NUM; ++i)
		{
			this.cards[i] = _cards[i];
		}
	}
	
	public final void calcCardType(RoomConfigSpecConfig specConfig)
	{
		for (int i=0; i<CARDS_NUM; ++i)
		{
			this.cardsOne[i] = 0;
			this.cardsTwo[i] = 0;
		}
		
		//先进行降序排序
		OderByDesc();
		/**
		 * 判断顺序：由于五小牛的倍数8倍 高于 炸弹牛6倍 高于 五花牛 5倍  故按照 1、五小牛 2、炸弹牛 3、五花牛的顺序判断
		 * */
		if (specConfig.isAllSmall() && IsAllSmall())
		{//判断是不是五小牛
			this.specType = specCardType.CARD_TYPE_ALL_SMALL;
			this.isSpecType = true;
			return;
		}
		if (specConfig.isBomb() && IsBomb())
		{//判断是不是炸弹牛
			this.specType = specCardType.CARD_TYPE_BOMB;
			this.isSpecType = true;
			return;
		}
		if (specConfig.isAllFace() && IsAllFace())
		{//判断是不是五花牛
			this.specType = specCardType.CARD_TYPE_ALL_FACE;
			this.isSpecType = true;
			return;
		}
		
		//不是上面的三种特殊牌型，下面进行牛牛和有无牛的判断
		int[] _cards = new int[CARDS_NUM];
		for (int i=0; i<_cards.length; ++i)
		{
			_cards[i] = cards[i];
		}
		
		//将花牌转化为10
		for (int i=0; i<_cards.length; ++i)
		{
			if (Cards.getCardValue(_cards[i]) > 10)
			{
				_cards[i] = (_cards[i]&0x0f)|0xA0;
			}
		}
		
		//计算牛的情况
		ClacSpecType(_cards);
		
		//cardsOne 和 cardsTwo中的不为0的牌的数量和为5，代表五张牌
		int iCount = 0;
		for (int i=0; i<CARDS_NUM; ++i)
		{
			if (cardsOne[i] > 0)
			{
				cards[iCount++] = cardsOne[i];
			}
		}
		for (int i=0; i<CARDS_NUM; ++i)
		{
			if (cardsTwo[i] > 0)
			{
				cards[iCount++] = cardsTwo[i];
			}
		}
	}
	
	public final void calcCardType()
	{
		for (int i=0; i<CARDS_NUM; ++i)
		{
			this.cardsOne[i] = 0;
			this.cardsTwo[i] = 0;
		}
		
		//先进行降序排序
		OderByDesc();
		/**
		 * 判断顺序：由于五小牛的倍数8倍 高于 炸弹牛6倍 高于 五花牛 5倍  故按照 1、五小牛 2、炸弹牛 3、五花牛的顺序判断
		 * */
		if (IsAllSmall())
		{//判断是不是五小牛
			this.specType = specCardType.CARD_TYPE_ALL_SMALL;
			this.isSpecType = true;
			return;
		}
		if (IsBomb())
		{//判断是不是炸弹牛
			this.specType = specCardType.CARD_TYPE_BOMB;
			this.isSpecType = true;
			return;
		}
		if (IsAllFace())
		{//判断是不是五花牛
			this.specType = specCardType.CARD_TYPE_ALL_FACE;
			this.isSpecType = true;
			return;
		}
		
		//不是上面的三种特殊牌型，下面进行牛牛和有无牛的判断
		int[] _cards = new int[CARDS_NUM];
		for (int i=0; i<_cards.length; ++i)
		{
			_cards[i] = cards[i];
		}
		
		//将花牌转化为10
		for (int i=0; i<_cards.length; ++i)
		{
			if (Cards.getCardValue(_cards[i]) > 10)
			{
				_cards[i] = (_cards[i]&0x0f)|0xA0;
			}
		}
		
		//计算牛的情况
		ClacSpecType(_cards);
		
		//cardsOne 和 cardsTwo中的不为0的牌的数量和为5，代表五张牌
		int iCount = 0;
		for (int i=0; i<CARDS_NUM; ++i)
		{
			if (cardsOne[i] > 0)
			{
				cards[iCount++] = cardsOne[i];
			}
		}
		for (int i=0; i<CARDS_NUM; ++i)
		{
			if (cardsTwo[i] > 0)
			{
				cards[iCount++] = cardsTwo[i];
			}
		}
	}
	
	public final void calcHundredsCardType()
	{
		for (int i=0; i<CARDS_NUM; ++i)
		{
			this.cardsOne[i] = 0;
			this.cardsTwo[i] = 0;
		}
		
		//先进行降序排序
		OderByDesc();
		/**
		 * 判断顺序：百人牛牛没有特殊牌型
		 * */
		
		//不是上面的三种特殊牌型，下面进行牛牛和有无牛的判断
		int[] _cards = new int[CARDS_NUM];
		for (int i=0; i<_cards.length; ++i)
		{
			_cards[i] = cards[i];
		}
		
		//将花牌转化为10
		for (int i=0; i<_cards.length; ++i)
		{
			if (Cards.getCardValue(_cards[i]) > 10)
			{
				_cards[i] = (_cards[i]&0x0f)|0xA0;
			}
		}
		
		//计算牛的情况
		ClacSpecType(_cards);
		
		//cardsOne 和 cardsTwo中的不为0的牌的数量和为5，代表五张牌
		int iCount = 0;
		for (int i=0; i<CARDS_NUM; ++i)
		{
			if (cardsOne[i] > 0)
			{
				cards[iCount++] = cardsOne[i];
			}
		}
		for (int i=0; i<CARDS_NUM; ++i)
		{
			if (cardsTwo[i] > 0)
			{
				cards[iCount++] = cardsTwo[i];
			}
		}
	}
	
	private void OderByDesc()
	{
		short temp = 0;
		for (int i=0; i<cards.length-1; ++i)
		{
			for (int j=0; j<cards.length-1-i; ++j)
			{
				if (cards[j] < cards[j+1])
				{
					temp = cards[j];
					cards[j] = cards[j+1];
					cards[j+1] = temp;
				}
			}
		}
	}
	
	//是否五花牛
	private boolean IsAllFace()
	{
		for (int i=0; i<cards.length; ++i)
		{
			int value = getValue(cards[i]);
			if (value <= 10)
			{
				return false;
			}
		}
		
		for (int i=0; i<CARDS_NUM; ++i)
		{
			cardsOne[i] = cards[i];
		}
		
		return true;
	}
	//是否炸弹牛
	private boolean IsBomb()
	{
		int value1 = getValue(cards[0]);
		int value2 = getValue(cards[1]);
		int value3 = getValue(cards[2]);
		int value4 = getValue(cards[3]);
		int value5 = getValue(cards[4]);
		
		//由于已经排序过了，所以这里只进行判断即可
		if (((value1==value2) && (value1==value3) && (value1 == value4)) || ((value2==value3) && (value2==value4) && (value2==value5)))
		{
			if (value2==value3)
			{
				for (int i=0; i<CARDS_NUM-1; ++i)
				{
					cardsOne[i] = cards[i];
				}
				cardsTwo[0] = cards[CARDS_NUM-1];
			}
			else
			{
				for (int i=1; i<CARDS_NUM; ++i)
				{
					cardsOne[i-1] = cards[i];
				}
				cardsTwo[0] = cards[0];
			}

			return true;
		}
		return false;
	}
	//是否五小牛
	private boolean IsAllSmall()
	{
		int value1 = getValue(cards[0]);
		int value2 = getValue(cards[1]);
		int value3 = getValue(cards[2]);
		int value4 = getValue(cards[3]);
		int value5 = getValue(cards[4]);
		
		if ((value1+value2+value3+value4+value5) >= 10)
		{//五张牌的和不小于10点则不为五小牛
			return false;
		}
		if ((value1>=5) || (value2>=5) || (value3>=5) || (value4>=5) || (value5>=5))
		{//每张牌不小于5则不为五小牛
			return false;
		}
		
		for (int i=0; i<CARDS_NUM; ++i)
		{
			cardsOne[i] = cards[i];
		}
		return true;
	}
	
	//计算是否有牛
	private boolean ClacSpecType(final int[] _cards)
	{
		int[] b = new int[10];
		int sum = getValue(_cards[0]) + getValue(_cards[1]) + getValue(_cards[2]) + getValue(_cards[3]) + getValue(_cards[4]);
		
		b[0] = getValue(_cards[0]) + getValue(_cards[1]) + getValue(_cards[2]);
		b[1] = getValue(_cards[0]) + getValue(_cards[1]) + getValue(_cards[3]);
		b[2] = getValue(_cards[0]) + getValue(_cards[1]) + getValue(_cards[4]);
		b[3] = getValue(_cards[0]) + getValue(_cards[2]) + getValue(_cards[3]);
		b[4] = getValue(_cards[0]) + getValue(_cards[2]) + getValue(_cards[4]);
		b[5] = getValue(_cards[0]) + getValue(_cards[3]) + getValue(_cards[4]);
		b[6] = getValue(_cards[1]) + getValue(_cards[2]) + getValue(_cards[3]);
		b[7] = getValue(_cards[1]) + getValue(_cards[2]) + getValue(_cards[4]);
		b[8] = getValue(_cards[1]) + getValue(_cards[3]) + getValue(_cards[4]);
		b[9] = getValue(_cards[2]) + getValue(_cards[3]) + getValue(_cards[4]);
		
		int points = 0;
		for (int i=0; i<b.length; ++i) 
		{
			if (!IsDivisibleToTen(b[i]))
			{
				continue;
			}
			switch (i)
			{
			case 0: 
			{
				cardsOne[0] = cards[0];
				cardsOne[1] = cards[1];
				cardsOne[2] = cards[2];
				cardsTwo[0] = cards[3];
				cardsTwo[1] = cards[4];
				break;
			}
			case 1: 
			{
				cardsOne[0] = cards[0];
				cardsOne[1] = cards[1];
				cardsOne[2] = cards[3];
				cardsTwo[0] = cards[2];
				cardsTwo[1] = cards[4];
				break;
			}
			case 2: 
			{
				cardsOne[0] = cards[0];
				cardsOne[1] = cards[1];
				cardsOne[2] = cards[4];
				cardsTwo[0] = cards[2];
				cardsTwo[1] = cards[3];
				break;
			}
			case 3: 
			{
				cardsOne[0] = cards[0];
				cardsOne[1] = cards[2];
				cardsOne[2] = cards[3];
				cardsTwo[0] = cards[1];
				cardsTwo[1] = cards[4];
				break;
			}
			case 4: 
			{
				cardsOne[0] = cards[0];
				cardsOne[1] = cards[2];
				cardsOne[2] = cards[4];
				cardsTwo[0] = cards[1];
				cardsTwo[1] = cards[3];
				break;
			}
			case 5: 
			{
				cardsOne[0] = cards[0];
				cardsOne[1] = cards[3];
				cardsOne[2] = cards[4];
				cardsTwo[0] = cards[1];
				cardsTwo[1] = cards[2];
				break;
			}
			case 6: 
			{
				cardsOne[0] = cards[1];
				cardsOne[1] = cards[2];
				cardsOne[2] = cards[3];
				cardsTwo[0] = cards[0];
				cardsTwo[1] = cards[4];
				break;
			}
			case 7: 
			{
				cardsOne[0] = cards[1];
				cardsOne[1] = cards[2];
				cardsOne[2] = cards[4];
				cardsTwo[0] = cards[0];
				cardsTwo[1] = cards[3];
				break;
			}
			case 8: 
			{
				cardsOne[0] = cards[1];
				cardsOne[1] = cards[3];
				cardsOne[2] = cards[4];
				cardsTwo[0] = cards[0];
				cardsTwo[1] = cards[2];
				break;
			}
			case 9: 
			{
				cardsOne[0] = cards[2];
				cardsOne[1] = cards[3];
				cardsOne[2] = cards[4];
				cardsTwo[0] = cards[0];
				cardsTwo[1] = cards[1];
				break;
			}
			default:
				break;
			}
			
			points = (sum - b[i])%10;
			//牛牛
			if (points == 0)
			{
				points = 10;
			}
			
			this.specType = specCardType.ValueOf(points);
			this.isSpecType = true;
			
			return true;
		}
		
		this.specType = specCardType.CARD_TYPE_NONE;
		for (int i=0; i<CARDS_NUM; ++i)
		{
			cardsTwo[i] = cards[i];
		}
		
		return false;
	}
	
	private boolean IsDivisibleToTen(int n)
	{
		return (n%10)==0;
	}
	
	public final boolean IsSpecType()
	{
		return this.isSpecType;
	}
	
	public final specCardType getSpecType()
	{
		return this.specType;
	}
	
	/** 调用该接口请勿修改返回的数组的值 */
	public final short[] getCards()
	{
		return this.cards;
	}
	
	private final int getValue(int card)
	{
		return Cards.getCardValue(card);
	}
	
	public final void reset()
	{
		for (int i=0; i<this.cards.length; ++i)
		{
			cards[i] = 0;
			cardsOne[i] = 0;
			cardsTwo[i] = 0;
		}
		
		this.specType = specCardType.CARD_TYPE_NONE;
		this.isSpecType = false;
	}
	

	@Override
	public String toString()
	{
		String str = "0x" + Integer.toHexString(cards[0]) + ",0x" + Integer.toHexString(cards[1]) + ",0x" + Integer.toHexString(cards[2]) + ",0x" 
				+ Integer.toHexString(cards[3]) + ",0x" + Integer.toHexString(cards[4]); 
		return str;
	}
	
	@SuppressWarnings("unused")
	private String convertValueToCard(int card)
	{
		int value = Cards.getCardValue(card);
		switch (value) 
		{
			case 1:
				return "A";
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
				return String.valueOf(value);
			case 11:
				return "J";
			case 12:
				return "Q";
			case 13:
				return "K";
		default:
			return "B";
		}
	}
}
