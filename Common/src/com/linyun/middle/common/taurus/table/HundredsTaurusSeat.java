package com.linyun.middle.common.taurus.table;

import java.io.Serializable;

import com.linyun.common.entity.User;
import com.linyun.middle.common.taurus.player.HundredsTaurusPlayer;

/**
*  @Author walker
*  @Since 2018年5月23日
**/

public class HundredsTaurusSeat implements Serializable
{

	private static final long serialVersionUID = 1L;
	//玩家在桌子上对应的位置
	private int id;
	
	private User user;
	
	private boolean isCanSitDown;
	
	public HundredsTaurusSeat(int id)
	{
		this.id=id;
		this.user = null;
		this.isCanSitDown = true;
	}

	public int getId() 
	{
		return id;
	}

	public void setId(int id) 
	{
		this.id = id;
	}

	public User getUser() 
	{
		return this.user;
	}

	public void setUser(User user) 
	{
		this.user = user;
	}
	
	public void clear()
	{
		this.user = null;
		this.isCanSitDown = true;
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
