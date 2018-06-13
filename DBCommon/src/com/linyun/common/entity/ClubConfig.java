package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Date;

public class ClubConfig implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	/*
	 *   configType的参数类型如下：
	 *10001-公共俱乐部每个玩家可消耗房卡的额度
     *10002-公共俱乐部每次开房消耗的额度
     *10003-私人俱乐最大开桌数
     *10004-允许玩家加入的私人俱乐部的上限
     *10005-私人俱乐部人数上限
     *10006-私人俱乐部每次开房消耗的额度
	 */
	
	private int id ;
	private int configType ;//配置的参数类型
	private int count ; //参数具体值
	private Date createTime;
	private Date updateTime;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getConfigType() {
		return configType;
	}
	public void setConfigType(int configType) {
		this.configType = configType;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
	

}
