package com.linyun.middle.common.taurus.club;

import com.linyun.common.entity.ClubMember;

public class TaurusClubMember
{
	private final ClubMember member; 
	private boolean isOnline;//是否在线
	private Object object_online_lock = new Object(); 
	
	public TaurusClubMember(ClubMember _member)
	{
		member = _member;
		isOnline = false;
	}
	
	public TaurusClubMember(ClubMember _member, boolean _isOnline)
	{
		member = _member;
		isOnline = _isOnline;
	}
	
	public int getUserId() {
		return member.getUserId();
	}

	public int getDiamondLimit() {
		return member.getDiamondLimit();
	}
	public void setDiamondLimit(int diamondLimit) {
		this.member.setDiamondLimit(diamondLimit);
	}
	public byte getPosition() 
	{
		return member.getPosition();
	}
	public void setPosition(byte position) {
		this.member.setPosition(position);
	}
	
	public synchronized int getCostDiamond() {
		return member.getCostDiamond();
	}
	public synchronized void setCostDiamond(int costDiamond) {
		this.member.setCostDiamond(costDiamond);
	}
    
	public void setScoreLimit(int scoreLimit)
	{
		this.member.setScoreLimit(scoreLimit);
	}
	public int getScoreLimit()
	{
		return this.member.getScoreLimit();
	}
	
	public void setCurrentScore(int currentScore)
	{
		this.member.setCurrentScore(currentScore);
	}
	
	public int getCurrentScore()
	{
		return this.member.getCurrentScore();
	}
	
	public void setCoinLimit(int coinLimit)
	{
		this.member.setCoinLimit(coinLimit);
	}
	public int getCoinLimit()
	{
		return this.member.getCoinLimit();
	}
	
	public ClubMember getMember() {
		return member;
	}

	public boolean isOnline()
	{
		synchronized (object_online_lock)
		{
			return isOnline;
		}
	}
	public void online()
	{
		synchronized (object_online_lock)
		{
			this.isOnline = true;
		}
	}
	public void disConnect()
	{
		synchronized (object_online_lock)
		{
			this.isOnline = false;
		}
	}

}
