package com.linyun.common.entity;

import java.io.Serializable;

public class UserNickName implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6276409391382588231L;
	private int id ;
	private String nickName ;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

}
