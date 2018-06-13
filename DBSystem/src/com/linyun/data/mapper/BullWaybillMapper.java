package com.linyun.data.mapper;

import java.util.List;
import java.util.Map;

import com.linyun.common.entity.BullWaybill;

/**
*  @Author walker
*  @Since 2018年5月26日
**/

public interface BullWaybillMapper 
{
	public void addBullWaybill(BullWaybill b);
	
	public List<BullWaybill> selectWaybill(int roomType);
	
	public BullWaybill getLastRoundResult(Map<String,Object> map);

}
