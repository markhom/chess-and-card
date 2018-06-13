package com.linyun.data.mapper;


import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import com.linyun.common.entity.GiftCodeBindInfo;
import com.linyun.common.entity.User;

import java.util.HashMap;

public interface UserMapper
{
	 //用户首次注册登录
	 void register(User user);
	 
	 //通过微信id登录
	 User getUserByWXId(String wxId);
	 
	 //外接平台用户登录
	 
	 User getPlatformUser(HashMap<String, Object> map);
	 
     //用户通过手机号登录
	 User getUserByPhoneNum(long phoneNUm);

	 //更新用户的登录信息
	 void updateLoginInfo(HashMap<String, Object> map);

	 //获取已注册的用户
	 User getExistUser(Integer userId);
	 
	 //更新玩家钻石数
	 void updateDiamond(HashMap<String, Object> map);
	 
	 //更新玩家金币
	 void updateCoin(Map<String,Object> map);
	 
	//更新玩家总局数
	 void updateRoundNum(HashMap<String, Object> map);
	 
	 //玩家通过userId登录，更新玩家的sessionId
	 void updateUserSessionId(Map<String,Object> paramMap);
	 
	 //绑定玩家的邀请码
	 void updateUserInviteCode(Map<String,Object> paramMap);
	 
	 //
	 void updateUserWxInfo(Map<String,Object> paramMap);
	 
	 void updateUserTableNum(Map<String, Object> paraMap);
	 
	 //获取所有玩家的id列表
	 Set<Integer> getAllUserIdList();
	 
	 void bindGiftCode(GiftCodeBindInfo giftCodeBindInfo);
	 
	 //获取玩家所有已经绑定的礼品码
	 Set<String> getBindGiftCode(Integer userId);
	 
	 //玩家已加入俱乐部数量+1
	 void incrementUserJoinClubCount(Integer userId);
	 //玩家已加入俱乐部数量-1
	 void decrementUserJoinClubCount(Integer userId);
	 
	 //玩家申请加入俱乐部数量+1
	 void incrementUserApplyClubCount(Integer userId);
	 //玩家申请加入俱乐部数量-1
	 void decrementUserApplyClubCount(Integer userId);
	 
	 //更新玩家的注册信息
	 void updateRegisterInfo(Map<String, Object> paraMap);
	 
	 //设置玩家为代理
	 void setProxy(Integer userId);
	 //取消玩家的代理身份
	 void cancelProxy(Integer userId);	 
	 //玩家冻结和解冻
	 void setUserFrozenStatue(@Param(value = "condition")User user);
	 
	 void updateUserNickName(Map<String, Object> paraMap);
	 //sessionId登录更新登录时间
	 void updateUserLoginTime(int userId);
}
