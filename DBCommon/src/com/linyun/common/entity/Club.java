package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Date;

public class Club implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private int id ;
	private int clubId ; //俱乐部Id
    private String clubName;//俱乐部名称
    private byte clubType ; // 0--私人俱乐部  1--公共俱乐部
    private int diamondPercent;//扣除群主钻石的百分比
    private int expandRate;//小盲伸缩倍率
    private int scoreRate; //最低买入底分倍率
    private int scorePool; //俱乐部群主的积分池 初始化为十亿
    private int coinPool; //俱乐部群主的金币池 初始化为0
    private String iconUrl ;//俱乐部图标的url
    private int creatorId ;//创建俱乐部的玩家的userId
    private String city ; //创建的俱乐部所属城市
    private int peopleCount;//加入俱乐部的总人数
    private Date createTime; //俱乐部创建时间
    private Date modifyTime ; 
	private String clubIntroduce ; //俱乐部介绍
	
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
	public String getClubName() {
		return clubName;
	}
	public void setClubName(String clubName) {
		this.clubName = clubName;
	}
	public byte getClubType() {
		return clubType;
	}
	public void setClubType(byte clubType) {
		this.clubType = clubType;
	}
	public String getIconUrl() {
		return iconUrl;
	}
	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}
	public int getCreatorId() {
		return creatorId;
	}
	public void setCreatorId(int creatorId) {
		this.creatorId = creatorId;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public int getPeopleCount() {
		return peopleCount;
	}
	public void setPeopleCount(int peopleCount) {
		this.peopleCount = peopleCount;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	public String getClubIntroduce() {
		return clubIntroduce;
	}
	public void setClubIntroduce(String clubIntroduce) {
		this.clubIntroduce = clubIntroduce;
	}
	public int getDiamondPercent() {
		return diamondPercent;
	}
	public void setDiamondPercent(int diamondPercent) {
		this.diamondPercent = diamondPercent;
	}
	public int getScorePool() {
		return scorePool;
	}
	public void setScorePool(int scorePool) {
		this.scorePool = scorePool;
	}
	public int getExpandRate() {
		return expandRate;
	}
	public void setExpandRate(int expandRate) {
		this.expandRate = expandRate;
	}
	public int getScoreRate() {
		return scoreRate;
	}
	public void setScoreRate(int scoreRate) {
		this.scoreRate = scoreRate;
	}
	public int getCoinPool() {
		return coinPool;
	}
	public void setCoinPool(int coinPool) {
		this.coinPool = coinPool;
	}
	
	
}
