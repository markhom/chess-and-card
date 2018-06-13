package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class TaurusRoundLog implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String roundIndex ; //局数对应的索引,唯一标识索引
	private int roomNum ;  // 房间号
	private byte round ; //游戏的局数   <= 20
	private String roomOwner ; // 房主的id AA支付时没有房主
	private String bankerId ; // 通比牛牛时没有庄家
	private byte chooseBaseScore;//底分为庄家选择模式时，需要记录庄家选择的底分
	
	private String playerId1 ; // 座位1位置上玩家的id
	private String cards1 ; //座位1的玩家的牌的信息
	private byte cardType1 ; // 座位1位置玩家的牌类型  有牛  无牛  牛几  
	private int baseScore1 ; //座位1玩家一局的押注底分，庄家不押注
	private int getScore1 ; // 座位1的玩家的一局得分 
	private byte isRobBanker1 ; // 1号位置上的玩家是否抢庄(自由抢庄时用到)
	private byte robBankerNum1 ;// 1位置的玩家的抢庄倍率  (明牌抢庄的时候用到1)
	private int getScoreTotal1 ; //玩家累计前面局数的总得分
    
	private String playerId2 ; 
	private String cards2 ; 
	private byte cardType2 ; 
	private int baseScore2 ; 
	private int getScore2 ; 
	private byte isRobBanker2 ; 
	private byte robBankerNum2 ;
	private int getScoreTotal2 ;

	private String playerId3 ; 
	private String cards3 ; 
	private byte cardType3 ; 
	private int baseScore3 ; 
	private int getScore3 ; 
	private byte isRobBanker3 ; 
	private byte robBankerNum3 ;
	private int getScoreTotal3 ;

	private String playerId4 ; 
	private String cards4 ; 
	private byte cardType4 ; 
	private int baseScore4 ; 
	private int getScore4 ; 
	private byte isRobBanker4 ; 
	private byte robBankerNum4 ;
	private int getScoreTotal4 ;

	private String playerId5 ; 
	private String cards5 ; 
	private byte cardType5 ; 
	private int baseScore5 ; 
	private int getScore5 ; 
	private byte isRobBanker5 ; 
	private byte robBankerNum5 ;
	private int getScoreTotal5 ;

	private String playerId6 ; 
	private String cards6 ; 
	private byte cardType6 ; 
	private int baseScore6 ; 
	private int getScore6 ; 
	private byte isRobBanker6 ; 
	private byte robBankerNum6 ;
	private int getScoreTotal6 ;
	
	public int getGetScoreTotal1() {
		return getScoreTotal1;
	}
	public void setGetScoreTotal1(int getScoreTotal1) {
		this.getScoreTotal1 = getScoreTotal1;
	}
	public int getGetScoreTotal2() {
		return getScoreTotal2;
	}
	public void setGetScoreTotal2(int getScoreTotal2) {
		this.getScoreTotal2 = getScoreTotal2;
	}
	public int getGetScoreTotal3() {
		return getScoreTotal3;
	}
	public void setGetScoreTotal3(int getScoreTotal3) {
		this.getScoreTotal3 = getScoreTotal3;
	}
	public int getGetScoreTotal4() {
		return getScoreTotal4;
	}
	public void setGetScoreTotal4(int getScoreTotal4) {
		this.getScoreTotal4 = getScoreTotal4;
	}
	public int getGetScoreTotal5() {
		return getScoreTotal5;
	}
	public void setGetScoreTotal5(int getScoreTotal5) {
		this.getScoreTotal5 = getScoreTotal5;
	}
	public int getGetScoreTotal6() {
		return getScoreTotal6;
	}
	public void setGetScoreTotal6(int getScoreTotal6) {
		this.getScoreTotal6 = getScoreTotal6;
	}

	private Timestamp updateTime;

	public String getRoundIndex() {
		return roundIndex;
	}
	public void setRoundIndex(String roundIndex) {
		this.roundIndex = roundIndex;
	}
	public int getRoomNum() {
		return roomNum;
	}
	public void setRoomNum(int roomNum) {
		this.roomNum = roomNum;
	}
	public byte getRound() {
		return round;
	}
	public void setRound(byte round) {
		this.round = round;
	}
	public String getRoomOwner() {
		return roomOwner;
	}
	public void setRoomOwner(String roomOwner) {
		this.roomOwner = roomOwner;
	}
	public String getBankerId() {
		return bankerId;
	}
	public void setBankerId(String bankerId) {
		this.bankerId = bankerId;
	}
	public String getPlayerId1() {
		return playerId1;
	}
	public void setPlayerId1(String playerId1) {
		this.playerId1 = playerId1;
	}
	public String getCards1() {
		return cards1;
	}
	public void setCards1(String cards1) {
		this.cards1 = cards1;
	}
	public byte getCardType1() {
		return cardType1;
	}
	public void setCardType1(byte cardType1) {
		this.cardType1 = cardType1;
	}
	public int getBaseScore1() {
		return baseScore1;
	}
	public void setBaseScore1(int baseScore1) {
		this.baseScore1 = baseScore1;
	}
	public int getGetScore1() {
		return getScore1;
	}
	public void setGetScore1(int getScore1) {
		this.getScore1 = getScore1;
	}
	public byte getIsRobBanker1() {
		return isRobBanker1;
	}
	public void setIsRobBanker1(byte isRobBanker1) {
		this.isRobBanker1 = isRobBanker1;
	}
	public byte getRobBankerNum1() {
		return robBankerNum1;
	}
	public void setRobBankerNum1(byte robBankerNum1) {
		this.robBankerNum1 = robBankerNum1;
	}
	public String getPlayerId2() {
		return playerId2;
	}
	public void setPlayerId2(String playerId2) {
		this.playerId2 = playerId2;
	}
	public String getCards2() {
		return cards2;
	}
	public void setCards2(String cards2) {
		this.cards2 = cards2;
	}
	public byte getCardType2() {
		return cardType2;
	}
	public void setCardType2(byte cardType2) {
		this.cardType2 = cardType2;
	}
	public int getBaseScore2() {
		return baseScore2;
	}
	public void setBaseScore2(int baseScore2) {
		this.baseScore2 = baseScore2;
	}
	public int getGetScore2() {
		return getScore2;
	}
	public void setGetScore2(int getScore2) {
		this.getScore2 = getScore2;
	}
	public byte getIsRobBanker2() {
		return isRobBanker2;
	}
	public void setIsRobBanker2(byte isRobBanker2) {
		this.isRobBanker2 = isRobBanker2;
	}
	public byte getRobBankerNum2() {
		return robBankerNum2;
	}
	public void setRobBankerNum2(byte robBankerNum2) {
		this.robBankerNum2 = robBankerNum2;
	}
	public String getPlayerId3() {
		return playerId3;
	}
	public void setPlayerId3(String playerId3) {
		this.playerId3 = playerId3;
	}
	public String getCards3() {
		return cards3;
	}
	public void setCards3(String cards3) {
		this.cards3 = cards3;
	}
	public byte getCardType3() {
		return cardType3;
	}
	public void setCardType3(byte cardType3) {
		this.cardType3 = cardType3;
	}
	public int getBaseScore3() {
		return baseScore3;
	}
	public void setBaseScore3(int baseScore3) {
		this.baseScore3 = baseScore3;
	}
	public int getGetScore3() {
		return getScore3;
	}
	public void setGetScore3(int getScore3) {
		this.getScore3 = getScore3;
	}
	public byte getIsRobBanker3() {
		return isRobBanker3;
	}
	public void setIsRobBanker3(byte isRobBanker3) {
		this.isRobBanker3 = isRobBanker3;
	}
	public byte getRobBankerNum3() {
		return robBankerNum3;
	}
	public void setRobBankerNum3(byte robBankerNum3) {
		this.robBankerNum3 = robBankerNum3;
	}
	public String getPlayerId4() {
		return playerId4;
	}
	public void setPlayerId4(String playerId4) {
		this.playerId4 = playerId4;
	}
	public String getCards4() {
		return cards4;
	}
	public void setCards4(String cards4) {
		this.cards4 = cards4;
	}
	public byte getCardType4() {
		return cardType4;
	}
	public void setCardType4(byte cardType4) {
		this.cardType4 = cardType4;
	}
	public int getBaseScore4() {
		return baseScore4;
	}
	public void setBaseScore4(int baseScore4) {
		this.baseScore4 = baseScore4;
	}
	public int getGetScore4() {
		return getScore4;
	}
	public void setGetScore4(int getScore4) {
		this.getScore4 = getScore4;
	}
	public byte getIsRobBanker4() {
		return isRobBanker4;
	}
	public void setIsRobBanker4(byte isRobBanker4) {
		this.isRobBanker4 = isRobBanker4;
	}
	public byte getRobBankerNum4() {
		return robBankerNum4;
	}
	public void setRobBankerNum4(byte robBankerNum4) {
		this.robBankerNum4 = robBankerNum4;
	}
	public String getPlayerId5() {
		return playerId5;
	}
	public void setPlayerId5(String playerId5) {
		this.playerId5 = playerId5;
	}
	public String getCards5() {
		return cards5;
	}
	public void setCards5(String cards5) {
		this.cards5 = cards5;
	}
	public byte getCardType5() {
		return cardType5;
	}
	public void setCardType5(byte cardType5) {
		this.cardType5 = cardType5;
	}
	public int getBaseScore5() {
		return baseScore5;
	}
	public void setBaseScore5(int baseScore5) {
		this.baseScore5 = baseScore5;
	}
	public int getGetScore5() {
		return getScore5;
	}
	public void setGetScore5(int getScore5) {
		this.getScore5 = getScore5;
	}
	public byte getIsRobBanker5() {
		return isRobBanker5;
	}
	public void setIsRobBanker5(byte isRobBanker5) {
		this.isRobBanker5 = isRobBanker5;
	}
	public byte getRobBankerNum5() {
		return robBankerNum5;
	}
	public void setRobBankerNum5(byte robBankerNum5) {
		this.robBankerNum5 = robBankerNum5;
	}
	public String getPlayerId6() {
		return playerId6;
	}
	public void setPlayerId6(String playerId6) {
		this.playerId6 = playerId6;
	}
	public String getCards6() {
		return cards6;
	}
	public void setCards6(String cards6) {
		this.cards6 = cards6;
	}
	public byte getCardType6() {
		return cardType6;
	}
	public void setCardType6(byte cardType6) {
		this.cardType6 = cardType6;
	}
	public int getBaseScore6() {
		return baseScore6;
	}
	public void setBaseScore6(int baseScore6) {
		this.baseScore6 = baseScore6;
	}
	public int getGetScore6() {
		return getScore6;
	}
	public void setGetScore6(int getScore6) {
		this.getScore6 = getScore6;
	}
	public byte getIsRobBanker6() {
		return isRobBanker6;
	}
	public void setIsRobBanker6(byte isRobBanker6) {
		this.isRobBanker6 = isRobBanker6;
	}
	public byte getRobBankerNum6() {
		return robBankerNum6;
	}
	public void setRobBankerNum6(byte robBankerNum6) {
		this.robBankerNum6 = robBankerNum6;
	}
	public Timestamp getUpdateTime()
	{
		return updateTime;
	}
	public void setUpdateTime(Timestamp updateTime)
	{
		this.updateTime = updateTime;
	}
	public byte getChooseBaseScore()
	{
		return chooseBaseScore;
	}
	public void setChooseBaseScore(byte chooseBaseScore)
	{
		this.chooseBaseScore = chooseBaseScore;
	}
	
	
	//确定返回玩家的id
		public String getGetPlayerIdx(int x)
		{
			switch (x)
			{
			case 1:
				return playerId1 ;
			case 2:
				return playerId2 ;
			case 3:
				return playerId3 ;
			case 4:
				return playerId4 ;
			case 5:
				return playerId5 ;
			case 6:
				return playerId6 ;
			default:
				return null;
			}
			
		}
		
		//自由抢庄模式玩家是否抢庄
	    public byte getIsRobBankerx(int x)
	    {
			switch (x)
			{
			case 1:
				return isRobBanker1 ;
			case 2:
				return isRobBanker2 ;
			case 3:
				return isRobBanker3 ;
			case 4:
				return isRobBanker4 ;
			case 5:
				return isRobBanker5 ;
			case 6:
				return isRobBanker6 ;
			default:
				return -1;
			}
	    }
	   
	    //明牌抢庄模式，玩家的抢庄的倍率
	    public byte getRobBankerNumx(int x)
	    {
			switch (x)
			{
			case 1:
				return robBankerNum1 ;
			case 2:
				return robBankerNum2 ;
			case 3:
				return robBankerNum3 ;
			case 4:
				return robBankerNum4 ;
			case 5:
				return robBankerNum5 ;
			case 6:
				return robBankerNum6 ;
			default:
				return -1;
			}
	    }

	    //玩家一局得失分
	    public int getBaseScorex(int x)
	    {
			switch (x)
			{
			case 1:
				return baseScore1 ;
			case 2:
				return baseScore2 ;
			case 3:
				return baseScore3 ;
			case 4:
				return baseScore4 ;
			case 5:
				return baseScore5 ;
			case 6:
				return baseScore6 ;
			default:
				return 0;
			}
	    }
	    
	    //玩家的手牌类型
	    public byte getCardTypex(int x)
	    {
			switch (x)
			{
			case 1:
				return cardType1 ;
			case 2:
				return cardType2 ;
			case 3:
				return cardType3 ;
			case 4:
				return cardType4 ;
			case 5:
				return cardType5 ;
			case 6:
				return cardType6 ;
			default:
				return 0;
			}
	    }
	    
	    //手牌信息
	    public String getCardsx(int x)
	    {
			switch (x)
			{
			case 1:
				return cards1 ;
			case 2:
				return cards2 ;
			case 3:
				return cards3 ;
			case 4:
				return cards4 ;
			case 5:
				return cards5 ;
			case 6:
				return cards6 ;
			default:
				return null;
			}
	    }
	
	    //拉取每个玩家前几局的累加总得分
	    public int getGetScoreTotalx(int x)
	    {
	    	switch (x)
			{
			case 1:
				return getScoreTotal1 ;
			case 2:
				return getScoreTotal2 ;
			case 3:
				return getScoreTotal3 ;
			case 4:
				return getScoreTotal4 ;
			case 5:
				return getScoreTotal5 ;
			case 6:
				return getScoreTotal6 ;
			default:
				return 0;
			}
	    	
	    }
	    //每个玩家当前局的得分
	    public int getGetScorex(int x)
	    {
	    	switch (x)
			{
			case 1:
				return getScore1 ;
			case 2:
				return getScore2 ;
			case 3:
				return getScore3 ;
			case 4:
				return getScore4 ;
			case 5:
				return getScore5 ;
			case 6:
				return getScore6 ;
			default:
				return 0;
			}
	    }
	
	
}
