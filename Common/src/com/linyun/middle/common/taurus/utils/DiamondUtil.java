package com.linyun.middle.common.taurus.utils;

import com.linyun.common.taurus.eum.BankerMode;
import com.linyun.common.taurus.eum.RoundNum;

public class DiamondUtil {
     
	   public static int getPayDiamond(int round , BankerMode bankMode)
	   {
		   int diamond = 0 ;
		   //轮庄牛牛消耗的钻石与10局相同
		   if(bankMode == BankerMode.BANKER_MODE_ROTATE)
		   {
			   diamond = 3 ;
		   }
		   else
		   {
			   if(round == RoundNum.ROUND_NUM_10)
			   {
				   diamond = 3 ;
			   }
			   else
			   {
				   diamond = 6 ;
			   }
		   }
		   return diamond ;
	   }
}
