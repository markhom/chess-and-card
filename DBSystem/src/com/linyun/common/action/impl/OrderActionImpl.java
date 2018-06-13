package com.linyun.common.action.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.linyun.common.action.OrderAction;
import com.linyun.common.entity.Order;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.OrderMapper;
import com.linyun.data.server.ActionAware;

/**
*  @Author walker
*  @Since 2018年5月29日
**/

public class OrderActionImpl extends ActionAware implements OrderAction
{

	@Override
	public void saveOrder(Order o)
	{
		SqlSession session = null;
		try
		{
			 session = SqlSessionFactoryUtil.ssf.openSession();
			 OrderMapper mapper = session.getMapper(OrderMapper.class);
			 mapper.saveOrder(o);
			 session.commit();
			
		}finally
		{
			if(session != null)
			{
				session.close();
			}
		}
	}

	@Override
	public void updateOrderStatus(String orderId, int status) 
	{
		SqlSession session = null;
		try
		{
			 session = SqlSessionFactoryUtil.ssf.openSession();
			 OrderMapper mapper = session.getMapper(OrderMapper.class);
			 Map<String,Object> map = new HashMap<String,Object>();
			 map.put("orderId", orderId);
			 map.put("status",status);
			 mapper.updateOrderStatus(map);
			 session.commit();
			
		}finally
		{
			if(session != null)
			{
				session.close();
			}
		}
		
	}

	@Override
	public Order selectByOrderId(String orderId) 
	{
		SqlSession session = null;
		try
		{
			 session = SqlSessionFactoryUtil.ssf.openSession();
			 OrderMapper mapper = session.getMapper(OrderMapper.class);
			 Order o = mapper.selectByOrderId(orderId);
			 return o;
			
		}finally
		{
			if(session != null)
			{
				session.close();
			}
		}
	}

}
