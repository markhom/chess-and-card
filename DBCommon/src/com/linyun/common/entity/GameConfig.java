package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class GameConfig implements Serializable
{	
	 /**
	 * 
	 */
	private static final long serialVersionUID = -7037930859612440768L;
	private int id ;
	 private byte type ;  //活动类型  1-绑定邀请码送钻石
	 private int addDiamond ; //增加钻石的数量
	 private Timestamp updateTime ;
	 
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public byte getType() {
		return type;
	}
	public void setType(byte type) {
		this.type = type;
	}
	public int getAddDiamond() {
		return addDiamond;
	}
	public void setAddDiamond(int addDiamond) {
		this.addDiamond = addDiamond;
	}
	public Timestamp getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
}
