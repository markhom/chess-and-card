package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
*  @Author walker
*  @Since 2018年4月12日
**/

public class ClubRoomLog implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private int id;
	private int clubId;
	private int roomNum;
	private int playedRound; //玩的总局数
	private int gameTime;  //游戏时长
	private int scoreLimit; //入场最低积分
	private byte bankerMode; //庄家模式  6种玩法
	private int baseScore;  // 底分  1代表1/2，2代表2/4，4代表4/8 
	private byte allCompareBaseScore;//  通比牛牛的底分值 
	private int roomOwnerId; // 房主ID 
	private Timestamp createTime;
	private Timestamp updateTime;
	
	
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
	public int getRoomNum() {
		return roomNum;
	}
	public void setRoomNum(int roomNum) {
		this.roomNum = roomNum;
	}
	public int getPlayedRound() {
		return playedRound;
	}
	public void setPlayedRound(int playedRound) {
		this.playedRound = playedRound;
	}
	public int getGameTime() {
		return gameTime;
	}
	public void setGameTime(int gameTime) {
		this.gameTime = gameTime;
	}
	public byte getBankerMode() {
		return bankerMode;
	}
	public void setBankerMode(byte bankerMode) {
		this.bankerMode = bankerMode;
	}
	public int getBaseScore() {
		return baseScore;
	}
	public void setBaseScore(int baseScore) {
		this.baseScore = baseScore;
	}
	public byte getAllCompareBaseScore() {
		return allCompareBaseScore;
	}
	public void setAllCompareBaseScore(byte allCompareBaseScore) {
		this.allCompareBaseScore = allCompareBaseScore;
	}
	public int getRoomOwnerId() {
		return roomOwnerId;
	}
	public void setRoomOwnerId(int roomOwnerId) {
		this.roomOwnerId = roomOwnerId;
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
	public int getScoreLimit() {
		return scoreLimit;
	}
	public void setScoreLimit(int scoreLimit) {
		this.scoreLimit = scoreLimit;
	}
	
	
}
