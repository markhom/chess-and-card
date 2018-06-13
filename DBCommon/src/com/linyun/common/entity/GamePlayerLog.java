package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

/**
*  @Author walker
*  @Since 2018年4月13日
**/

public class GamePlayerLog implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int id;
	private int clubId;
	private String clubName;
	private int roomNum;
	private int playerId;
	private int score;
	private int result;//输赢结果
	private int buyScore;//在房间内买入的历史累计积分
	private String headImgUrl;
	private String nickName;
	private Timestamp updateTime;
	private ClubRoomLog clubRoomLog;
	
	
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
	
	public int getRoomNum() {
		return roomNum;
	}
	public void setRoomNum(int roomNum) {
		this.roomNum = roomNum;
	}
	public int getPlayerId() {
		return playerId;
	}
	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public int getBuyScore() {
		return buyScore;
	}
	public void setBuyScore(int buyScore) {
		this.buyScore = buyScore;
	}
	
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	public String getHeadImgUrl() {
		return headImgUrl;
	}
	public void setHeadImgUrl(String headImgUrl) {
		this.headImgUrl = headImgUrl;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public Timestamp getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
	public ClubRoomLog getClubRoomLog() {
		return clubRoomLog;
	}
	public void setClubRoomLog(ClubRoomLog clubRoomLog) {
		this.clubRoomLog = clubRoomLog;
	}
	
	

}
