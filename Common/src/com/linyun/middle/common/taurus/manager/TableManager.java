package com.linyun.middle.common.taurus.manager;

import java.util.concurrent.ConcurrentLinkedDeque;

import com.linyun.middle.common.taurus.table.TaurusTable;

public class TableManager 
{	  
	private static ConcurrentLinkedDeque<TaurusTable> tableList = new ConcurrentLinkedDeque<TaurusTable>();
	  
	public static void addTaurusTable(TaurusTable table)
	{
		if (table == null)
		{
			return;
		}
		
		tableList.add(table);
	}
	  
  	public static TaurusTable getTaurusTable()
  	{    
  		if (tableList.isEmpty())
		{
			return null;
		}
		return tableList.removeFirst();
  	}
}
