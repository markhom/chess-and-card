package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Date;

public class ClubOperateLog  implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private int id ;
	private int clubId ; //俱乐部Id
	private int operatorId ; //俱乐部操作者userId 
	private int clubPosition; //操作人在俱乐部的职位  0--创建者 1--普通成员 2--非俱乐部成员
	private int operateType ;//操作权限类型 普通成员： 0-申请加入俱乐部 1-退出俱乐部  创建者：10-创建俱乐部 11-删除俱乐部 12-审批加入申请通过 13-审批拒绝 14-邀请新成员 15-踢出成员  
	private String remark ;//备注
	private Date createTime ;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getClubId() {
		return clubId;
	}
	public void setClubId(int clubId) {
		this.clubId = clubId;
	}
	public int getOperatorId() {
		return operatorId;
	}
	public void setOperatorId(int operatorId) {
		this.operatorId = operatorId;
	}
	public int getClubPosition() {
		return clubPosition;
	}
	public void setClubPosition(int clubPosition) {
		this.clubPosition = clubPosition;
	}
	public int getOperateType() {
		return operateType;
	}
	public void setOperateType(int operateType) {
		this.operateType = operateType;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	
	
	

}
