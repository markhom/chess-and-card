package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class Marquee implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id ;
	private String content ; //跑马灯的内容
	private int voild ; // 跑马灯的启动和禁用
	private Timestamp createTime ;
	private Timestamp updateTime ;
	private byte pId ; // 每一条跑马灯的序列号

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
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
	public byte getpId() {
		return pId;
	}
	public void setpId(byte pId) {
		this.pId = pId;
	}
	public int getVoild() {
		return voild;
	}
	public void setVoild(int voild) {
		this.voild = voild;
	}




}
