package com.linyun.middle.common.taurus.player;

import com.linyun.bottom.common.Player;

/**
 * @Author walker
 * @Since 2018年5月23日
 **/

public class HundredsTaurusPlayer extends Player {

	public HundredsTaurusPlayer(String tableId, String userId)
	{
		super(tableId, userId);
		// TODO Auto-generated constructor stub
	}

	private int bet_coin; // 玩家的下注总金额

	private int bet_sky; // 天位置下注

	private int bet_earth; // 地位置下注

	private int bet_people; // 人位置下注

	private int score; // 当前局的得分

	
	public void clear()
	{
		this.bet_coin = 0;
		this.bet_sky =0;
		this.bet_earth=0;
		this.bet_people =0;
		this.score = 0;
		
	}
	
	public int getBet_coin() {
		return bet_coin;
	}

	public void setBet_coin(int bet_coin) {
		this.bet_coin += bet_coin;
	}

	public int getBet_sky() {
		return bet_sky;
	}

	public void setBet_sky(int bet_sky) {
		this.bet_sky += bet_sky;
	}

	public int getBet_earth() {
		return bet_earth;
	}

	public void setBet_earth(int bet_earth) {
		this.bet_earth += bet_earth;
	}

	
	public int getBet_people() {
		return bet_people;
	}

	public void setBet_people(int bet_people) {
		this.bet_people += bet_people;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

}
