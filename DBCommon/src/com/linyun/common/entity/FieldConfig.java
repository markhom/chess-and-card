package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

/**
*  @Author walker
*  @Since 2018年5月23日
**/

public class FieldConfig implements Serializable
{

	/**
	 * 场次配置类
	 */
	private static final long serialVersionUID = -6337450631087380139L;
	
	public static final int FIELD_OPEN = 1;//场次开放
	public static final int FIELD_CLOSE =0;//场次关闭
	
	private int id;
	private int typeId;
	private String fieldName;
	private int entryLimit;
	private int min_bet;
	private int rate;
	private byte isOpen;//场次是否关闭
	private int upBankerLimit;//上庄金额
	private Timestamp updateTime;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getTypeId() {
		return typeId;
	}
	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public int getEntryLimit() {
		return entryLimit;
	}
	public void setEntryLimit(int entryLimit) {
		this.entryLimit = entryLimit;
	}
	public int getMin_bet() {
		return min_bet;
	}
	public void setMin_bet(int min_bet) {
		this.min_bet = min_bet;
	}
	public int getRate() {
		return rate;
	}
	public void setRate(int rate) {
		this.rate = rate;
	}
	public byte getIsOpen() {
		return isOpen;
	}
	public void setIsOpen(byte isOpen) {
		this.isOpen = isOpen;
	}
	public int getUpBankerLimit() {
		return upBankerLimit;
	}
	public void setUpBankerLimit(int upBankerLimit) {
		this.upBankerLimit = upBankerLimit;
	}
	

}
