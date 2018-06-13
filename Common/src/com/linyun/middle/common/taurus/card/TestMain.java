package com.linyun.middle.common.taurus.card;

import com.linyun.bottom.util.CardUtils;
import com.linyun.middle.common.taurus.bean.RoomConfigSpecConfig;

public class TestMain
{
	public static void main(String[] args)
	{
//		HandCard card = new HandCard();
//		short[] cards = new short[5];
//		cards[0] = 0x61;
//		cards[1] = 0x62;
//		cards[2] = 0x63;
//		cards[3] = 0x64;
//		cards[4] = 0x71;
//		
//		card.setCards(cards);
//		
//		System.out.println(card.toString());
		
		short[] cards = CardUtils.getTaurusHandCard("0x41,0x62,0xA3,0x64,0x31");
		
		HandCard handCard = new HandCard();
		handCard.setCards(cards);
		
		RoomConfigSpecConfig config = new RoomConfigSpecConfig();
		config.setAllFace(true);
		config.setAllSmall(true);
		config.setBomb(true);
		
		handCard.calcCardType(config);
		
		System.out.println(handCard.getSpecType());
		
	}
	
	public static void test()
	{
		
	}
	

}
