package com.linyun.bottom.container;

public class GameSeat
{
	/**椅子id,也就是椅子的位置 */
	protected int id;
	/** 椅子上是否可以坐下来 */
	protected boolean isCanSitDown;
	
	public GameSeat(int _id)
	{
		this.id = _id;
		this.isCanSitDown = true;
	}
	public GameSeat(int _id, boolean _isCanSitDown)
	{
		this.id = _id;
		this.isCanSitDown = _isCanSitDown;
	}
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	
	public boolean isCanSitDown()
	{
		return isCanSitDown;
	}
	public void setCanSitDown(boolean isCanSitDown)
	{
		this.isCanSitDown = isCanSitDown;
	}
}
