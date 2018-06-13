package com.linyun.middle.common.taurus.card;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import com.linyun.bottom.util.CardUtils;
import com.linyun.common.utils.ConstantsUtils;

public class Cards
{    
	//是否测试模式，测试模式时，可以摆牌，非测试模式时，不能摆牌
	public static final boolean isTest = false;
	private static final int SEAT_NUM = 6;
	
	// 基本牌型
	static short typeArray[] = 
	{
		0x11, 0x12, 0x13, 0x14, // A
		0x21, 0x22, 0x23, 0x24, // 2
		0x31, 0x32, 0x33, 0x34, // 3
		0x41, 0x42, 0x43, 0x44, // 4
		0x51, 0x52, 0x53, 0x54, // 5
		0x61, 0x62, 0x63, 0x64, // 6
		0x71, 0x72, 0x73, 0x74, // 7
		0x81, 0x82, 0x83, 0x84, // 8
		0x91, 0x92, 0x93, 0x94, // 9
		0xa1, 0xa2, 0xa3, 0xa4, // 10
		0xb1, 0xb2, 0xb3, 0xb4, // J
		0xc1, 0xc2, 0xc3, 0xc4, // Q
		0xd1, 0xd2, 0xd3, 0xd4  // K
	};
	
	public Cards()
	{
		init();
	}
	
	public static enum Suit
	{// 花色
		Diamonds((byte) 0x01), // 方块
		Clubs((byte) 0x02), // 梅花
		Hearts((byte) 0x03), // 红桃
		Spades((byte) 0x04);// 黑桃
		byte value;

		Suit(byte v)
		{
			value = v;
		}
	};

	public static int getCardValue(int card)
	{
		return (card&0xf0)>>4;
	}
	public static int getCardSuit(int card)
	{
		return (card&0x0f);
	}
	
	private static short[] getAllCards()
	{
		synchronized (typeArray)
		{
			shuffle(typeArray,new Random());
			short[] cardsArray = new short[typeArray.length];
			for (int i=0; i<cardsArray.length; ++i)
			{
				cardsArray[i] = typeArray[i];
			}
			return cardsArray;
		}
	}
	
	private static void shuffle(short[] array, Random random)
    {
        for(int i = array.length; i >= 1; i--)
        {
            swap(array,i-1,random.nextInt(i));
        }
    }
    private static void swap(short[] array, int i , int j)
    {
        short temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
	
	
	//去掉大括号
	 public static String delBrace(String str)
	 {  
		String st =  str.substring(str.indexOf("{")+1, str.lastIndexOf("}"));
		return st;
	 }
	 
	 //从整副牌中随机抽取一张牌 
	 public static int getOneCard()
	 {   
		 Random rondom = new Random();
		 int index = rondom.nextInt(typeArray.length);
		 return typeArray[index] ;
	 }
	 
	 private void init() 
	 {
		
     }	
	 
	 private static Properties getTestHandCard()
	 {
		 
		 Properties pro = new Properties();
		 try
		 {
			 InputStream in = new FileInputStream(ConstantsUtils.SERVER_PATH+File.separator+"config/cards.properties");
			 pro.load(in);
		 }
		 catch (Exception e)
		 {
			 e.printStackTrace();
			 return null;
		 }
		 return pro;
	 }
	 
	
	public static HandCard[] getAllHandCard(String roomNum)
	{
		short[] cards = getAllCards();
		HandCard[] gp = new HandCard[SEAT_NUM];
		
		Properties prop = getTestHandCard();
		String roomNum2 =prop.getProperty("roomNum").trim();
		boolean isTest = false;
		if ((prop != null) && roomNum2.equals(roomNum))
		{
			isTest = true;
		}
		 
		if (isTest)//测试模式下，并且房间号和配置文件内的相同
		{//测试模式，可以摆牌
			List<Short> sCardList = new LinkedList<Short>();//遍历的主要list
			for (int i=0; i<cards.length; ++i)
			{
				sCardList.add(cards[i]);
			}
			 
			boolean[] isHasCards = new boolean[SEAT_NUM];
			int [] cardsCount = new int[SEAT_NUM];
			for (int i=0; i<isHasCards.length; ++i)
			{
				isHasCards[i] = false;
				cardsCount[i] = 0;
			}
			short[][] allHandCards = new short[SEAT_NUM][HandCard.CARDS_NUM];
			
			for (int i=0; i<HandCard.CARDS_NUM; ++i) //把6个玩家的牌放进二维数组中，同时在整副牌中移去
			{
				short[] handCard = CardUtils.getTaurusHandCard(prop.getProperty("cards" + (i+1)));
				if (handCard != null)
				{//读到了有效牌型，需要做摆牌处理
					int iCount = 0;
					for (int j=0; j<HandCard.CARDS_NUM; ++j)
					{
						for (int k=0; k<sCardList.size(); ++k)
						{
							if (handCard[j] == sCardList.get(k))
							{
								sCardList.remove(k);
								allHandCards[i][iCount] = handCard[j];
								++iCount;
								break;
							}
						}
					}
					isHasCards[i] = true; 
					cardsCount[i] = iCount;
				}
			}
			
			for (int i=0; i<SEAT_NUM; ++i)
			{
				if (isHasCards[i])
				{
					if (cardsCount[i] < HandCard.CARDS_NUM) //如果缺牌则补牌
					{
						for (int j=cardsCount[i]; j<HandCard.CARDS_NUM; ++j)
						{
							allHandCards[i][j] = sCardList.get(0);
							sCardList.remove(0);
						}
					}
				}
			}
			
			for (int i=0; i<SEAT_NUM; ++i)
			{
				if (!isHasCards[i])
				{
					for (int j=0; j<HandCard.CARDS_NUM; ++j)
					{
						allHandCards[i][j] = sCardList.get(0);
						sCardList.remove(0);
					}
				}
			}
			
			//将二维数组拆分，赋值到手牌数组中
			short[] hCard = new short[HandCard.CARDS_NUM];
			for (int i=0; i<allHandCards.length; ++i)
			{
				 for (int j = 0; j < hCard.length; j++)
				 {
					 hCard[j]  = allHandCards[i][j];
				 }
				
				gp[i] = new HandCard();
				gp[i].setCards(hCard);
			}
			return gp ;
			
		}
		else
		{
			short[] sHandCard = new short[HandCard.CARDS_NUM];
			for (int i=0; i<gp.length; ++i)
			{
				sHandCard[0] = cards[i*HandCard.CARDS_NUM];
				sHandCard[1] = cards[i*HandCard.CARDS_NUM+1];
				sHandCard[2] = cards[i*HandCard.CARDS_NUM+2];
				sHandCard[3] = cards[i*HandCard.CARDS_NUM+3];
				sHandCard[4] = cards[i*HandCard.CARDS_NUM+4];
				 
				gp[i] = new HandCard();
				gp[i].setCards(sHandCard);
			}
			
			return gp;
		}
		 
	}
	 
	
	 
}
