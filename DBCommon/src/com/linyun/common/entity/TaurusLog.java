package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class TaurusLog implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private int id ;
	private int roomNum ; //房间号
	private int roundTotal ; //设置游戏的总局数 10 or 20 
	private int playedRound ; //已玩的局数
	private byte bankerMode ; //庄家模式  6种玩法
	private byte baseScore ; // 底分  1代表1/2，2代表2/4，4代表4/8 
	private byte allCompareBaseScore ; //  通比牛牛的底分值 
	private int roomOwnerId ; // 房主ID AA支付时没有房主
	private byte payMode ; // 支付方式 0房主支付   1 AA支付
	private String playerId1 ; //坐在位置1上的玩家的ID 
	private int score1 ; // 座位1的玩家的总成绩
	private String headImgUrl1;
	private String nickName1;
	private String playerId2 ;
	private int score2 ;
	private String headImgUrl2;
	private String nickName2;
	private String playerId3 ;
	private int score3 ;
	private String headImgUrl3;
	private String nickName3;
	private String playerId4 ;
	private int score4 ;
	private String headImgUrl4;
	private String nickName4;
	private String playerId5 ;
	private int score5 ;
	private String headImgUrl5;
	private String nickName5;
	private String playerId6 ;
	private int score6 ;
	private String headImgUrl6;
	private String nickName6;
	private String roundIndex1 ; //不同房间 每一局游戏的唯一索引
	private String roundIndex2 ;
	private String roundIndex3 ;
	private String roundIndex4 ;
	private String roundIndex5 ;
	private String roundIndex6 ;
	private String roundIndex7 ;
	private String roundIndex8 ;
	private String roundIndex9 ;
	private String roundIndex10 ;
	private String roundIndex11 ;
	private String roundIndex12 ;
	private String roundIndex13 ;
	private String roundIndex14 ;
	private String roundIndex15 ;
	private String roundIndex16 ;
	private String roundIndex17 ;
	private String roundIndex18 ;
	private String roundIndex19 ;
	private String roundIndex20 ;
	private Timestamp updateTime;
	private Timestamp createTime ;
	private String userId ;  //查询战绩的参数，作为一个临时的对象属性，用来查询玩家的所有战绩
	private int clubId ;//游戏的俱乐部Id

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
	public int getRoundTotal() {
		return roundTotal;
	}
	public void setRoundTotal(int roundTotal) {
		this.roundTotal = roundTotal;
	}
	public int getPlayedRound() {
		return playedRound;
	}
	public void setPlayedRound(int playedRound) {
		this.playedRound = playedRound;
	}
	public byte getBankerMode() {
		return bankerMode;
	}
	public void setBankerMode(byte bankerMode) {
		this.bankerMode = bankerMode;
	}
	public byte getBaseScore() {
		return baseScore;
	}
	public void setBaseScore(byte baseScore) {
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
	public byte getPayMode() {
		return payMode;
	}
	public void setPayMode(byte payMode) {
		this.payMode = payMode;
	}
	public String getPlayerId1() {
		return playerId1;
	}
	public void setPlayerId1(String playerId1) {
		this.playerId1 = playerId1;
	}
	public String getPlayerId2() {
		return playerId2;
	}
	public void setPlayerId2(String playerId2) {
		this.playerId2 = playerId2;
	}
	public String getPlayerId3() {
		return playerId3;
	}
	public void setPlayerId3(String playerId3) {
		this.playerId3 = playerId3;
	}
	public String getPlayerId4() {
		return playerId4;
	}
	public void setPlayerId4(String playerId4) {
		this.playerId4 = playerId4;
	}
	public String getPlayerId5() {
		return playerId5;
	}
	public void setPlayerId5(String playerId5) {
		this.playerId5 = playerId5;
	}
	public String getPlayerId6() {
		return playerId6;
	}
	public void setPlayerId6(String playerId6) {
		this.playerId6 = playerId6;
	}
	public int getScore1() {
		return score1;
	}
	public void setScore1(int score1) {
		this.score1 = score1;
	}
	public int getScore2() {
		return score2;
	}
	public void setScore2(int score2) {
		this.score2 = score2;
	}
	public int getScore3() {
		return score3;
	}
	public void setScore3(int score3) {
		this.score3 = score3;
	}
	public int getScore4() {
		return score4;
	}
	public void setScore4(int score4) {
		this.score4 = score4;
	}
	public int getScore5() {
		return score5;
	}
	public void setScore5(int score5) {
		this.score5 = score5;
	}
	public int getScore6() {
		return score6;
	}
	public void setScore6(int score6) {
		this.score6 = score6;
	}
	public String getRoundIndex1() {
		return roundIndex1;
	}
	public void setRoundIndex1(String roundIndex1) {
		this.roundIndex1 = roundIndex1;
	}
	public String getRoundIndex2() {
		return roundIndex2;
	}
	public void setRoundIndex2(String roundIndex2) {
		this.roundIndex2 = roundIndex2;
	}
	public String getRoundIndex3() {
		return roundIndex3;
	}
	public void setRoundIndex3(String roundIndex3) {
		this.roundIndex3 = roundIndex3;
	}
	public String getRoundIndex4() {
		return roundIndex4;
	}
	public void setRoundIndex4(String roundIndex4) {
		this.roundIndex4 = roundIndex4;
	}
	public String getRoundIndex5() {
		return roundIndex5;
	}
	public void setRoundIndex5(String roundIndex5) {
		this.roundIndex5 = roundIndex5;
	}
	public String getRoundIndex6() {
		return roundIndex6;
	}
	public void setRoundIndex6(String roundIndex6) {
		this.roundIndex6 = roundIndex6;
	}
	public String getRoundIndex7() {
		return roundIndex7;
	}
	public void setRoundIndex7(String roundIndex7) {
		this.roundIndex7 = roundIndex7;
	}
	public String getRoundIndex8() {
		return roundIndex8;
	}
	public void setRoundIndex8(String roundIndex8) {
		this.roundIndex8 = roundIndex8;
	}
	public String getRoundIndex9() {
		return roundIndex9;
	}
	public void setRoundIndex9(String roundIndex9) {
		this.roundIndex9 = roundIndex9;
	}
	public String getRoundIndex10() {
		return roundIndex10;
	}
	public void setRoundIndex10(String roundIndex10) {
		this.roundIndex10 = roundIndex10;
	}
	public String getRoundIndex11() {
		return roundIndex11;
	}
	public void setRoundIndex11(String roundIndex11) {
		this.roundIndex11 = roundIndex11;
	}
	public String getRoundIndex12() {
		return roundIndex12;
	}
	public void setRoundIndex12(String roundIndex12) {
		this.roundIndex12 = roundIndex12;
	}
	public String getRoundIndex13() {
		return roundIndex13;
	}
	public void setRoundIndex13(String roundIndex13) {
		this.roundIndex13 = roundIndex13;
	}
	public String getRoundIndex14() {
		return roundIndex14;
	}
	public void setRoundIndex14(String roundIndex14) {
		this.roundIndex14 = roundIndex14;
	}
	public String getRoundIndex15() {
		return roundIndex15;
	}
	public void setRoundIndex15(String roundIndex15) {
		this.roundIndex15 = roundIndex15;
	}
	public String getRoundIndex16() {
		return roundIndex16;
	}
	public void setRoundIndex16(String roundIndex16) {
		this.roundIndex16 = roundIndex16;
	}
	public String getRoundIndex17() {
		return roundIndex17;
	}
	public void setRoundIndex17(String roundIndex17) {
		this.roundIndex17 = roundIndex17;
	}
	public String getRoundIndex18() {
		return roundIndex18;
	}
	public void setRoundIndex18(String roundIndex18) {
		this.roundIndex18 = roundIndex18;
	}
	public String getRoundIndex19() {
		return roundIndex19;
	}
	public void setRoundIndex19(String roundIndex19) {
		this.roundIndex19 = roundIndex19;
	}
	public String getRoundIndex20() {
		return roundIndex20;
	}
	public void setRoundIndex20(String roundIndex20) {
		this.roundIndex20 = roundIndex20;
	}
	public Timestamp getUpdateTime()
	{
		return updateTime;
	}
	public void setUpdateTime(Timestamp updateTime)
	{
		this.updateTime = updateTime;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public String getHeadImgUrl1() {
		return headImgUrl1;
	}
	public void setHeadImgUrl1(String headImgUrl1) {
		this.headImgUrl1 = headImgUrl1;
	}
	public String getNickName1() {
		return nickName1;
	}
	public void setNickName1(String nickName1) {
		this.nickName1 = nickName1;
	}
	public String getHeadImgUrl2() {
		return headImgUrl2;
	}
	public void setHeadImgUrl2(String headImgUrl2) {
		this.headImgUrl2 = headImgUrl2;
	}
	public String getNickName2() {
		return nickName2;
	}
	public void setNickName2(String nickName2) {
		this.nickName2 = nickName2;
	}
	public String getHeadImgUrl3() {
		return headImgUrl3;
	}
	public void setHeadImgUrl3(String headImgUrl3) {
		this.headImgUrl3 = headImgUrl3;
	}
	public String getNickName3() {
		return nickName3;
	}
	public void setNickName3(String nickName3) {
		this.nickName3 = nickName3;
	}
	public String getHeadImgUrl4() {
		return headImgUrl4;
	}
	public void setHeadImgUrl4(String headImgUrl4) {
		this.headImgUrl4 = headImgUrl4;
	}
	public String getNickName4() {
		return nickName4;
	}
	public void setNickName4(String nickName4) {
		this.nickName4 = nickName4;
	}
	public String getHeadImgUrl5() {
		return headImgUrl5;
	}
	public void setHeadImgUrl5(String headImgUrl5) {
		this.headImgUrl5 = headImgUrl5;
	}
	public String getNickName5() {
		return nickName5;
	}
	public void setNickName5(String nickName5) {
		this.nickName5 = nickName5;
	}
	public String getHeadImgUrl6() {
		return headImgUrl6;
	}
	public void setHeadImgUrl6(String headImgUrl6) {
		this.headImgUrl6 = headImgUrl6;
	}
	public String getNickName6() {
		return nickName6;
	}
	public void setNickName6(String nickName6) {
		this.nickName6 = nickName6;
	}
	public int getClubId() {
		return clubId;
	}
	public void setClubId(int clubId) {
		this.clubId = clubId;
	}
	
	public String getPlayerIdByIndex(int index)
	{
		switch(index)
		{
		 case 1: return playerId1;
		 case 2: return playerId2;
		 case 3: return playerId3;
		 case 4: return playerId4;
		 case 5: return playerId5;
		 case 6: return playerId6;
		 default:return ""; 
		}
	}
	
	public String getNickNameByIndex(int index)
	{
		switch(index)
		{
		 case 1: return nickName1;
		 case 2: return nickName2;
		 case 3: return nickName3;
		 case 4: return nickName4;
		 case 5: return nickName5;
		 case 6: return nickName6;
		 default:return ""; 
		}
	}
	
	public String getFaceUrlByIndex(int index)
	{
		switch(index)
		{
		 case 1: return headImgUrl1;
		 case 2: return headImgUrl2;
		 case 3: return headImgUrl3;
		 case 4: return headImgUrl4;
		 case 5: return headImgUrl5;
		 case 6: return headImgUrl6;
		 default:return ""; 
		}
		
	}
	
	public int getScoreByIndex(int index)
	{
		switch(index)
		{
		 case 1: return score1;
		 case 2: return score2;
		 case 3: return score3;
		 case 4: return score4;
		 case 5: return score5;
		 case 6: return score6;
		 default:return 0; 
		}

	}
}
