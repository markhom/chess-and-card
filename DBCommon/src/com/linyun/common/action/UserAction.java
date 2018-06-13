package com.linyun.common.action;

import com.linyun.bottom.common.exception.GameException;
import com.linyun.common.entity.Club;
import com.linyun.common.entity.User;

public interface UserAction {
	
	//新用户用微信第一次注册
	User registerUser(User user);
	
	//BG平台用户注册
	User registerPlatformUser(User user);
	
	//用户微信id登录
	User loginByWXId(String wxId);
	
	//外接平台用户登录
	User getPlatformUser(int platformId, String userName) throws GameException;
	
	//用户手机号登录
	User getUserByPhoneNum(long phoneNum);
	
	//获取存在的user，不存在抛出异常
	User getExistUser(String userId) throws GameException;
	
	//获取存在的user，不存在抛出异常
	User getExistUserBySql(String userId) throws GameException;
	
	//获取存在用户，不存在抛出异常，冻结或者解冻用户调用
	User getExistUserNoCareFrozen(String userId) throws GameException;

	//用户每次登陆，更新用户的登录时间
	void  updateLoginInfo(int userId, String ip, String location);
	
	//根据wxId获取玩家对象
	 User getExistUserByWXId(String wxId);
	 
	/*--------------更新玩家的微信相关的个人信息 1.头像 2.昵称-------------------*/
	void updateUserWxInfo(String userId , String headImgUrl, String nickName);
	
	/*--------------更新玩家的注册信息 ------------------*/
	void updateUserRegisterInfo(String userId, String registerIp, String position);
	/*--------------更新玩家的登录信息 ------------------*/
	void updateUserLoginInfo(String userId , String loginIp, String position);
	
	/*玩家通过userId登录，更新玩家的sessionId*/
	void updateUserSessionId(String userId , String sessionId);
	
	/*绑定玩家的邀请码到数据库*/
	void bindUserInviteCode(String userId,int inviteCode);
	
	//给玩家充值钻石
	void rechargeDiamond(String userId, int diamond);
	//扣除玩家的钻石
	void deductDiamond(String userId, int diamond);
	//扣除玩家的钻石
	void deductDiamond(String userId, int diamond, Club club,int roomNum);
	 
	//玩家的总局数加一
	void addUserOneRound(String userId);
	
	//退还玩家扣除的房费
	void returnRoomPayDiamond(String userId, int diamond);
	//退还玩家扣除的房费
	void returnRoomPayDiamond(String userId, int diamond, int clubId,int roomNum);
	
	//房间未开始就解散返还群主钻石(新版H5专用)
	void returnRoomOwnerDiamond(String userId, int diamond, Club club,int roomNum);
	
	//更新玩家的是否在桌子里的状态，主动退出房间时清除，收到客户端的“收到解散消息”时清除
	void updateUserTableNum(String userId, String tableNum);
	
	boolean isBindGiftCode(String userId, String giftCode);
	  
	void bindGiftCode(String userId, String giftCode, int presentDiamond);
	
	//玩家加入一个俱乐部成功或者创建一个俱乐部，更新玩家拥有的俱乐部的数量
	void updateUserClubCount(int userId);
	
	//设置\取消 用户代理身份
	void setProxy(int userId);
	void cancelProxy(int userId);
	
	//给玩家充值钻石
	void manualRechargeDiamond(String userId, int diamond,String remark);
	//扣除玩家的钻石
	void manualDeductDiamond(String userId, int diamond,String remark);
	//用户冻结和解冻
	void setUserFrozenStatue(String userId ,int statue);
	
	void updateUserNickName(int userId, String nickName);
	//用户通过sessionId登录，更新登录时间
	void updateUserLoginTime(String userId);
	
	//更新玩家金币
	void udpateCoin(String userId, int coin);
}
