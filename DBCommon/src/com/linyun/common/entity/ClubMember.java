package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Date;

import com.linyun.common.taurus.club.eum.ClubPosition;

public class ClubMember implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private int id ;
	private int clubId ; //俱乐部Id
	private int userId ; //俱乐部成员Id
	private int diamondLimit;//俱乐部成员可以消耗的钻石的总数量
	private int costDiamond; //玩家已消耗的俱乐部钻石数量
	private int scoreLimit; //俱乐部群主给成员设置的积分上限
	private int costScore;  //群成员已消耗的积分
	private int currentScore; // 群成员当前的积分牌
	private int coinLimit;//群主给群成员分配的金币限额
	private byte position; //在俱乐部的地位  0--创建者  1--管理员/预留 2--普通成员
	private Date createTime ;
	
	public ClubMember(int clubId, int userId)
	{
		this.clubId = clubId;
		this.userId = userId;
		this.diamondLimit = 0;
		this.costDiamond = 0;
		this.scoreLimit = 0;
		this.costScore = 0;
		this.currentScore=0;
		this.coinLimit =0;
		this.position = ClubPosition.POSITION_MEMBER.value;
	}
	
	public ClubMember()
	{
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
	public int getDiamondLimit() {
		return diamondLimit;
	}
	public void setDiamondLimit(int diamondLimit) {
		this.diamondLimit = diamondLimit;
	}
	public int getCostDiamond() {
		return costDiamond;
	}
	public void setCostDiamond(int costDiamond) {
		this.costDiamond = costDiamond;
	}
	public byte getPosition() {
		return position;
	}
	public void setPosition(byte position) {
		this.position = position;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public int getScoreLimit() {
		return scoreLimit;
	}

	public void setScoreLimit(int scoreLimit) {
		this.scoreLimit = scoreLimit;
	}

	public int getCostScore() {
		return costScore;
	}

	public void setCostScore(int costScore) {
		this.costScore = costScore;
	}

	public int getCurrentScore() {
		return currentScore;
	}

	public void setCurrentScore(int currentScore) {
		this.currentScore = currentScore;
	}

	public int getCoinLimit() {
		return coinLimit;
	}

	public void setCoinLimit(int coinLimit) {
		this.coinLimit = coinLimit;
	} 
	
	
}
