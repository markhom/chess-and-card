package com.linyun.middle.common.taurus.card;

import com.linyun.common.taurus.eum.TimesMode;
import com.linyun.common.taurus.eum.specCardType;
import com.linyun.middle.common.taurus.bean.TaurusRoomConfig;
import com.linyun.middle.common.taurus.player.TaurusPlayer;

public class CardStyleMath
{
	public static final int CARD_TYPE_ALL_SMALL_ODDS = 8;
	public static final int CARD_TYPE_BOMB_ODDS = 6;
	public static final int CARD_TYPE_ALL_FACE_ODDS = 5;
	public static final int CARD_TYPE_ALL_ODDS_4 = 4;
	public static final int CARD_TYPE_ALL_ODDS_3 = 3;
	public static final int CARD_TYPE_NINE_ODDS_3 = 3;
	public static final int CARD_TYPE_NINE_ODDS_2 = 2;
	public static final int CARD_TYPE_EIGHT_ODDS_2 = 2;
	public static final int CARD_TYPE_SEVEN_ODDS_2 = 2;
	
	//比较两手牌的大小
	public static int comparePlayerCards(HandCard card1, HandCard card2,TaurusRoomConfig config)
	{
		specCardType bankerType = card1.getSpecType();
		specCardType playerType = card2.getSpecType();
		
		int bankerMaxCard = getMaxCardByHandCard(card1);
		int playerMaxCard = getMaxCardByHandCard(card2);
		if (config.getSpecConfig().isAllSmall() && ((bankerType==specCardType.CARD_TYPE_ALL_SMALL)||(playerType==specCardType.CARD_TYPE_ALL_SMALL)))
		{//先判断是否有五小牛
			if ((bankerType==specCardType.CARD_TYPE_ALL_SMALL) && (playerType==specCardType.CARD_TYPE_ALL_SMALL))
			{
				//均为五小牛
				if (compareTwoCards(bankerMaxCard, playerMaxCard))
				{//庄家大
					return 1;
				}
				else
				{//闲家大
					return -1;
				}
			}
			else if (bankerType==specCardType.CARD_TYPE_ALL_SMALL)
			{//庄家五小牛大
				return 1;
			}
			else
			{//闲家五小牛大
				return -1;
			}
		}
		
		if (config.getSpecConfig().isBomb() && ((bankerType==specCardType.CARD_TYPE_BOMB)||(playerType==specCardType.CARD_TYPE_BOMB)))
		{//判断是否有炸弹牛
			if ((bankerType==specCardType.CARD_TYPE_BOMB) && (playerType==specCardType.CARD_TYPE_BOMB))
			{
				//均为炸弹牛
				if (compareTwoCards(card1.getCards()[0], card2.getCards()[0]))
				{//庄家大
					return 1;
				}
				else
				{//闲家大
					return -1;
				}
			}
			else if (bankerType==specCardType.CARD_TYPE_BOMB)
			{//庄家炸弹牛大
				return 1;
			}
			else
			{//闲家炸弹牛大
				return -1;
			}
		}
		
		if (config.getSpecConfig().isAllFace() && ((bankerType==specCardType.CARD_TYPE_ALL_FACE)||(playerType==specCardType.CARD_TYPE_ALL_FACE)))
		{//判断是否有五花牛
			if ((bankerType==specCardType.CARD_TYPE_ALL_FACE) && (playerType==specCardType.CARD_TYPE_ALL_FACE))
			{
				//均为五花牛
				if (compareTwoCards(bankerMaxCard, playerMaxCard))
				{//庄家大
					return 1;
				}
				else
				{//闲家大
					return -1;
				}
			}
			else if (bankerType==specCardType.CARD_TYPE_ALL_FACE)
			{//庄家五花牛大
				return 1;
			}
			else
			{//闲家五花牛大
				return -1;
			}
		}
		
		int points = 0;
		if (card1.IsSpecType() && card2.IsSpecType())
		{//都是特殊牌型
			points = bankerType.value - playerType.value;
			//都有牛，则比较牛的大小
			if (points == 0)
			{
				//牛大小相同,比最大一张牌大小
				if (compareTwoCards(bankerMaxCard, playerMaxCard))
				{
					return 1;
				}
				else
				{
				    return -1;
				}
			}
			else if (points > 0)
			{//banker > player
				return 1;
			}
			else 
			{//player>banker
				return -1;
			}
		}
		else if (card1.IsSpecType() && !card2.IsSpecType())
		{ 
			//banker有牛，player没牛   banker>player 
			return 1;
		}
		else if (!card1.IsSpecType() && card2.IsSpecType())
		{
			//banker没牛，player有牛， player>banker
			return -1;
		}
		else
		{
			//都没有牛，比较最大一张牌
			if (compareTwoCards(bankerMaxCard, playerMaxCard))
			{
				return 1;
			}
			else
			{
				return -1;
			}
		}
	}
	/**
	 * @param player 牛牛玩家  其中包括了玩家的押注情况<即，调用此函数时，玩家应该是已经押过注了>
	 * @param bankerCard 庄家牌
	 * @param playerCard 闲家的牌
	 * @param config 特殊牌型的选择情况
	 * 
	 * 	 * */
	public static void clacPlayerScore(TaurusPlayer player, final HandCard bankerCard, final HandCard playerCard,final TaurusRoomConfig config)
	{
		specCardType bankerType = bankerCard.getSpecType();
		specCardType playerType = playerCard.getSpecType();
		
		int bankerMaxCard = getMaxCardByHandCard(bankerCard);
		int playerMaxCard = getMaxCardByHandCard(playerCard);
		if (config.getSpecConfig().isAllSmall() && ((bankerType==specCardType.CARD_TYPE_ALL_SMALL)||(playerType==specCardType.CARD_TYPE_ALL_SMALL)))
		{//先判断是否有五小牛
			int score = player.getBetCoin()*CARD_TYPE_ALL_SMALL_ODDS;
			if ((bankerType==specCardType.CARD_TYPE_ALL_SMALL) && (playerType==specCardType.CARD_TYPE_ALL_SMALL))
			{
				//均为五小牛
				if (compareTwoCards(bankerMaxCard, playerMaxCard))
				{//庄家大
					player.setScore(-1*score);
				}
				else
				{//闲家大
					player.setScore(score);
				}
			}
			else if (bankerType==specCardType.CARD_TYPE_ALL_SMALL)
			{//庄家五小牛大
				player.setScore(-1*score);
			}
			else
			{//闲家五小牛大
				player.setScore(score);
			}
			return;
		}
		
		if (config.getSpecConfig().isBomb() && ((bankerType==specCardType.CARD_TYPE_BOMB)||(playerType==specCardType.CARD_TYPE_BOMB)))
		{//判断是否有炸弹牛
			int score = player.getBetCoin()*CARD_TYPE_BOMB_ODDS;
			if ((bankerType==specCardType.CARD_TYPE_BOMB) && (playerType==specCardType.CARD_TYPE_BOMB))
			{
				//均为炸弹牛
				if (compareTwoCards(bankerCard.getCards()[0], playerCard.getCards()[0]))
				{//庄家大
					player.setScore(-1*score);
				}
				else
				{//闲家大
					player.setScore(score);
				}
			}
			else if (bankerType==specCardType.CARD_TYPE_BOMB)
			{//庄家炸弹牛大
				player.setScore(-1*score);
			}
			else
			{//闲家炸弹牛大
				player.setScore(score);
			}
			return;
		}
		
		if (config.getSpecConfig().isAllFace() && ((bankerType==specCardType.CARD_TYPE_ALL_FACE)||(playerType==specCardType.CARD_TYPE_ALL_FACE)))
		{//判断是否有五花牛
			int score = player.getBetCoin()*CARD_TYPE_ALL_FACE_ODDS;
			if ((bankerType==specCardType.CARD_TYPE_ALL_FACE) && (playerType==specCardType.CARD_TYPE_ALL_FACE))
			{
				//均为五花牛
				if (compareTwoCards(bankerMaxCard, playerMaxCard))
				{//庄家大
					player.setScore(-1*score);
				}
				else
				{//闲家大
					player.setScore(score);
				}
			}
			else if (bankerType==specCardType.CARD_TYPE_ALL_FACE)
			{//庄家五花牛大
				player.setScore(-1*score);
			}
			else
			{//闲家五花牛大
				player.setScore(score);
			}
			return;
		}
		
		int points = 0;
		final int betCoin = player.getBetCoin();
		if (bankerCard.IsSpecType() && playerCard.IsSpecType())
		{//都是特殊牌型
			points = bankerType.value - playerType.value;
			//都有牛，则比较牛的大小
			if (points == 0)
			{
				//牛大小相同,比最大一张牌大小
				if (compareTwoCards(bankerMaxCard, playerMaxCard))
				{
					player.setScore(getOddsByTimesMode(bankerType, config.getTimesMode())*betCoin*(-1));
				}
				else
				{
					player.setScore(getOddsByTimesMode(bankerType, config.getTimesMode())*betCoin);
				}
			}
			else if (points > 0)
			{//banker > player
				player.setScore(getOddsByTimesMode(bankerType, config.getTimesMode())*betCoin*(-1));
			}
			else 
			{//player>banker
				player.setScore(getOddsByTimesMode(playerType, config.getTimesMode())*betCoin);
			}
		}
		else if (bankerCard.IsSpecType() && !playerCard.IsSpecType())
		{ 
			//banker有牛，player没牛   banker>player 
			player.setScore(getOddsByTimesMode(bankerType, config.getTimesMode())*betCoin*(-1));
		}
		else if (!bankerCard.IsSpecType() && playerCard.IsSpecType())
		{
			//banker没牛，player有牛， player>banker
			player.setScore(getOddsByTimesMode(playerType, config.getTimesMode())*betCoin);
		}
		else
		{
			//都没有牛，比较最大一张牌
			if (compareTwoCards(bankerMaxCard, playerMaxCard))
			{
				player.setScore((-1)*betCoin);
			}
			else
			{
				player.setScore(betCoin);
			}
		}
	}
	
	
	public static short getMaxCardByHandCard(final HandCard one)
	{
		if (one == null)
		{
			return 0;
		}
		
		final short[] cards = one.getCards();
		
		short maxCard = cards[0];
		for (int i=1; i<cards.length; ++i)
		{
			if (maxCard < cards[i])
			{
				maxCard = cards[i];
			}
		}
		
		return maxCard;
	}
	
	public static int getOddsByTimesMode(specCardType specType, TimesMode timesMode)
	{
		int odds = 1;
		
		switch (specType)
		{
			case  CARD_TYPE_ALL_SMALL:
			{//五小牛
				odds=CARD_TYPE_ALL_SMALL_ODDS;
				break;
			}
			case  CARD_TYPE_BOMB:
			{//炸弹牛
				odds=CARD_TYPE_BOMB_ODDS;
				break;
			}
			case  CARD_TYPE_ALL_FACE:
			{//五花牛
				odds=CARD_TYPE_ALL_FACE_ODDS;
				break;
			}
			case CARD_TYPE_ALL:
			{//牛牛
				if (timesMode == TimesMode.TIMES_MODE_TAURUS_4)
				{
					odds = CARD_TYPE_ALL_ODDS_4;
				}
				else
				{
					odds = CARD_TYPE_ALL_ODDS_3;
				}
				break;
			}
			case CARD_TYPE_NINE:
			{//牛九
				if (timesMode == TimesMode.TIMES_MODE_TAURUS_4)
				{
					odds = CARD_TYPE_NINE_ODDS_3;
				}
				else
				{
					odds=CARD_TYPE_NINE_ODDS_2;
				}
				break;
			}
			case CARD_TYPE_EIGHT:
			{//牛八
				odds=CARD_TYPE_EIGHT_ODDS_2;
				break;
			}
			case CARD_TYPE_SEVEN:
			{//牛七
				if (timesMode == TimesMode.TIMES_MODE_TAURUS_4)
				{
					odds=CARD_TYPE_SEVEN_ODDS_2;
				}
				break;
			}
			default:
			{
				break;
			}
				
		}
		
		return odds;
	}
	
	
	//比较两张牌的大小，前者大于后者返回true，否则<即小于>返回false 注：一副扑克牌不存在相等的两张牌
	public static boolean compareTwoCards(final int card1, final int card2)
	{
		int value1 = Cards.getCardValue(card1);
		int value2 = Cards.getCardValue(card2);
		int value = value1 - value2;
		if (value == 0)
		{
			int suit1 = Cards.getCardSuit(card1);
			int suit2 = Cards.getCardSuit(card2);
			int suit = suit1 - suit2;
			if (suit > 0)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else if (value > 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	
}
