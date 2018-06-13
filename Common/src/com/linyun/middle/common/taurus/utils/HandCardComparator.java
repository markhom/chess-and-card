package com.linyun.middle.common.taurus.utils;

import java.util.Comparator;

import com.linyun.middle.common.taurus.bean.TaurusRoomConfig;
import com.linyun.middle.common.taurus.card.CardStyleMath;
import com.linyun.middle.common.taurus.table.TaurusSeat;

/**
*  @Author walker
*  @Since 2018年4月23日
**/

public class HandCardComparator implements Comparator<TaurusSeat> 
{
    private TaurusRoomConfig config;
    
    
    
	public TaurusRoomConfig getConfig() {
		return config;
	}



	public void setConfig(TaurusRoomConfig config) {
		this.config = config;
	}



	@Override
	public int compare(TaurusSeat seat1, TaurusSeat seat2) 
	{
		return -CardStyleMath.comparePlayerCards(seat1.getCards(), seat2.getCards(), this.config);
	}
	
	

}
