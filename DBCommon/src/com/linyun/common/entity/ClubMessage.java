package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Date;

public class ClubMessage  implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private int id ;
	private int clubId ; //俱乐部Id
	private int userId ; //玩家userId 
	private int applyId ;//申请加入俱乐部的玩家的id
	private byte type ; //消息类型
	private String content ; //消息内容
	private byte isRead ; //是否阅览  0--未读  1--已读
	private Date createTime ;
	
	public ClubMessage(){}
	
	public ClubMessage(int clubId, int userId, String content)
	{
		this.clubId = clubId;
		this.userId = userId;
		this.applyId = 0;
		this.type = 0;
		this.content = content;
		this.isRead = 0;
	}
	
	public ClubMessage(int clubId, int userId, int applyId, String content)
	{
		this.clubId = clubId;
		this.userId = userId;
		this.applyId = applyId;
		this.type = 1;
		this.content = content;
		this.isRead = 0;
	}
	
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
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public byte getType() {
		return type;
	}
	public void setType(byte type) {
		this.type = type;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public int getApplyId() {
		return applyId;
	}
	public void setApplyId(int applyId) {
		this.applyId = applyId;
	}
	public byte getIsRead() {
		return isRead;
	}
	public void setIsRead(byte isRead) {
		this.isRead = isRead;
	}

}
