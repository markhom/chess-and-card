package com.linyun.common.action;

import java.util.List;

import com.linyun.common.entity.ClubMember;
import com.linyun.common.entity.ClubMessage;


public interface ClubMemberAction {

	//俱乐部群主同意玩家申请，俱乐部新增一个俱乐部成员
	void addOnePrivateClubMember(ClubMember cm);
	
	//私人俱乐部成员退出俱乐部
	void exitPrivateClub(int userId ,int clubId);
	
	//通过俱乐部id获取所有俱乐部成员列表
	List<ClubMember> getAllMember(int clubId);
	
	//获取所有跟玩家相关的俱乐部id
	List<String> getAllClubId(int userId);
	
	//根据clubId和userId查询clubMember对象
	ClubMember getClubMemberByClubId(int clubId , int userId);
	
	//增加俱乐部成员的房卡可消耗额度
	void addCostDiamondLimit(int clubId, int userId, int diamondLimit, ClubMessage clubMessage);
	
	//增加俱乐部成员的积分额度并刷新可买入积分
	void updateClubMemberScoreLimit(int clubId, int userId, int scoreLimit,ClubMessage clubMessage);
	
	//增加俱乐部成员的金币额度
	void updateClubMemberCoinLimit(int clubId, int userId, int coinLimit,ClubMessage clubMessage);
	
	//更新俱乐部成员的现有积分
	void updateClubMemberCurrentScore(int clubId, int userId, int currentScore);
	
}
