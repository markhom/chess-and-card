package com.linyun.club.taurus.manager;

import java.util.LinkedList;

import com.linyun.middle.common.taurus.table.HundredsTaurusTable;

public class GameTableManager 
{
	private static GameTableManager gameManager =  new GameTableManager();
	
	private static LinkedList<HundredsTaurusTable> listTables = new LinkedList<HundredsTaurusTable>();
	
	private GameTableManager()
	{
	}
	
	public static GameTableManager getInstance()
	{
		return gameManager;
	}
	
	public void addTable(HundredsTaurusTable table)
	{
		if (table != null)
		{
			listTables.add(table);
		}
	}
	
	public HundredsTaurusTable getTable(int tableId)
	{
		
		for (int i = 0 ; i < listTables.size(); ++i)
		{
			HundredsTaurusTable table = listTables.get(i); 
			if (table.getTableId()==tableId)
			{
				return table;
			}
		}
		
		return null;
	}
	
	public void removeTable(HundredsTaurusTable table)
	{
		if (table != null)
		{
			if (listTables.indexOf(table) != -1)
			{
				listTables.remove(table);
			}
		}
	}
}
