package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class GiftCode  implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id ;
	private String giftCode; //礼品码
	private int presentDiamond; //
	private Timestamp createTime ;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	
	public String getGiftCode()
	{
		return giftCode;
	}
	public void setGiftCode(String giftCode)
	{
		this.giftCode = giftCode;
	}
	
	public int getPresentDiamond()
	{
		return presentDiamond;
	}
	public void setPresentDiamond(int presentDiamond)
	{
		this.presentDiamond = presentDiamond;
	}
}
