package com.linyun.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.linyun.common.entity.ClubMember;

public interface ClubMemberMapper {
	
	//俱乐部群主同意申请，新增一个俱乐部成员
	void addOneClubMember(ClubMember cm);
	
	//删除一条俱乐部成员记录
	void delClubMember(int userId,int clubId);
	
	List<ClubMember> getAllMember(int clubId);
	
	List<String> getAllClubId(int userId);
	
	//根据clubId与userid查询该俱乐部的成员对象
	ClubMember getClubMemberByClubId(@Param("clubId")int clubId,@Param("userId")int userId);
	
	//玩家消耗群主的钻石增加
	void updateClubMemberCostDiamond(@Param("clubId")int clubId, @Param("userId")int userId, @Param("costDiamond")int diamond);
	
	//更改俱乐部群主在成员表中总额度
	void updateClubCreatorDiamondLimit(@Param("clubId")int clubId, @Param("userId")int userId, @Param("diamondLimit")int diamond);

	void addDiamondLimit(@Param("clubId")int clubId, @Param("userId")int userId,  @Param("diamondLimit")int diamondLimit);
	
	void updateClubMemberScoreLimit(@Param("clubId")int clubId, @Param("userId") int userId, @Param("scoreLimit") int scoreLimit,@Param("currentScore") int currentScore);
	
	void updateClubMemberCurrentScore(@Param("clubId")int clubId, @Param("userId") int userId, @Param("currentScore") int currentScore);
	
	void updateClubMemberCoinLimit(@Param("clubId")int clubId,@Param("userId") int userId, @Param("coinLimit") int coinLimit);
}
