package com.linyun.club.taurus.utils;

import java.util.Random;

import com.linyun.middle.common.taurus.server.ActionAware;

public class ClubIdUtil {
	
	public static final ActionAware aAction = new ActionAware();
	//生成唯一8位数的clubId
	public static int generatClubId()
	{   
		Random random = new Random();
		int clubId = 20000000 + random.nextInt(80000000);
		while(aAction.clubAction().selectPrivateClub(clubId) != null)
		{
			clubId = 20000000 + random.nextInt(80000000);
		}
		return clubId ;
	}

}
