package com.linyun.middle.common.taurus.utils;

import com.linyun.common.taurus.eum.BankerMode;
import com.linyun.common.taurus.eum.RoomPayMode;
import com.linyun.common.taurus.eum.RoundNum;

public class DiamondUtils
{
	public static int getPayDiamond(BankerMode bankerMode, RoomPayMode payMode, int roundNum)
	{
		int diamond = 0;
		if (payMode == RoomPayMode.PAY_MODE_ALL)
		{//AA支付
			if (bankerMode == BankerMode.BANKER_MODE_ROTATE)
			{
				diamond = 1;
			}
			else 
			{
				if (roundNum == RoundNum.ROUND_NUM_10)
				{
					diamond = 1;
				}
				else
				{
					diamond = 2;
				}
			}
		}
		else
		{
			if (bankerMode == BankerMode.BANKER_MODE_ROTATE)
			{
				diamond = 3;
			}
			else
			{
				if (roundNum == RoundNum.ROUND_NUM_10)
				{
					diamond = 3;
				}
				else
				{
					diamond = 6;
				}
			}
		}
		
		return diamond;
	}
}
