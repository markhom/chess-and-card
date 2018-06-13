package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Date;

public class ClubDiamondLog  implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private int id ;
	private int clubId ;//俱乐部Id
	private int roomNum ;//房主创建的俱乐部房间号
	private int userId ; //俱乐部玩家Id
	private int diamond ;//消耗钻石的数量
	private byte type ;//4-消耗 5-退还/超时解散退还、游戏未开始解散退还
	private Date createTime ;
	private String remark;
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
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public int getDiamond()
	{
		return diamond;
	}
	public void setDiamond(int diamond)
	{
		this.diamond = diamond;
	}
	public byte getType()
	{
		return type;
	}
	public void setType(byte type)
	{
		this.type = type;
	}
	public String getRemark()
	{
		return remark;
	}
	public void setRemark(String remark)
	{
		this.remark = remark;
	}
	public int getRoomNum() {
		return roomNum;
	}
	public void setRoomNum(int roomNum) {
		this.roomNum = roomNum;
	}
}
