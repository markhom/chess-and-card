package com.linyun.common.action;

import com.linyun.common.entity.Club;
import com.linyun.common.entity.ClubMember;
import com.linyun.common.entity.ClubMessage;

public interface ClubCommonAction
{
	void CreatorCreateClub(Club club,ClubMessage clubMsg, ClubMember clubMember);//创建者 --- 创建俱乐部
	void CreatorDeleteClub(int creatorId, int clubId, ClubMessage msgCreator);//创建者 --- 删除俱乐部
	void UserApplyJoinClub(int userId, int clubId, ClubMessage msgCretor);//非俱乐部玩家 --- 申请加入俱乐部
	void MemberExitClub(int memberId, int clubId, ClubMessage msgMember, ClubMessage msgCreator);//成员 --- 退出俱乐部
	void CreatorInviteUserJoinClub(int creatorId, int inviteId, int clubId, ClubMessage msgInvite, ClubMessage msgCreator, ClubMember member);//创建者 --- 邀请玩家加入俱乐部
	
	void CreatorAgreeApplyJoinClub(int msgId,int creatorId, int clubId, int applyId, ClubMessage msgApply, ClubMessage msgCreator, ClubMember member);	//创建者审批 --- 同意申请者加入俱乐部
	void CreatorRefuseApplyJoinClub(int msgId,int creatorId, int clubId,int applyId, ClubMessage msgApply, ClubMessage msgCreator);	//创建者审批 --- 拒绝申请者加入俱乐部
	void CreatorKickClubMember(int creatorId, int clubId, int memberId, ClubMessage msgKick, ClubMessage msgCreator);   //创建者 --- 踢出玩家
	void CreatorSetClubConfigInfo(int clubId, String clubName, String clubContent, String iconUrl);//创建者 --- 设置俱乐部信息
	
	void JoinPublicClub(int clubId, int userId, ClubMember clubMember, ClubMessage msgApply);//玩家加入公共俱乐部
	
	void SysHandleTimeOutApply(int msgId, int applyId, ClubMessage msgApply);
}

