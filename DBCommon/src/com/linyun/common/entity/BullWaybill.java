package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

/**
*  @Author walker
*  @Since 2018年5月26日
**/

public class BullWaybill implements Serializable
{

	private static final long serialVersionUID = 1L;
	
	private int id;
	private byte roomType;
	private int round;
	private int banker_result;
	private String banker_cards;
	private int player1_result;
	private String player1_cards;
	private int player2_result;
	private String player2_cards;
	private int player3_result;
	private String player3_cards;
	private Timestamp updateTime;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public byte getRoomType() {
		return roomType;
	}
	public void setRoomType(byte roomType) {
		this.roomType = roomType;
	}
	public int getRound() {
		return round;
	}
	public void setRound(int round) {
		this.round = round;
	}
	public int getBanker_result() {
		return banker_result;
	}
	public void setBanker_result(int banker_result) {
		this.banker_result = banker_result;
	}
	public String getBanker_cards() {
		return banker_cards;
	}
	public void setBanker_cards(String banker_cards) {
		this.banker_cards = banker_cards;
	}
	public int getPlayer1_result() {
		return player1_result;
	}
	public void setPlayer1_result(int player1_result) {
		this.player1_result = player1_result;
	}
	public String getPlayer1_cards() {
		return player1_cards;
	}
	public void setPlayer1_cards(String player1_cards) {
		this.player1_cards = player1_cards;
	}
	public int getPlayer2_result() {
		return player2_result;
	}
	public void setPlayer2_result(int player2_result) {
		this.player2_result = player2_result;
	}
	public String getPlayer2_cards() {
		return player2_cards;
	}
	public void setPlayer2_cards(String player2_cards) {
		this.player2_cards = player2_cards;
	}
	public int getPlayer3_result() {
		return player3_result;
	}
	public void setPlayer3_result(int player3_result) {
		this.player3_result = player3_result;
	}
	public String getPlayer3_cards() {
		return player3_cards;
	}
	public void setPlayer3_cards(String player3_cards) {
		this.player3_cards = player3_cards;
	}
	public Timestamp getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
	
	
}
