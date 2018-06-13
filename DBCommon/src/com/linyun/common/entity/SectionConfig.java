package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

/**
*  @Author walker
*  @Since 2018年5月29日
**/

public class SectionConfig implements Serializable
{

	private static final long serialVersionUID = 1L;
	
	private int id;
	private int secType; //区间序列
	private int minMoney; 
	private int maxMoney;
	private int scale;
	private Timestamp createTime;
	private Timestamp updateTime;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSecType() {
		return secType;
	}
	public void setSecType(int secType) {
		this.secType = secType;
	}
	public int getMinMoney() {
		return minMoney;
	}
	public void setMinMoney(int minMoney) {
		this.minMoney = minMoney;
	}
	public int getMaxMoney() {
		return maxMoney;
	}
	public void setMaxMoney(int maxMoney) {
		this.maxMoney = maxMoney;
	}
	public int getScale() {
		return scale;
	}
	public void setScale(int scale) {
		this.scale = scale;
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
	
	

}
