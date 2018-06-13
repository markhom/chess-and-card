package com.linyun.middle.common.taurus.utils;

import com.linyun.bottom.util.OutputMessage;
import com.linyun.common.taurus.eum.BankerChooseBaseScore;
import com.linyun.common.taurus.eum.BaseScoreType;
import com.linyun.common.taurus.eum.GameStatus;
import com.linyun.common.taurus.eum.PlayerInjection;
import com.linyun.middle.common.taurus.bean.TaurusRoomConfig;
import com.linyun.middle.common.taurus.card.CardStyleMath;
import com.linyun.middle.common.taurus.card.HandCard;

/**
 * 
 * */

public class TaurusEngineMathUtils
{
	/**
	 * @param baseScore 底分类型，这里的底分类型只能为 1/2 2/4 4/8 三种
	 * @param injection 推注倍率，这里的推注分数只能为5 10 20倍率 三种
	 * 上面的两个参数由调用者保证，底分类型为庄家选择的时候没有闲家推注，闲家推注未开启的时候也没有闲家推注
	 * */
	public static int getMaxInjectionScore(BaseScoreType baseScore, TaurusRoomConfig config)
	{
		PlayerInjection injection = config.getPlayerInjection();
		if(baseScore == BaseScoreType.MODE_BANKER_CHOICE)
		{
			return 0;
		}
		if (injection == PlayerInjection.PLAYER_INJECTION_NONE)
		{
			return 0;
		}
		int maxInjectionScore = 0;
		if (baseScore == BaseScoreType.MODE_1_2)
		{
			maxInjectionScore = 2 * (injection.value + 1);
		}
		else if (baseScore == BaseScoreType.MODE_2_4)
		{
			maxInjectionScore = 4 * (injection.value + 1);
		}
		else if(baseScore == BaseScoreType.MODE_4_8)
		{
			maxInjectionScore = 8 * (injection.value + 1);
		}else
		{
			maxInjectionScore = config.getClubRoomBaseScore() * (injection.value + 1)*2;
		}
		
		return maxInjectionScore;
	}
	
	public static int getMaxBaseScore(TaurusRoomConfig config)
	{
		if (config.getBaseScore() == BaseScoreType.MODE_BANKER_CHOICE)
		{
			return 0;
		}
		
		if (config.getBaseScore()  == BaseScoreType.MODE_1_2)
		{
			return 2;
		}
		else if (config.getBaseScore()  == BaseScoreType.MODE_2_4)
		{
			return 4;
		}
		else if(config.getBaseScore() == BaseScoreType.MODE_4_8)
		{
			return 8;
		}else
		{
			return config.getClubRoomBaseScore()*2;
		}
	}
	
	public static void setBaseScoreOM(OutputMessage om, TaurusRoomConfig config)
	{
		BaseScoreType baseScore = config.getBaseScore();
		if (baseScore == BaseScoreType.MODE_BANKER_CHOICE)
		{
			return;
		}
		
		if (baseScore == BaseScoreType.MODE_1_2)
		{
			om.putInt(1);
			om.putInt(2);
		}
		else if (baseScore == BaseScoreType.MODE_2_4)
		{
			om.putInt(2);
			om.putInt(4);
		}
		else if(baseScore == BaseScoreType.MODE_4_8)
		{
			om.putInt(4);
			om.putInt(8);
		}else
		{
			om.putInt(config.getClubRoomBaseScore());
			om.putInt(config.getClubRoomBaseScore()*2);
		}
	}
	
	/** 比较两副牌牛牛的大小 */
	public static boolean compareTaurusTaurus(HandCard one, HandCard two)
	{			
		if (CardStyleMath.compareTwoCards(getMaxCardByHandCard(one), getMaxCardByHandCard(two)))
		{
			return true;
		}
		
		return false;
	}
	private static short getMaxCardByHandCard(final HandCard one)
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
	
	
	public static OutputMessage getBetOM(int round, TaurusRoomConfig config,BankerChooseBaseScore baseScore, PlayerInjection injection)
	{
		//@need 通知客户端开始押注
		OutputMessage om = new OutputMessage(true);
		om.putInt(round);
		om.putByte(GameStatus.GAME_STATUS_TABLE_BET.value);
		
		BaseScoreType scoreType = config.getBaseScore();
		if (scoreType == BaseScoreType.MODE_BANKER_CHOICE)
		{
			switch (baseScore)
			{
				case BANKER_BASE_SCORE_2:
				{
					om.putByte((byte)1);//筹码个数,后面的为筹码的具体值
					om.putInt(2);
					break;
				}
				case BANKER_BASE_SCORE_4:
				{
					om.putByte((byte)2);//筹码个数,后面的为筹码的具体值
					om.putInt(2);
					om.putInt(4);
					break;
				}
				case BANKER_BASE_SCORE_8:
				{
					om.putByte((byte)3);//筹码个数,后面的为筹码的具体值
					om.putInt(2);
					om.putInt(4);
					om.putInt(8);
					break;
				}
				case BANKER_BASE_SCORE_16:
				{
					om.putByte((byte)4);//筹码个数,后面的为筹码的具体值
					om.putInt(2);
					om.putInt(4);
					om.putInt(8);
					om.putInt(16);
					break;
				}
				case BANKER_BASE_SCORE_32:
				{
					om.putByte((byte)5);//筹码个数,后面的为筹码的具体值
					om.putInt(2);
					om.putInt(4);
					om.putInt(8);
					om.putInt(16);
					om.putInt(32);
					break;
				}
				default:
				{
					om.putByte((byte)1);//筹码个数,后面的为筹码的具体值
					om.putInt(2);
					break;
				}
			}
		}
		else
		{
			if (injection == PlayerInjection.PLAYER_INJECTION_NONE)
			{//房间未开启闲家推注选项，只能押固定分数
				om.putByte((byte)2);//押注的筹码个数
				TaurusEngineMathUtils.setBaseScoreOM(om, config);
			}
		}
		return om;
	}
}

