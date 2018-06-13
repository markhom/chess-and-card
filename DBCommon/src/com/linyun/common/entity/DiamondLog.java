package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class DiamondLog implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private int userId;
	private int oldDiamond;
	private int newDiamond;
	private int changedDiamond;
	private byte changedType;
	private Timestamp updateTime;
	private String remark ;
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public int getUserId()
	{
		return userId;
	}
	public void setUserId(int userId)
	{
		this.userId = userId;
	}
	public int getOldDiamond()
	{
		return oldDiamond;
	}
	public void setOldDiamond(int oldDiamond)
	{
		this.oldDiamond = oldDiamond;
	}
	public int getNewDiamond()
	{
		return newDiamond;
	}
	public void setNewDiamond(int newDiamond)
	{
		this.newDiamond = newDiamond;
	}
	public int getChangedDiamond()
	{
		return changedDiamond;
	}
	public void setChangedDiamond(int changedDiamond)
	{
		this.changedDiamond = changedDiamond;
	}
	public byte getChangedType()
	{
		return changedType;
	}
	public void setChangedType(byte changedType)
	{
		this.changedType = changedType;
	}
	public Timestamp getUpdateTime()
	{
		return updateTime;
	}
	public void setUpdateTime(Timestamp updateTime)
	{
		this.updateTime = updateTime;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
}
