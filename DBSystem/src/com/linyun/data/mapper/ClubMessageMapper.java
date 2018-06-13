package com.linyun.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.linyun.common.entity.ClubMessage;

public interface ClubMessageMapper 
{
	//俱乐部成员操作，生成对应的消息记录
	void addOneClubMessage(ClubMessage cm);
	
	List<ClubMessage> getAllMessage(int userId);
	
	ClubMessage getMessage(int msgId);
	
	void readMessage(int msgId);
	
	//
	List<ClubMessage> getAllTimeoutNoHandleMsg();
	
	void delMsg(int msgId);
	
	void bakClubMsg();
	
	//删除超过三天之前的消息
	void delTimeoutClubMsg();
	
	//删除俱乐部时，查询所有没有处理的申请消息
	List<ClubMessage> selectAllApplyMsgInDelClub(int clubId);
	
	ClubMessage isApplyJoinClub(@Param("clubId")int clubId,@Param("applyId")int applyId);
	
	//拉取玩家所有未读消息
	List<ClubMessage> getUserUnreadMsg(int userId);
}
