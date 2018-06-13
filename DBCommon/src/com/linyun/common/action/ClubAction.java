package com.linyun.common.action;

import java.util.List;

import com.linyun.common.entity.Club;

public interface ClubAction {
	
	//创建俱乐部
	void createClub(Club c);
	//设置俱乐部的扣钻百分比
	void updateClubPercent(int percent, int clubId);
	//设置俱乐部底分倍率
	void updateClubRate(int expandRate, int scoreRate, int clubId);
	//更新俱乐部积分池
	void updateClubScorePool(int scorePool, int clubId);
	
	void updateClubCoinPool(int clubId, int coinPool);
	
	//按照俱乐部Id查询私人俱乐部
	Club selectPrivateClub(int clubId);
	
	//查询所有俱乐部
	List<Club> getAllClub();
	
	//把玩家的userId作为俱乐部的创建者的creatorId，查询玩家是否创建过俱乐部
	Club getClubByCreatorId(int userId);

}
