package com.linyun.common.utils;

public class ConstantsUtils {
	  
	public static final String SERVER_PATH = System.getProperty("user.dir");
	
	//控制是否启动
	public static final int STATUS_ON = 1 ;
	public static final int STATUS_OFF = 0 ;
	
	
	public static final String NOTICE_INDEX = "notice"; //公告索引  用于存储在redis中的索引
	public static final String ACTIVITY_INDEX = "activity"; //活动索引
	public static final String MARQUEE_INDEX = "marquee"; //跑马灯索引
	public static final String CUSTOM_SERVICE_INDEX = "customService"; //跑马灯索引
	public static final String INVITE_CODE_INDEX = "inviteCode"; //邀请码索引
	public static final String INVITE_CODE_DIAMOND_INDEX = "inviteCodeDiamond"; //邀请码送钻石数量的索引
}
