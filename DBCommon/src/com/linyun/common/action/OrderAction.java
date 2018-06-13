package com.linyun.common.action;

import com.linyun.common.entity.Order;

/**
*  @Author walker
*  @Since 2018年5月29日
**/

public interface OrderAction 
{
   public void saveOrder(Order o);
   
   public void updateOrderStatus(String orderId, int status);
   
   public Order selectByOrderId(String orderId);
}
