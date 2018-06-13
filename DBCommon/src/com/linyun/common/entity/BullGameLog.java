package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 百人牛牛玩家的游戏记录实体类
*  @Author walker
*  @Since 2018年5月31日
**/

public class BullGameLog implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int id;
	private int roomType;
	private int round;
	private int userId;
	private int player1_bet; //天 位置下注额
	private int player2_bet; //地位置下注额
	private int player3_bet; //人位置下注额
	private int bet_total; //下注总金额
	private int reward_total; //总派奖金额
	private int isBanker; //是否为庄家  0否1是
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
	public int getPlayer1_bet() {
		return player1_bet;
	}
	public void setPlayer1_bet(int player1_bet) {
		this.player1_bet = player1_bet;
	}
	public int getPlayer2_bet() {
		return player2_bet;
	}
	public void setPlayer2_bet(int player2_bet) {
		this.player2_bet = player2_bet;
	}
	public int getPlayer3_bet() {
		return player3_bet;
	}
	public void setPlayer3_bet(int player3_bet) {
		this.player3_bet = player3_bet;
	}
	public int getBet_total() {
		return bet_total;
	}
	public void setBet_total(int bet_total) {
		this.bet_total = bet_total;
	}
	public int getReward_total() {
		return reward_total;
	}
	public void setReward_total(int reward_total) {
		this.reward_total = reward_total;
	}
	public int getIsBanker() {
		return isBanker;
	}
	public void setIsBanker(int isBanker) {
		this.isBanker = isBanker;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

    
}
