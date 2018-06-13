package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class CustomService implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4599484905341872974L;
	private int id ;
	private String qqGroup ; //qq群
	private String wxPublicAccount ; //微型公众号
	private String qqService ; //游戏客服qq
	private Timestamp createTime ;
	private Timestamp modifyTime ;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getQqGroup() {
		return qqGroup;
	}
	public void setQqGroup(String qqGroup) {
		this.qqGroup = qqGroup;
	}
	public String getWxPublicAccount() {
		return wxPublicAccount;
	}
	public void setWxPublicAccount(String wxPublicAccount) {
		this.wxPublicAccount = wxPublicAccount;
	}
	public String getQqService() {
		return qqService;
	}
	public void setQqService(String qqService) {
		this.qqService = qqService;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public Timestamp getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Timestamp modifyTime) {
		this.modifyTime = modifyTime;
	}



}
