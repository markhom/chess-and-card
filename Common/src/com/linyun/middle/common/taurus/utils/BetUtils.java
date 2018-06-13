package com.linyun.middle.common.taurus.utils;

import com.linyun.common.taurus.eum.BankerMode;
import com.linyun.middle.common.taurus.bean.TaurusRoomConfig;

public class BetUtils
{
	public static int getLeastBet(TaurusRoomConfig config)
	{
		int leastBetCoin = 0;
		
		if (config.getBankerMode() == BankerMode.BANKER_MODE_ROTATE || config.getBankerMode() == BankerMode.BANKER_MODE_TAURUS 
			|| config.getBankerMode() == BankerMode.BANKER_MODE_FIXED || config.getBankerMode() == BankerMode.BANKER_MODE_FREE
			|| config.getBankerMode() == BankerMode.BANKER_MODE_BRIGHT_ROB)
		{//轮庄
			switch (config.getBaseScore())
			{
				case MODE_BANKER_CHOICE:
				{
					leastBetCoin = 2;
					break;
				}
				case MODE_1_2:
				{
					leastBetCoin = 1;
					break;
				}
				case MODE_2_4:
				{
					leastBetCoin = 2;
					break;
				}
				case MODE_4_8:
				{
					leastBetCoin = 4;
					break;
				}
				case MODE_SCROLL_SELECTED:
				{
					leastBetCoin = config.getClubRoomBaseScore();
					break;
				}
				default:
					break;
			}
		}
		
		return leastBetCoin;
	}
}
