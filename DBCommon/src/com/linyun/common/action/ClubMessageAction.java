package com.linyun.common.action;

import java.util.List;

import com.linyun.common.entity.ClubMessage;

public interface ClubMessageAction {
	
	//俱乐部成员操作，生成对应的消息记录
	void addOneClubMessage(ClubMessage cm);
	
	//获取玩家的所有消息
	List<ClubMessage> getAllMessage(int userId);
	
	//
	ClubMessage getMessage(int msgId);
	
	void readMessage(int msgId);
	
	void delMsg(int msgId);
	
	/**
	 * 下面为新增，处理玩家消息的超时消息，消息只保留3天
	 * */
	//获取所有未处理的消息的玩家列表
	List<ClubMessage> getAllTimeoutNoHandleMsg();
	
	//删除三天之前的消息
	void delTimeoutClubMsg();
	
	//对删除的消息进行备份
	void bakMsg();
	
	//在群主删除俱乐部时，查询所有没有处理的申请消息
	List<ClubMessage> selectAllApplyMsgInDelClub(int clubId);
	
	//等待群主答复的有效时间内（3天），玩家不可向该俱乐部发起重复加入的申请
	ClubMessage isApplyJoinClub(int clubId,int applyId);
	
	//拉取玩家是否有未读消息
	List<ClubMessage> getUserUnreadMsg(int userId);
	
}
