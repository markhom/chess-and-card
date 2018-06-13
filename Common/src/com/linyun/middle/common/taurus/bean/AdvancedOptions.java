package com.linyun.middle.common.taurus.bean;

public class AdvancedOptions
{
	/** 游戏开始禁止入内 */
	private boolean noEnter;
	/** 禁止搓牌 */
	private boolean noShuffle;
	
	public AdvancedOptions()
	{
		this.noEnter = false;
		this.noShuffle = false;
	}
	
	public boolean isNoEnter()
	{
		return noEnter;
	}
	public void setNoEnter(boolean noEnter)
	{
		this.noEnter = noEnter;
	}
	
	public boolean isNoShuffle()
	{
		return noShuffle;
	}
	public void setNoShuffle(boolean noShuffle)
	{
		this.noShuffle = noShuffle;
	}
	
	public void clear()
	{
		this.noEnter = false;
		this.noShuffle = false;
	}
	
}
