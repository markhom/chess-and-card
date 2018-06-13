package com.linyun.club.taurus.timer;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.linyun.bottom.container.GameSession;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.club.taurus.service.ClubServer;
import com.linyun.club.taurus.utils.TimeUtil;
import com.linyun.common.entity.ClubMessage;
import com.linyun.middle.common.taurus.club.BaseClubServer;
import com.linyun.middle.common.taurus.club.TaurusClub;

public class ClubMsgTimeOutTask extends BaseClubServer
{
	private static Logger logger = LoggerFactory.getLogger(ClubMsgTimeOutTask.class);
	public static final long ROOM_TIMEOUT_DISSOLUTION_TIME = 24*60*60; //unit seconds
	
	public static final long ROOM_TIMEOUT_DISSOLUTION_TIME_COUNT = 24*60*60*1000;  //unit seconds
	
	private static ClubMsgTimeOutTask clubMsgTimeOutTask = new ClubMsgTimeOutTask();
	
	public static ClubMsgTimeOutTask getInstance()
	{
		return clubMsgTimeOutTask;
	}
	
	/**
	 * 每分钟检查一次 房间表中的 超过三十分钟未开始游戏的记录
	 * */
	public void start()
	{
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);  
	    
		Runnable runnable = new Runnable() 
		{
			@Override
			public void run() 
			{
				try
				{
					List<ClubMessage> list = clubMessageAction().getAllTimeoutNoHandleMsg();
					int clubId = 0;
					TaurusClub club = null;
					GameSession session = null;
					for (ClubMessage clubMsg : list)
					{
						clubId = clubMsg.getClubId();
						club = getClub(clubId);
						if (club == null)
						{
							logger.error("in ClubMsgTimeOutTask::run, club is null");
							clubMessageAction().delMsg(clubMsg.getId());
							continue;
						}
						
						String applyContent = "您加入"+club.getClubName()+"("+clubId+")的申请由于超过三天未被俱乐部创建者处理,已被系统默认拒绝";
						ClubMessage msgApply = new ClubMessage(clubId, clubMsg.getApplyId(), applyContent); 
						clubCommonAction().SysHandleTimeOutApply(clubMsg.getId(), clubMsg.getApplyId(), msgApply);
						
						session = getUserSession(String.valueOf(clubMsg.getApplyId()));
						if (session != null)
						{//如果被拒绝消息的玩家在线，则通知其有新消息
							session.sendMessage(ClubServer.Protocol_Ser_New_Msg, new OutputMessage(true));
						}
					}
					
					//将超过三天的消息进行备份
					clubMessageAction().bakMsg();
					
					//删除三天之前的消息
					clubMessageAction().delTimeoutClubMsg();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		long nextDayMorning = TimeUtil.getInit(); //第二天凌晨时间
		long curTime = System.currentTimeMillis();//当前时间
		long delayTime = nextDayMorning - curTime ;//距离第一次启动的时间
	    service.scheduleAtFixedRate(runnable, delayTime, ROOM_TIMEOUT_DISSOLUTION_TIME_COUNT, TimeUnit.MILLISECONDS);  
	}
}
