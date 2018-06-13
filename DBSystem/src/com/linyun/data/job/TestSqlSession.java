package com.linyun.data.job;

import java.io.IOException;
import com.linyun.bottom.cached.RedisResource;
import com.linyun.common.action.UserAction;
import com.linyun.common.action.impl.UserActionImpl;


public class TestSqlSession 
{	
	public static void main(String[] args) throws IOException
	{
		SqlSessionFactoryUtil.getInstance();
		RedisResource.getInstance();
		
		
		
		
//		TaurusRoundLogAction logAction = new TaurusRoundLogActionImpl();
//		logAction.getHandCardByRoomIndex("NMQBWP4Z6");
		
//		PrivateRoomAction roomAction = new PrivateRoomActionImpl();
//		roomAction.startGame(515739);
//		
//		OnlineTaurusAction onlineTaurusAction = new OnlineTaurusActionImpl();
//		onlineTaurusAction.updateOnlineCount(10);
//		
		UserAction userAction = new UserActionImpl();
//		CommonAction commonAction = new CommonActionImpl();
//		
//		Set<Integer> set = commonAction.getInviteCodeList();
//		int i= 0;
//		for (Integer id:set)
//		{
//			System.out.println(i++ + " \t" + id);
//		}
//		String pattern = "\\d{5}";
//		boolean isMatch1 = Pattern.matches(pattern,"123456");
//		System.out.println("1---" + isMatch1);
//		boolean isMatch2 = Pattern.matches(pattern,"1a345");
//		System.out.println("1---" + isMatch2);
//		
//		UserJoinRoomLogAction joinAction = new UserJoinRoomLogActionImpl();
//		joinAction.delLogsWithRoomNum(982800);
		
//		userAction.updateUserWxInfo("10017", "http://touxiang.qqzhi.com/uploads/2012-11/1111135112148.jpg", "等闲变却");
//		action.updateRoundNum(812369, 10);
		userAction.bindGiftCode("10175", "10010", 8);
	      
	}

}
