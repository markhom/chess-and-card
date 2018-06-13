package com.linyun.middle.common.taurus.club;

import java.util.List;

import com.linyun.middle.common.taurus.server.ActionAware;

public enum ClubConfig
{
	INSTANCE;
	
	private Config instance = null;
	private ClubConfig()
	{
		instance = new Config();
	}
	
	public Config getConfig()
	{
		return instance;
	}
	
	private Object readResolve()
	{
		return instance;
	}
	
	public static final ActionAware aAction = new ActionAware();
	
	public static class Config
	{
		private int pubClubUserCostLimit;//公共俱乐部每个玩家可消耗房卡的额度
		private int pubClubCreateRoomCostLimit;//公共俱乐部每次开房消耗的额度	
		private int priClubMaxTableNum;//私人俱乐部最大开桌数
		private int userCanJoinPriClubNum;//玩家可加入私人俱乐部数量
		private int priClubPeopleMaxNum;//私人俱乐部人数上限
		private int priClubCreateRoomCostLimit;//私人俱乐部每次开房消耗的额度
		private int createClubScoreRateInitialValue;//创建俱乐部时买入底分倍率的初始值
		
		public void getInit()
		{
			List<Integer> countList = aAction.clubConfigAction().getValueFromConfig();
			setConfig(countList.get(0),countList.get(1),countList.get(2),countList.get(3),countList.get(4),countList.get(5),countList.get(6));
		}
		
		public void setConfig(int pubClubUserCostLimit, int pubClubCreateRoomCostLimit, int priClubMaxTableNum, int userCanJoinPriClubNum, int priClubPeopleMaxNum, int priClubCreateRoomCostLimit,int createClubScoreRateInitialValue)
		{
			synchronized (INSTANCE)
			{
				this.pubClubUserCostLimit = pubClubUserCostLimit;
				this.pubClubCreateRoomCostLimit = pubClubCreateRoomCostLimit;
				this.priClubMaxTableNum = priClubMaxTableNum;
				this.userCanJoinPriClubNum = userCanJoinPriClubNum;
				this.priClubPeopleMaxNum = priClubPeopleMaxNum;
				this.priClubCreateRoomCostLimit = priClubCreateRoomCostLimit;
				this.createClubScoreRateInitialValue=createClubScoreRateInitialValue;
			}
		}
		
		public int getPubClubCreateRoomCostLimit()
		{
			synchronized (INSTANCE)
			{
				return pubClubCreateRoomCostLimit;
			}
		}
		public void setPubClubCreateRoomCostLimit(int pubClubCreateRoomCostLimit)
		{
			synchronized (INSTANCE)
			{
				this.pubClubCreateRoomCostLimit = pubClubCreateRoomCostLimit;
			}
		}
		
		public int getPriClubMaxTableNum()
		{
			synchronized (INSTANCE)
			{
				return priClubMaxTableNum;
			}
		}
		public void setPriClubMaxTableNum(int priClubMaxTableNum)
		{
			synchronized (INSTANCE)
			{
				this.priClubMaxTableNum = priClubMaxTableNum;
			}
		}
		
		public int getUserCanJoinPriClubNum()
		{
			synchronized (INSTANCE)
			{
				return userCanJoinPriClubNum;
			}
		}
		public void setUserCanJoinPriClubNum(int userCanJoinPriClubNum)
		{
			synchronized (INSTANCE)
			{
				this.userCanJoinPriClubNum = userCanJoinPriClubNum;
			}
		}
		
		public int getPriClubPeopleMaxNum()
		{
			synchronized (INSTANCE)
			{
				return priClubPeopleMaxNum;
			}
		}
		public void setPriClubPeopleMaxNum(int priClubPeopleMaxNum)
		{
			synchronized (INSTANCE)
			{
				this.priClubPeopleMaxNum = priClubPeopleMaxNum;
			}
		}
		
		public int getPriClubCreateRoomCostLimit()
		{
			synchronized (INSTANCE)
			{
				return priClubCreateRoomCostLimit;
			}
		}
		public void setPriClubCreateRoomCostLimit(int priClubCreateRoomCostLimit)
		{
			synchronized (INSTANCE)
			{
				this.priClubCreateRoomCostLimit = priClubCreateRoomCostLimit;
			}
		}

		public int getPubClubUserCostLimit()
		{
			synchronized (INSTANCE)
			{
				return pubClubUserCostLimit;
			}
		}

		public void setPubClubUserCostLimit(int pubClubUserCostLimit)
		{
			synchronized (INSTANCE)
			{
				this.pubClubUserCostLimit = pubClubUserCostLimit;
			}
		}

		public int getCreateClubScoreRateInitialValue() 
		{
			synchronized(INSTANCE)
			{
				return createClubScoreRateInitialValue;
			}
			
		}

		public void setCreateClubScoreRateInitialValue(int createClubScoreRateInitialValue) 
		{
			synchronized(INSTANCE)
			{
				this.createClubScoreRateInitialValue = createClubScoreRateInitialValue;
			}
		}
		
		
	}
}
