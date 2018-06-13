package com.linyun.bottom.container;

public abstract class GameTable
{
	/** 表示桌子是否可以加入，默认为true */
	protected boolean isCanEnter;
	/** 桌子Id，也就是桌号 */
	protected String tableId;
	/** 桌子的座位总数 */
	protected int seatTotal;
	/** 桌子上的实际玩家数量 */
	protected int realPlayer;
	
	public GameTable(String _tableId, int _seatTotal)
	{
		isCanEnter = true;
		tableId = _tableId;
		seatTotal = _seatTotal;
		realPlayer = 0;
	}
	
	public boolean isCanEnter()
	{
		return isCanEnter;
	}
	public void setCanEnter(boolean isCanEnter)
	{
		this.isCanEnter = isCanEnter;
	}
	
	public String getTableId()
	{
		return tableId;
	}
	public void setTableId(String tableId)
	{
		this.tableId = tableId;
	}
	
	public int getSeatTotal()
	{
		return seatTotal;
	}
	public void setSeatTotal(int seatTotal)
	{
		this.seatTotal = seatTotal;
	}
	
	public int getRealPlayer()
	{
		return realPlayer;
	}
	
	/** 重置桌子里面的数据 */
	abstract public void reset();
	
	
	
}
