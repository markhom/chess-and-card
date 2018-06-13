package com.linyun.bottom.util;

public class CardUtils 
{
	public static final int TAURUS_CARDS_NUM = 5;
	public static final char[] cs = new char[4];
    
	static
	{
		cs[0] = 0x2666;
		cs[1] = 0x2663;
		cs[2] = 0x2665;
		cs[3] = 0x2660;	    
	};
    
    public static char getSuit(int n)
    {
    	return cs[n-1];
    }
    
   
    public static short[] getTaurusHandCard(String cards)
    {
    	if (cards == null)
    	{
    		return null;
    	}
    	
    	String[] db_cards = cards.split(",");
    	if (db_cards.length != TAURUS_CARDS_NUM)
    	{
    		return null;
    	}
    	
    	short[] handCard = new short[TAURUS_CARDS_NUM];
    	String str = null;
    	for (int i=0; i<TAURUS_CARDS_NUM; ++i)
    	{
    		str = db_cards[i].substring(2);   //0x的长度
    		handCard[i] = Short.valueOf(str, 16);
    	}
    	
    	return handCard;
    }

}
