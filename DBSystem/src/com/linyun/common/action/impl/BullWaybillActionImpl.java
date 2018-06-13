package com.linyun.common.action.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.linyun.common.action.BullWaybillAction;
import com.linyun.common.entity.BullWaybill;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.BullWaybillMapper;

/**
*  @Author walker
*  @Since 2018年5月26日
**/

public class BullWaybillActionImpl implements BullWaybillAction
{

	@Override
	public void addBullWaybill(BullWaybill b) 
	{
		SqlSession session = null ;
		 try
		 {
			 session = SqlSessionFactoryUtil.ssf.openSession();
			 BullWaybillMapper bullwaybillMapper = session.getMapper(BullWaybillMapper.class);
			 bullwaybillMapper.addBullWaybill(b);
			 session.commit();
		 }
		 finally
		 {
			 if(session != null)
			 {
				 session.close();
			 }
		 }
	}

	@Override
	public List<BullWaybill> selectWaybill(int roomType)
	{
		SqlSession session = null ;
		 try
		 {
			 session = SqlSessionFactoryUtil.ssf.openSession();
			 BullWaybillMapper bullwaybillMapper = session.getMapper(BullWaybillMapper.class);
			 List<BullWaybill> waybills = bullwaybillMapper.selectWaybill(roomType);
			 return waybills;
		 }
		 finally
		 {
			 if(session != null)
			 {
				 session.close();
			 }
		 }
	}

	@Override
	public BullWaybill getLastRoundResult(int roomType, int round) 
	{
		SqlSession session = null ;
		 try
		 {
			 session = SqlSessionFactoryUtil.ssf.openSession();
			 BullWaybillMapper bullwaybillMapper = session.getMapper(BullWaybillMapper.class);
			 Map<String,Object> map = new HashMap<String,Object>();
			 map.put("roomType",roomType);
			 map.put("round", round);
			 BullWaybill waybill = bullwaybillMapper.getLastRoundResult(map);
			 return waybill;
		 }
		 finally
		 {
			 if(session != null)
			 {
				 session.close();
			 }
		 }
	}

}
