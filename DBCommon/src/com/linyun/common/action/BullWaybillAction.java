package com.linyun.common.action;

import java.util.List;

import com.linyun.common.entity.BullWaybill;

/**
*  @Author walker
*  @Since 2018年5月26日
**/

public interface BullWaybillAction
{
	public void addBullWaybill(BullWaybill b);
	
	public List<BullWaybill> selectWaybill(int roomType);
	
	public BullWaybill getLastRoundResult(int roomType, int round);

}
