package com.linyun.club.taurus.utils;

import com.linyun.common.taurus.eum.specCardType;
import com.linyun.middle.common.taurus.card.Cards;
import com.linyun.middle.common.taurus.card.HandCard;
import com.linyun.middle.common.taurus.player.HundredsTaurusPlayer;

public class CardStyleMath
{
	
	public static final int CARD_TYPE_ALL_SMALL_ODDS = 8;
	public static final int CARD_TYPE_BOMB_ODDS = 6;
	public static final int CARD_TYPE_ALL_FACE_ODDS = 5;
	public static final int CARD_TYPE_ALL_ODDS_4 = 4;
	public static final int CARD_TYPE_NINE_ODDS_3 = 3;
	public static final int CARD_TYPE_EIGHT_ODDS_2 = 2;
	public static final int CARD_TYPE_SEVEN_ODDS_2 = 2;
	/**
	 * @param player 牛牛玩家  其中包括了玩家的押注情况<即，调用此函数时，玩家应该是已经压过注了>
	 * @param bankerCard 庄家牌
	 * @param playerCard1 闲家一的牌
	 * @param playerCard2 闲家二的牌
	 * @param playerCard3 闲家三的牌
	 * 
	 * 计算玩家一局的得分和实际抽水
	 * 	 * */
	public static void clacPlayerScore(HundredsTaurusPlayer player, int[] result)
	{   

		if (player.getBet_coin() == 0)
		{   
			player.setScore(0);
			return;
		}
		int bet_sky = player.getBet_sky();
		int bet_earth = player.getBet_earth();
		int bet_people = player.getBet_people();
		int score = 0;
	    if(bet_sky>0)
	    {
	    	score += result[0]*bet_sky;
	    }
	    if(bet_earth>0)
	    {
	    	score += result[1]*bet_earth;
	    }
	    if(bet_people>0)
	    {
	    	score += result[2]*bet_people;
	    }
		
		//玩家一局得分
		player.setScore(score);
	}
	
	public static int[] calPlayerIsWin(final HandCard bankerCard, final HandCard playerCard1, final HandCard playerCard2, final HandCard playerCard3)
	{
		int player1_odds = CompareTwoHandleCard(bankerCard,playerCard1);
		int player2_odds = CompareTwoHandleCard(bankerCard,playerCard2);
		int player3_odds = CompareTwoHandleCard(bankerCard,playerCard3);
		int[] result = new int[3];
		result[0] = player1_odds;
		result[1] = player2_odds;
		result[2] = player3_odds;
		return result;
	}
	
	 /**
	  * @param one 一副手牌
	  * @param two 另一副手牌
	  * @param mode 押注模式 1：平倍 2：翻倍 3：多倍
	  * 
	  * 返回one和two的比较结果，包括点数、输还是赢 
	  * 注：返回的结果直接去Odds中查找值
	  * */
	 public static int CompareTwoHandleCard(final HandCard bankerCard, final HandCard playerCard)
	 {
		 specCardType bankerType = bankerCard.getSpecType();
			specCardType playerType = playerCard.getSpecType();
			
			int bankerMaxCard = getMaxCardByHandCard(bankerCard);
			int playerMaxCard = getMaxCardByHandCard(playerCard);
			
			int points = 0;
			int odds = 0;
			if (bankerCard.IsSpecType() && playerCard.IsSpecType())
			{//都是特殊牌型
				points = bankerType.value - playerType.value;
				//都有牛，则比较牛的大小
				if (points == 0)
				{
					//牛大小相同,比最大一张牌大小
					if (compareTwoCards(bankerMaxCard, playerMaxCard))
					{
						odds = getOddsByTimesMode(bankerType)*(-1);
					}
					else
					{
						odds = getOddsByTimesMode(bankerType);
					}
				}
				else if (points > 0)
				{//banker > player
					odds = getOddsByTimesMode(bankerType)*(-1);
				}
				else 
				{//player>banker
					odds = getOddsByTimesMode(playerType);
				}
			}
			else if (bankerCard.IsSpecType() && !playerCard.IsSpecType())
			{ 
				//banker有牛，player没牛   banker>player 
				odds = getOddsByTimesMode(bankerType)*(-1);
			}
			else if (!bankerCard.IsSpecType() && playerCard.IsSpecType())
			{
				//banker没牛，player有牛， player>banker
				odds = getOddsByTimesMode(playerType);
			}
			else
			{
				//都没有牛，比较最大一张牌
				if (compareTwoCards(bankerMaxCard, playerMaxCard))
				{
					odds = -1;
				}
				else
				{
					odds = 1;
				}
			}
			
			return odds;
	 }
	 
	 public static int getOddsByTimesMode(specCardType specType)
		{
			int odds = 1;
			
			switch (specType)
			{
				/*case  CARD_TYPE_ALL_SMALL:
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
				}*/
				case CARD_TYPE_ALL:
				{//牛牛
					
					odds = CARD_TYPE_ALL_ODDS_4;
					break;
				}
				case CARD_TYPE_NINE:
				{//牛九
				    odds = CARD_TYPE_NINE_ODDS_3;
					break;
				}
				case CARD_TYPE_EIGHT:
				{//牛八
					odds=CARD_TYPE_EIGHT_ODDS_2;
					break;
				}
				case CARD_TYPE_SEVEN:
				{//牛七
					odds=CARD_TYPE_SEVEN_ODDS_2;
					break;
				}
				default:
				{
					break;
				}
					
			}
			return odds;
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
	 //比较两张牌的大小，前者大于后者返回true，否则<即小于>返回false 注：一副扑克牌不存在相等的两张牌
	 private static boolean compareTwoCards(final int card1, final int card2)
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
