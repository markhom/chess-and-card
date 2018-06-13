package com.linyun.data.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.linyun.common.entity.Club;

public interface ClubMapper {
	
	//创建俱乐部
	void createClub(Club c);

	void updateClubPercent(@Param(value="diamondPercent") int diamondPercent, @Param(value="clubId") int clubId);
	
	void updateClubScorePool(@Param(value="scorePool") int scorePool, @Param(value="clubId") int clubId);
	
	void updateClubRate(@Param(value="expandRate") int expandRate, @Param(value="scoreRate") int scoreRate, @Param(value="clubId") int clubId);
	
	void updateClubCoinPool(@Param("clubId")int clubId, @Param("coinPool")int coinPool);
	//通过clubId查询私人俱乐部
	Club getClub(int clubId);
	
	//查询所有俱乐部 私人和公共
	List<Club> getAllClub();
	
	void delClub(int clubId);

	//判断玩家是否已有创建俱乐部
	Club getClubByCreatorId(int userId);
	
	void updateClubConfigInfo(@Param(value="condition")Club c);
	
	//俱乐部人数+1
	void incrementClubCountOne(int clubId);
	
	//俱乐部人数-1
	void decrementClubCountOne(int clubId);
}
