package com.linyun.common.entity;

import java.io.Serializable;
import java.sql.Date;

public class User implements Serializable,Comparable<User>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6870891587035440536L;
	/**正常用户*/
	public static final byte STATE_OPEN = 0;
	/**冻结用户*/
	public static final byte STATE_FROZEN = 1;
	
	private Integer userId ;
	private String wxId ;//微信id
	private byte sex ;//性别
	private String nickName ;//昵称
	private String headImgUrl;//头像地址
	private String loginAddress ; //登录的地理位置
	private String registerIp ;//注册ip
	private String loginIp ; // 登录ip
	private int  diamond ; //玩家的钻石
	private int  coin; //玩家的金币
	private int  roundNum ; //玩家已玩游戏的总局数
	private Date registerTime ; //注册时间
	private Date loginTime ; 
	private String sessionId ; //用已登录的sessionId 
	private byte isFrozen ; //是否冻结 0--没有冻结  1--已经冻结
	private int inviteCode ; //玩家绑定的邀请码
	private String tableNum;//玩家是否在桌子内
	private int applyClubCount ;//正在申请加入的俱乐部数量
	private int clubCount ; //已经加入的俱乐部的数量
	private boolean isProxy;//玩家是否代理
	private long phoneNum;
	private int platformId;//平台id(自定义的代号1)
	private String userName;//平台用户的账户名
	private int seatId; //座位id
	
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getWxId() {
		return wxId;
	}
	public void setWxId(String wxId) {
		this.wxId = wxId;
	}
	public byte getSex() {
		return sex;
	}
	public void setSex(byte sex) {
		this.sex = sex;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getHeadImgUrl() {
		return headImgUrl;
	}
	public void setHeadImgUrl(String headImgUrl) {
		this.headImgUrl = headImgUrl;
	}
	public String getLoginAddress() {
		return loginAddress;
	}
	public void setLoginAddress(String loginAddress) {
		this.loginAddress = loginAddress;
	}
	public String getRegisterIp() {
		return registerIp;
	}
	public void setRegisterIp(String registerIp) {
		this.registerIp = registerIp;
	}
	public String getLoginIp() {
		return loginIp;
	}
	public void setLoginIp(String loginIp) {
		this.loginIp = loginIp;
	}
	public int getDiamond() {
		return diamond;
	}
	public void setDiamond(int diamond) {
		this.diamond = diamond;
	}
	public int getRoundNum() {
		return roundNum;
	}
	public void setRoundNum(int roundNum) {
		this.roundNum = roundNum;
	}
	public Date getRegisterTime() {
		return registerTime;
	}
	public void setRegisterTime(Date registerTime) {
		this.registerTime = registerTime;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public byte getIsFrozen() {
		return isFrozen;
	}
	public void setIsFrozen(byte isFrozen) {
		this.isFrozen = isFrozen;
	}
	public Date getLoginTime() {
		return loginTime;
	}
	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}

	public int getInviteCode() {
		return inviteCode;
	}
	public void setInviteCode(int inviteCode) {
		this.inviteCode = inviteCode;
	}
	public String getTableNum()
	{
		return tableNum;
	}
	public void setTableNum(String tableNum)
	{
		this.tableNum = tableNum;
	}
	public int getApplyClubCount() {
		return applyClubCount;
	}
	public void setApplyClubCount(int applyClubCount) {
		this.applyClubCount = applyClubCount;
	}
	public int getClubCount() {
		return clubCount;
	}
	public void setClubCount(int clubCount) {
		this.clubCount = clubCount;
	}
	public boolean isProxy()
	{
		return isProxy;
	}
	public void setProxy(boolean isProxy)
	{
		this.isProxy = isProxy;
	}
	public long getPhoneNum()
	{
		return phoneNum;
	}
	public void setPhoneNum(long phoneNum)
	{
		this.phoneNum = phoneNum;
	}
	public int getPlatformId() 
	{
		return platformId;
	}
	public void setPlatformId(int platformId) 
	{
		this.platformId = platformId;
	}
	public String getUserName() 
	{
		return userName;
	}
	public void setUserName(String userName) 
	{
		this.userName = userName;
	}
	public int getCoin() 
	{
		return coin;
	}
	public void setCoin(int coin) 
	{
		this.coin = coin;
	}
	
	public int getSeatId()
	{
		return seatId;
	}
	public void setSeatId(int seatId) 
	{
		this.seatId = seatId;
	}
	@Override
	public int compareTo(User o) 
	{
		if(this.coin < o.getCoin())
			return 1;
		else if(this.coin > o.getCoin())
			return -1;
		return 0;
	}
	
	
}
