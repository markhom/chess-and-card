package com.linyun.middle.common.taurus.bean;

public class RoomConfigSpecConfig
{
	/** 特殊牌型-五花 */
	private boolean allFace;
	/** 特殊牌型-炸弹 */
	private boolean bomb;
	/** 特殊牌型-五小牛 */
	private boolean allSmall;
	
	public RoomConfigSpecConfig()
	{
		this.setAllFace(false);
		this.setBomb(false);
		this.setAllSmall(false);
	}

	public boolean isAllFace()
	{
		return allFace;
	}

	public void setAllFace(boolean allFace)
	{
		this.allFace = allFace;
	}

	public boolean isAllSmall()
	{
		return allSmall;
	}

	public void setAllSmall(boolean allSmall)
	{
		this.allSmall = allSmall;
	}

	public boolean isBomb()
	{
		return bomb;
	}

	public void setBomb(boolean bomb)
	{
		this.bomb = bomb;
	}
	
	public void clear()
	{
		this.allSmall = false;
		this.bomb = false;
		this.allFace = false;
	}
}
