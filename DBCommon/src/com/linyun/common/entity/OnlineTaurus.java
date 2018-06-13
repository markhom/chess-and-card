package com.linyun.common.entity;

import java.io.Serializable;

public class OnlineTaurus implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4616392052474936725L;
	
	private int id;
	private int onlineCount;
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	
	public int getOnlineCount()
	{
		return onlineCount;
	}
	public void setOnlineCount(int onlineCount)
	{
		this.onlineCount = onlineCount;
	}
}
