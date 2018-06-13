package com.linyun.common.entity;

import java.io.Serializable;

public class GiftCodeBindInfo implements Serializable
{
	//***
	private static final long serialVersionUID = 6942883057914313374L;
	
	private int id;
	private Integer userId;
	private String giftCode;
	private Integer diamond;//礼品码对应的赠送钻石数量
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	
	public Integer getUserId()
	{
		return userId;
	}
	public void setUserId(Integer userId)
	{
		this.userId = userId;
	}
	
	public String getGiftCode()
	{
		return giftCode;
	}
	public void setGiftCode(String giftCode)
	{
		this.giftCode = giftCode;
	}
	public Integer getDiamond()
	{
		return diamond;
	}
	public void setDiamond(Integer diamond)
	{
		this.diamond = diamond;
	}
}
