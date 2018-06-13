package com.linyun.data.mapper;

import java.util.Map;

import com.linyun.common.entity.Order;

/**
*  @Author walker
*  @Since 2018年5月29日
**/

public interface OrderMapper 
{
    public void saveOrder(Order o);
    
    public void updateOrderStatus(Map<String,Object> map);
    
    public Order selectByOrderId(String orderId);
}
