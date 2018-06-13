package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

/**
*  @Author walker
*  @Since 2018年5月29日
**/

public class Order implements Serializable
{
	
	public static final int STATUS_CREATED = 0;
	public static final int STATUS_VERIFIED =1;
	public static final int STATUS_REVOKED =2;
	public static final int STATUS_TIMEOUT = 3;
	
	private int id;
	private String orderId;//订单号
	private int userId; //用户id
	private int diamond; //充值钻石数
	private int amount; //充值金额 单位：分
	private int status; //订单状态 0表示创建 1 成功  2 撤销  3超时
	private Timestamp createTime; //创建时间
	private Timestamp updateTime; //更新时间
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public Timestamp getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
	public int getDiamond() {
		return diamond;
	}
	public void setDiamond(int diamond) {
		this.diamond = diamond;
	}
	

}
