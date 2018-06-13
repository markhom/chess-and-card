package com.linyun.common.entity;

import java.io.Serializable;

public class PrivateRoom  implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private int id ;
	private int roomNum ; // 房间号 
	private byte roomStatus ; // 房间状态
	private int roomOwnerId; //房主Id
	private int gameTime; //俱乐部开房游戏时长
	private int joinGameScoreLimit; //该房间加入游戏的最低积分
	private byte upBankerMode ; //上庄模式  默认是1--牛牛上庄  后面几个在此基础上累加
	private byte roundNum ; //本房间的局数  10or20 默认是10
	private int seatNum ; //座位数量  默认是6
	private int sitDownNum ;//坐下的人数
	private int baseScore ; //游戏底分   通比牛牛中不需要此项
	private int allCompareBaseScore ; //通比牛牛的底分选项
	private byte payMode ; // 支付方式
	private byte timesMode ; //翻倍规则
	private byte allFace ; // 五花（5倍）
	private byte bomb ; //炸弹妞 （6倍）
	private byte allSmall ; // 五小牛（8倍）
	private byte playerInjection ; //闲家推注
	private byte noEnter ; // 游戏开始后禁止进入房间  
	private byte noShuffle ; // 禁止搓牌  
	private byte mostRobBanker ; // 最大抢庄  明牌抢庄模式特有
	private int upBankerScore ; // 上庄分数  固定庄家模式特有
	private int clubId ;//创建房间的位置，0-非俱乐部创建房间 ，在俱乐部中创建房间，该值是俱乐部Id
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getRoomNum() {
		return roomNum;
	}
	public void setRoomNum(int roomNum) {
		this.roomNum = roomNum;
	}
	public byte getUpBankerMode() {
		return upBankerMode;
	}
	public void setUpBankerMode(byte upBankerMode) {
		this.upBankerMode = upBankerMode;
	}
	public byte getRoundNum() {
		return roundNum;
	}
	public void setRoundNum(byte roundNum) {
		this.roundNum = roundNum;
	}
	public int getSeatNum() {
		return seatNum;
	}
	public void setSeatNum(int seatNum) {
		this.seatNum = seatNum;
	}
	public int getSitDownNum() {
		return sitDownNum;
	}
	public void setSitDownNum(int sitDownNum) {
		this.sitDownNum = sitDownNum;
	}
	public int getBaseScore() {
		return baseScore;
	}
	public void setBaseScore(int baseScore) {
		this.baseScore = baseScore;
	}
	public int getAllCompareBaseScore() {
		return allCompareBaseScore;
	}
	public void setAllCompareBaseScore(int allCompareBaseScore) {
		this.allCompareBaseScore = allCompareBaseScore;
	}
	public byte getPayMode() {
		return payMode;
	}
	public void setPayMode(byte payMode) {
		this.payMode = payMode;
	}
	public byte getTimesMode() {
		return timesMode;
	}
	public void setTimesMode(byte timesMode) {
		this.timesMode = timesMode;
	}
	public byte getAllFace() {
		return allFace;
	}
	public void setAllFace(byte allFace) {
		this.allFace = allFace;
	}
	public byte getBomb() {
		return bomb;
	}
	public void setBomb(byte bomb) {
		this.bomb = bomb;
	}
	public byte getAllSmall() {
		return allSmall;
	}
	public void setAllSmall(byte allSmall) {
		this.allSmall = allSmall;
	}
	public byte getPlayerInjection() {
		return playerInjection;
	}
	public void setPlayerInjection(byte playerInjection) {
		this.playerInjection = playerInjection;
	}
	public byte getNoEnter() {
		return noEnter;
	}
	public void setNoEnter(byte noEnter) {
		this.noEnter = noEnter;
	}
	public byte getNoShuffle() {
		return noShuffle;
	}
	public void setNoShuffle(byte noShuffle) {
		this.noShuffle = noShuffle;
	}
	public byte getMostRobBanker() {
		return mostRobBanker;
	}
	public void setMostRobBanker(byte mostRobBanker) {
		this.mostRobBanker = mostRobBanker;
	}
	public int getRoomOwnerId()
	{
		return roomOwnerId;
	}
	public void setRoomOwnerId(int roomOwnerId)
	{
		this.roomOwnerId = roomOwnerId;
	}
	public int getUpBankerScore()
	{
		return upBankerScore;
	}
	public void setUpBankerScore(int upBankerScore)
	{
		this.upBankerScore = upBankerScore;
	}
	
	public byte getRoomStatus() {
		return roomStatus;
	}
	public void setRoomStatus(byte roomStatus) {
		this.roomStatus = roomStatus;
	}
	public int getClubId() {
		return clubId;
	}
	public void setClubId(int clubId) {
		this.clubId = clubId;
	}
	public int getGameTime() {
		return gameTime;
	}
	public void setGameTime(int gameTime) {
		this.gameTime = gameTime;
	}
	public int getJoinGameScoreLimit() {
		return joinGameScoreLimit;
	}
	public void setJoinGameScoreLimit(int joinGameScoreLimit) {
		this.joinGameScoreLimit = joinGameScoreLimit;
	}
	
	
	
}
