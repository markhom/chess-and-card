package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 百人牛牛游戏中玩家的帐变记录实体类
*  @Author walker
*  @Since 2018年5月31日
**/

public class GameAccountLog implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private int roomType;
	private int round;
	private int userId;
	private int oldMoney;
	private int newMoney;
	private int betCoin;
	private int rewardMoney;
	private Timestamp createTime;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getRoomType() {
		return roomType;
	}
	public void setRoomType(int roomType) {
		this.roomType = roomType;
	}
	public int getRound() {
		return round;
	}
	public void setRound(int round) {
		this.round = round;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getOldMoney() {
		return oldMoney;
	}
	public void setOldMoney(int oldMoney) {
		this.oldMoney = oldMoney;
	}
	public int getNewMoney() {
		return newMoney;
	}
	public void setNewMoney(int newMoney) {
		this.newMoney = newMoney;
	}
	public int getBetCoin() {
		return betCoin;
	}
	public void setBetCoin(int betCoin) {
		this.betCoin = betCoin;
	}
	public int getRewardMoney() {
		return rewardMoney;
	}
	public void setRewardMoney(int rewardMoney) {
		this.rewardMoney = rewardMoney;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

}
