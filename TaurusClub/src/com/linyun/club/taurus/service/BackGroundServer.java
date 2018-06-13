package com.linyun.club.taurus.service;

import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import com.linyun.bottom.common.exception.GameException;
import com.linyun.bottom.container.GameSession;
import com.linyun.bottom.handler.HttpRequest;
import com.linyun.bottom.handler.HttpResponse;
import com.linyun.bottom.log.LoggerFactory;
import com.linyun.bottom.util.OutputMessage;
import com.linyun.club.taurus.engine.GameEngine;
import com.linyun.club.taurus.engine.HundredsTaurusEngine;
import com.linyun.club.taurus.manager.FieldConfigManager;
import com.linyun.club.taurus.manager.GameRoomManager;
import com.linyun.common.entity.Club;
import com.linyun.common.entity.ClubMessage;
import com.linyun.common.entity.FieldConfig;
import com.linyun.common.entity.User;
import com.linyun.middle.common.taurus.club.BaseClubServer;
import com.linyun.middle.common.taurus.club.ClubConfig;
import com.linyun.middle.common.taurus.club.TaurusClub;
import com.linyun.middle.common.taurus.club.TaurusClubMember;
import com.linyun.middle.common.taurus.room.HundredsTaurusRoom;
import com.linyun.middle.common.taurus.room.TaurusRoom;
import com.linyun.middle.common.taurus.service.BaseServer;
import com.linyun.middle.common.taurus.table.TaurusTable;

import net.sf.json.JSONObject;

public class BackGroundServer extends BaseClubServer
{
	private static Logger logger = LoggerFactory.getLogger(BackGroundServer.class);
	/**
	 * 营运后台的接口
	 * */
	public static final String SET_CLUB_CONFIG = "setClubConfig";
	public static final String ADD_PRIVATE_CLUB_USER_COST_LIMIT = "addPrivateClubUserCostLimit";//设置私人俱乐部对应玩家的可消耗钻石额度
	public static final String SET_USER_PROXY = "setUserProxy";
	public static final String CANCEL_USER_PROXY = "cancelUserProxy";
	public static final String RECHARGE_DIAMOND = "rechargeDiamond";
	public static final String DEDUCT_DIAMOND = "deductDiamond";
	public static final String SET_USER_FROZEN_STATUE = "setUserFrozenStatue";
	
	private static final int PUBLIC_USER_COST_LIMIT = 10001;//公共俱乐部每个玩家可消耗房卡的额度
	private static final int PUBLIC_CREATE_ROOM_COST_LIMIT = 10002;//公共俱乐部每次开房消耗的额度
	private static final int PRIVATE_MAX_TABLE_NUM = 10003;//私人俱乐部最大开桌数
	private static final int USER_CAN_JOIN_CLUB_NUM = 10004;//玩家可加入私人俱乐部数量
	private static final int PRIVATE_USER_COUNT_LIMIT = 10005;//私人俱乐部人数上限
	private static final int PRIVATE_CREATE_ROOM_COST_LIMIT = 10006;//私人俱乐部每次开房消耗的额度
	private static final int CREATE_CLUB_SCORERATE_INITIAL_VALUE=10007;//创建私人俱乐部给定的底分倍率初始值
	
	//管理后台修改百人牛牛场次配置
	public void updateFieldConfig(HttpRequest request, HttpResponse response)
	{
		String body = request.body();
		JSONObject obj = new JSONObject();
		try
		{
			JSONObject params = JSONObject.fromObject(body);
			int typeId = params.getInt("typeId");
			int entryLimit = params.getInt("entryLimit");
			int bet_min = params.getInt("bet_min");
			int rate = params.getInt("rate");
			int isOpen = params.getInt("isOpen");
			
			List<FieldConfig> ls = FieldConfigManager.getInstance().configs;
			FieldConfig f = ls.get(typeId-1);
			f.setEntryLimit(entryLimit);
			f.setMin_bet(bet_min);
			f.setRate(rate);
			f.setIsOpen((byte)isOpen);
			
			commonAction().updateFieldConfig(f);
			HundredsTaurusRoom room = GameRoomManager.getInstance().getRoom(typeId);
			HundredsTaurusEngine engine = (HundredsTaurusEngine)room.getObject(GameEngine.GAME_ENGINE);
			engine.setConfig(f);
			obj.put("ret", 0);
			response.content(obj.toString());
			response.end();
			
		}catch(Exception e)
		{
			obj.put("ret", 500);
			response.content(obj.toString());
			response.end();
		}
	}
	//管理后台设置俱乐部扣钻的百分比
	public void setClubDiamondPercent(HttpRequest request, HttpResponse response)
	{
		String body = request.body();
		try
		{
			JSONObject params = JSONObject.fromObject(body);
			int clubId = params.getInt("clubId");
			int percent = params.getInt("percent");
			
			TaurusClub taurusClub = getClub(clubId);
			if(taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST,"设置"+clubId+"俱乐部扣钻百分比时，找不到俱乐部");
				
			}
			clubAction().updateClubPercent(percent, clubId);
			taurusClub.setDiamondPercent(percent);
			
			
			JSONObject obj = new JSONObject();
			obj.put("ret", 0);
			response.content(obj.toString());
			response.end();
			
		}catch(Exception e)
		{
			JSONObject error = new JSONObject();
			error.put("ret", 1);
			response.content(error.toString());
			response.end();
			logger.error(e.getMessage(),e);
		}
	}
	//设置俱乐部底分倍率
	public void setClubRate(HttpRequest request,HttpResponse response)
	{
		String body = request.body();
		try
		{
			JSONObject params = JSONObject.fromObject(body);
			int clubId = params.getInt("clubId");
			int expandRate = params.getInt("expandRate");
			int scoreRate = params.getInt("scoreRate");
			
			TaurusClub taurusClub = getClub(clubId);
			if(taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST,"设置"+clubId+"俱乐部底分倍率时，找不到俱乐部");
				
			}
			clubAction().updateClubRate(expandRate, scoreRate, clubId);
			taurusClub.setExpandRate(expandRate);
			taurusClub.setScoreRate(scoreRate);
			
			JSONObject obj = new JSONObject();
			obj.put("ret", 0);
			response.content(obj.toString());
			response.end();
			
		}catch(Exception e)
		{
			JSONObject error = new JSONObject();
			error.put("ret", 1);
			response.content(error.toString());
			response.end();
			logger.error(e.getMessage(),e);
		}
	}
	/**
	 * 增加俱乐部金币池总量
	 * @param request
	 * @param response
	 */
	public void addClubCoinPool(HttpRequest request, HttpResponse response)
	{
		String body = request.body();
		JSONObject obj = new JSONObject();
		try
		{
			JSONObject params = JSONObject.fromObject(body);
			int clubId = params.getInt("clubId");
			int addCoin = params.getInt("addCoin");
			
			TaurusClub taurusClub = getClub(clubId);
			if(taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST,"设置"+clubId+"俱乐部可消耗金币额度时，找不到俱乐部");
			}
			int currentCoinPool = taurusClub.getClub().getCoinPool()+addCoin;
			clubAction().updateClubCoinPool(clubId, currentCoinPool);
			taurusClub.setClubCoinPool(currentCoinPool);
			
			obj.put("ret", 0);
			response.content(obj.toString());
			response.end();
			
		}catch(GameException e)
		{
			obj.put("ret", e.getId());
			response.content(obj.toString());
			response.end();
		}
		catch(Exception e)
		{
			obj.put("ret", 500);
			response.content(obj.toString());
			response.end();
		}
	}
	
	//代理后台群主给群成员分配金币
	public void addClubMemberCoinLimit(HttpRequest request, HttpResponse response)
	{
		String body = request.body();
		JSONObject obj = new JSONObject();
		try
		{
			JSONObject params = JSONObject.fromObject(body);
			int clubId = params.getInt("clubId");
			int userId = params.getInt("userId");
			int addCoin = params.getInt("addCoin");
			User user = getExistUser(String.valueOf(userId));
			if (user == null)
			{
				throw new GameException(GameException.USER_NOT_EXIST, "后台增加私人俱乐部对应的玩家的可消耗金币额度的时候，通过用户id找不到对应的用户，userId="+userId);
			}
			TaurusClub taurusClub = getClub(clubId);
			if(taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST,"设置玩家" + userId + "的"+clubId+"俱乐部可消耗金币额度时，找不到俱乐部");
			}
			TaurusClubMember clubMember = taurusClub.getMember(userId);
			if (clubMember == null)
			{
				throw new GameException(GameException.CLUB_USER_IS_NOT_MEMBER,"设置玩家" + userId + "的"+clubId+"俱乐部可消耗金币额度时，在俱乐部中找不到当前玩家");
			}
		 
			int coinLimit = clubMember.getCoinLimit();
			if(addCoin > 0 && taurusClub.getClub().getCoinPool()< addCoin)
			{
				throw new GameException(GameException.CLUB_COINPOOL_LACK,"设置玩家"+userId+"在俱乐部"+clubId+"的金币限额时,群主的金币池余额不足！");
			}
			if(addCoin < 0 && user.getCoin()< -addCoin)
			{
				throw new GameException(GameException.CLUB_RECYCLING_COIN_LACK,"俱乐部"+clubId+"的群主在回收群成员"+userId+"的金币时成员的现有金币不足！");
			}
			String content = null;
			if(addCoin>0)
			{
				content = "您在俱乐部"+taurusClub.getClubName()+"("+clubId+")的积分可消耗额度增加"+addCoin;
			}else
			{
				content = "您在俱乐部"+taurusClub.getClubName()+"("+clubId+")被群主回收积分"+(-addCoin);
			}
			GameSession userSession = getUserSession(String.valueOf(userId));
			if (userSession != null)//如果玩家在线通知玩家有新消息
			{
				userSession.sendMessage(ClubServer.Protocol_Ser_New_Msg, new OutputMessage(true));
			}
			ClubMessage clubMsg = new ClubMessage(clubId,userId,content);
			if(addCoin > 0)
			{
				clubMemberAction().updateClubMemberCoinLimit(clubId, userId, coinLimit+addCoin, clubMsg);
				clubMember.setCoinLimit(coinLimit+addCoin);
			}
			int coinPool = taurusClub.getClub().getCoinPool();
			userAction().udpateCoin(String.valueOf(userId), user.getCoin()+addCoin);
			clubAction().updateClubCoinPool(clubId, coinPool-addCoin);
			taurusClub.setClubCoinPool(coinPool-addCoin);
			
			obj.put("ret", 0);
			response.content(obj.toString());
			response.end();
			
		}catch(GameException e)
		{
			obj.put("ret", e.getId());
			response.content(obj.toString());
			response.end();
		}catch(Exception e)
		{
			logger.error(e.getMessage(),e);
			obj.put("ret", 500);
			response.content(obj.toString());
			response.end();
		}
	}
	
	//代理后台群主给群成员增加或者回收积分接口
	public void addClubMemberScoreLimit(HttpRequest request, HttpResponse response)
	{
		String body = request.body();
		Semaphore sem = null;
		try
		{ 
			JSONObject params = JSONObject.fromObject(body);
			int clubId = params.getInt("clubId");
			int userId = params.getInt("userId");
			int addScore = params.getInt("addScore");//正数为增加，负数为回收
			
			User user = getExistUser(String.valueOf(userId));
			if (user == null)
			{
				throw new GameException(GameException.USER_NOT_EXIST, "后台增加私人俱乐部对应的玩家的可消耗积分额度的时候，通过用户id找不到对应的用户，userId="+userId);
			}
			
			String strUserId = String.valueOf(userId);
		    sem = getSemp(strUserId);
			sem.acquire();
			TaurusClub taurusClub = getClub(clubId);
			if(taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST,"设置玩家" + userId + "的"+clubId+"俱乐部可消耗积分额度时，找不到俱乐部");
			}
			TaurusClubMember clubMember = taurusClub.getMember(userId);
			if (clubMember == null)
			{
				throw new GameException(GameException.CLUB_USER_IS_NOT_MEMBER,"设置玩家" + userId + "的"+clubId+"俱乐部可消耗积分额度时，在俱乐部中找不到当前玩家");
			}
			
			if(addScore > 0 && taurusClub.getScorePool() < addScore)
			{
				throw new GameException(GameException.CLUB_SCOREPOOL_LACK,"设置玩家"+userId+"在俱乐部"+clubId+"的可消耗积分超出积分池现有积分！");
			}
			if(addScore < 0 && clubMember.getCurrentScore() < -addScore)
			{
				throw new GameException(GameException.CLUB_RECYCLING_SCORE_LACK,"俱乐部"+clubId+"群主在回收玩家"+userId+"的积分时玩家积分不足！");
			}
		
		    int scoreLimit = clubMember.getScoreLimit();
		    int currentScore = clubMember.getCurrentScore();
			String strContent = null;
			if(addScore>0)
			{
				strContent = "您在俱乐部"+taurusClub.getClubName()+"("+clubId+")的积分可消耗额度增加"+addScore;
			}else
			{
				strContent = "您在俱乐部"+taurusClub.getClubName()+"("+clubId+")被群主回收积分"+(-addScore);
			}
			
			
			GameSession userSession = getUserSession(strUserId);
			if (userSession != null)//如果玩家在线通知玩家有新消息
			{
				userSession.sendMessage(ClubServer.Protocol_Ser_New_Msg, new OutputMessage(true));
			}
			ClubMessage clubMsg = new ClubMessage(clubId,userId,strContent);
			clubMemberAction().updateClubMemberScoreLimit(clubId, userId, addScore, clubMsg);
			int old_scorePool = taurusClub.getScorePool();
			//更新俱乐部积分池
			clubAction().updateClubScorePool(old_scorePool-addScore, clubId);
			taurusClub.setScorePool(old_scorePool-addScore);
			clubMember.setCurrentScore(currentScore+addScore);
			if(addScore > 0)
			{
				clubMember.setScoreLimit(scoreLimit+addScore);
			}
			
			JSONObject obj = new JSONObject();
			obj.put("ret", 0);
			response.content(obj.toString());
			response.end();
			
		}catch(GameException e)
		{
			JSONObject error = new JSONObject();
			error.put("ret", e.getId());
			response.content(error.toString());
			response.end();
			logger.error(e.getMessage(),e);
		}catch(Exception e)
		{
			JSONObject error = new JSONObject();
			error.put("ret", 500);
			response.content(error.toString());
			response.end();
			logger.error(e.getMessage(),e);
		}
		finally
		{
			if(sem !=null)
			{
				sem.release();
			}
		}
	}
	//设置公共俱乐部每个玩家可消耗房卡的额度
	public void setClubConfig(HttpRequest request, HttpResponse response)
	{    
		String body = request.body();
		try
		{
			JSONObject paramJson = JSONObject.fromObject(body);
			int configType = paramJson.getInt("configType");
			int value = paramJson.getInt("value");
			logger.info("BackGroundServer::setClubConfig configType=" + configType + ",value=" + value);
			
			clubConfigAction().updateClubConfigByType(value, configType);
			switch (configType)
			{
				case PUBLIC_USER_COST_LIMIT:
					ClubConfig.INSTANCE.getConfig().setPubClubUserCostLimit(value);
					break;
				case PUBLIC_CREATE_ROOM_COST_LIMIT:
					ClubConfig.INSTANCE.getConfig().setPubClubCreateRoomCostLimit(value);
					break;
				case PRIVATE_MAX_TABLE_NUM:
					ClubConfig.INSTANCE.getConfig().setPriClubMaxTableNum(value);
					break;
				case USER_CAN_JOIN_CLUB_NUM:
					ClubConfig.INSTANCE.getConfig().setUserCanJoinPriClubNum(value);
					break;
				case PRIVATE_USER_COUNT_LIMIT:
					ClubConfig.INSTANCE.getConfig().setPriClubPeopleMaxNum(value); 
					break;
				case PRIVATE_CREATE_ROOM_COST_LIMIT:
					ClubConfig.INSTANCE.getConfig().setPriClubCreateRoomCostLimit(value);
					break;	
				case CREATE_CLUB_SCORERATE_INITIAL_VALUE:
					ClubConfig.INSTANCE.getConfig().setCreateClubScoreRateInitialValue(value);
				    break;
				default:
					logger.error("BackGroundServer::setClubConfig : 错误的配置类型");
					break;
			 }
			
			 JSONObject js = new JSONObject();
			 js.put("ret", (short)0);
			 response.content(js.toString());
			 response.end();
			 logger.info("管理后台操作，配置表：configType is "+configType+",value is "+value+"根据类型设置值success");
		 }
		 catch (GameException e)
		 {
			 JSONObject json = new JSONObject();
			 json.put("ret", e.getId());
			 response.content(json.toString());
			 response.end();
			 logger.error(e.getMessage(), e);
		 }
		 catch (Exception e)
		 {
			 JSONObject json = new JSONObject();
			 json.put("ret", 500);
			 response.content(json.toString());
			 response.end();
			 logger.error(e.getMessage(), e);
		}
	}
	
	//增加私人俱乐部对应玩家的可消耗钻石额度
	public void addPrivateClubUserCostLimit(HttpRequest request, HttpResponse response)
	{    
		String body = request.body();
		Semaphore sem = null;
		try
		{
			JSONObject paramJson = JSONObject.fromObject(body);
			int clubId = paramJson.getInt("clubId");
			int userId = paramJson.getInt("userId");
			int value = paramJson.getInt("value");
			
			User user = getExistUser(String.valueOf(userId));
			if (user == null)
			{
				throw new GameException(GameException.USER_NOT_EXIST, "后台增加私人俱乐部对应的玩家的可消耗钻石额度的时候，通过用户id找不到对应的用户，userId="+userId);
			}
			
			logger.info("BackGroundServer::setPrivateClubUserCostLimit clubId=" + clubId +",userId=" + userId + ",value=" + value);
			String strUserId = String.valueOf(userId);
			sem = getSemp(strUserId);
			sem.acquire();
			TaurusClub taurusClub = getClub(clubId);
			if(taurusClub == null)
			{
				throw new GameException(GameException.CLUB_IS_NOT_EXIST,"设置玩家" + userId + "的"+clubId+"俱乐部可消耗钻石额度时，找不到俱乐部");
			}
			TaurusClubMember clubMember = taurusClub.getMember(userId);
			if (clubMember == null)
			{
				throw new GameException(GameException.CLUB_USER_IS_NOT_MEMBER,"设置玩家" + userId + "的"+clubId+"俱乐部可消耗钻石额度时，在俱乐部中找不到当前玩家");
			}
			//设置钻石不能超过群主钻石
			int creatorId = taurusClub.getCreatorId();
			User creatorUser = getExistUser(String.valueOf(creatorId));
			if(value > creatorUser.getDiamond())
			{
				throw new GameException(GameException.CLUB_CREATOR_SET_MEMBER_DIAMOND_LIMIT,
						"群主："+creatorId+"设置成员"+userId+"的额度时，设置额度超过了群主拥有的钻石");
			}
			int oldDiamondLimit = clubMember.getDiamondLimit();
			int newDIamondLimit = oldDiamondLimit + value;
			clubMember.setDiamondLimit(newDIamondLimit);
			
			String strContent = "您在俱乐部"+taurusClub.getClubName()+"("+clubId+")的房卡可消耗额度增加"+value+",增加之后的总额度为"+newDIamondLimit+"，增加之前额度为"+oldDiamondLimit;
			ClubMessage clubMessage = new ClubMessage(clubId, userId, strContent);
			
			clubMemberAction().addCostDiamondLimit(clubId, userId, newDIamondLimit, clubMessage);
			GameSession userSession = getUserSession(strUserId);
			if (userSession != null)//如果玩家在线通知玩家有新消息
			{
				userSession.sendMessage(ClubServer.Protocol_Ser_New_Msg, new OutputMessage(true));
			}
			
			JSONObject js = new JSONObject();
			js.put("ret", (short)0);
			response.content(js.toString());
			response.end();
			logger.info("群主设置玩家userId is "+userId+",最大可用额度 costDiamond is "+newDIamondLimit+"success");
		}
		catch (GameException e)
		{
			JSONObject json = new JSONObject();
			json.put("ret", e.getId());
			response.content(json.toString());
			response.end();
			logger.error(e.getMessage(), e);
		}
		catch (Exception e)
		{
			JSONObject json = new JSONObject();
			json.put("ret", 500);
			response.content(json.toString());
			response.end();
			logger.error(e.getMessage(), e);
		}
		finally 
		{
			if (sem != null)
			{
				sem.release();
			}
		}
	}
	
	//设置玩家成为代理
	public void setUserProxy(HttpRequest request, HttpResponse response)
	{    
		String body = request.body();
		try
		{
			JSONObject paramJson = JSONObject.fromObject(body);
			int userId = paramJson.getInt("userId");
			logger.info("BackGroundServer::setUserProxy userId=" + userId);
			User user = getExistUser(String.valueOf(userId));
			if (user == null)
			{
				throw new GameException(GameException.USER_NOT_EXIST, "后台设置玩家成为代理时，通过用户id找不到对应的用户，userId="+userId);
			}
			userAction().setProxy(userId);
			
			JSONObject js = new JSONObject();
			js.put("ret", (short)0);
			response.content(js.toString());
			response.end();
			logger.info("管理后台操作，玩家：userId is "+userId+"设置成为代理success");
		}
		catch (GameException e)
		{
			JSONObject json = new JSONObject();
			json.put("ret", e.getId());
			response.content(json.toString());
			response.end();
			logger.error(e.getMessage(), e);
		}
		catch (Exception e)
		{
			JSONObject json = new JSONObject();
			json.put("ret", 500);
			response.content(json.toString());
			response.end();
			logger.error(e.getMessage(), e);
		}
	}
		
	//取消玩家的代理身份
	public void cancelUserProxy(HttpRequest request, HttpResponse response)
	{    
		String body = request.body();
		try
		{
			JSONObject paramJson = JSONObject.fromObject(body);
			int userId = paramJson.getInt("userId");
			logger.info("BackGroundServer::cancelUserProxy userId=" + userId);
			User user = getExistUser(String.valueOf(userId));
			if (user == null)
			{
				throw new GameException(GameException.USER_NOT_EXIST, "后台取消玩家代理身份时，通过用户id找不到对应的用户，userId="+userId);
			}
			
			userAction().cancelProxy(userId);
			
			JSONObject js = new JSONObject();
			js.put("ret", (short)0);
			response.content(js.toString());
			response.end();
			logger.info("管理后台操作，玩家：userId is "+userId+"取消代理success");
		}
		catch (GameException e)
		{
			JSONObject json = new JSONObject();
			json.put("ret", e.getId());
			response.content(json.toString());
			response.end();
			logger.error(e.getMessage(), e);
		}
		catch (Exception e)
		{
			JSONObject json = new JSONObject();
			json.put("ret", 500);
			response.content(json.toString());
			response.end();
			logger.error(e.getMessage(), e);
		}
	}
	
	//充值钻石接口
	public void rechargeDiamond(HttpRequest request, HttpResponse response)
	{    
		String body = request.body();
		try
		{
			JSONObject paramJson = JSONObject.fromObject(body);
			int userId = paramJson.getInt("userId");
			int diamond = paramJson.getInt("diamond");
			String remark = paramJson.getString("remark");
			logger.info("BackGroundServer::rechargeDiamond userId=" + userId + ",diamond=" + diamond);
			User user = getExistUser(String.valueOf(userId));
			if (user == null)
			{
				throw new GameException(GameException.USER_NOT_EXIST, "后台充值玩家钻石时，通过用户id找不到对应的用户，userId="+userId);
			}
			
			Club club = clubAction().getClubByCreatorId(userId);
			if(club != null)
			{
				 TaurusClub taurusClub = getClub(club.getClubId());
				 TaurusClubMember taurusMember = taurusClub.getMember(userId);
				 taurusMember.setDiamondLimit(user.getDiamond() + diamond);
			}
			userAction().manualRechargeDiamond(String.valueOf(userId), diamond,remark);
			 
			JSONObject js = new JSONObject();
			js.put("ret", (short)0);
			response.content(js.toString());
			response.end();
			logger.info("管理后台操作，玩家：userId is "+userId+",手动充值"+diamond+"钻石success");
		}
		catch (GameException e)
		{
			JSONObject json = new JSONObject();
			json.put("ret", e.getId());
			response.content(json.toString());
			response.end();
			logger.error(e.getMessage(), e);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			JSONObject json = new JSONObject();
			json.put("ret", 500);
			response.content(json.toString());
			response.end();
			logger.error(e.getMessage(), e);
		}
	}
	
	//扣除钻石接口
	public void deductDiamond(HttpRequest request, HttpResponse response)
	{    
		String body = request.body();
		try
		{
			JSONObject paramJson = JSONObject.fromObject(body);
			int userId = paramJson.getInt("userId");
			int diamond = paramJson.getInt("diamond");
			String remark = paramJson.getString("remark");
			logger.info("BackGroundServer::deductDiamond userId=" + userId + ",diamond=" + diamond);
		
			User user = getExistUser(String.valueOf(userId));
			if (user == null)
			{
				throw new GameException(GameException.USER_NOT_EXIST, "后台扣除玩家钻石时，通过用户id找不到对应的用户，userId="+userId);
			}
			//手工扣去的钻石超过了玩家的钻石
			if(diamond > user.getDiamond())
			{
				throw new GameException(GameException.CLUB_CREATOR_DIAMOND_IS_NOT_ENOUGH,"玩家userId is "+userId+"钻石不足");
			}
			
			Club club = clubAction().getClubByCreatorId(userId);
			if(club != null)
			{
				 TaurusClub taurusClub = getClub(club.getClubId());
				 TaurusClubMember taurusMember = taurusClub.getMember(userId);
				 taurusMember.setDiamondLimit(user.getDiamond() - diamond);
			}
			
			userAction().manualDeductDiamond(String.valueOf(userId), diamond,remark);
		 
			JSONObject js = new JSONObject();
			js.put("ret", (short)0);
			response.content(js.toString());
			response.end();
			logger.info("管理后台操作，玩家：userId is "+userId+",手动扣除"+diamond+"钻石success");
		}
		catch (GameException e)
		{
			JSONObject json = new JSONObject();
			json.put("ret", e.getId());
			response.content(json.toString());
			response.end();
			logger.error(e.getMessage(), e);
		}	
		catch (Exception e)
		{
			JSONObject json = new JSONObject();
			json.put("ret", 500);
			response.content(json.toString());
			response.end();
			logger.error(e.getMessage(), e);
		}
	}
	
	//用户冻结和解冻
	public void setUserFrozenStatue(HttpRequest request, HttpResponse response)
	{
		String body = request.body();
		try
		{
			JSONObject jsonObj = JSONObject.fromObject(body);
			String userId = jsonObj.getString("userId");
			int statue = jsonObj.getInt("statue");
			
			logger.info("管理后台更改玩家状态是否冻结，userId is "+userId+",statue is "+statue);
			//冻结或者解冻玩家的时候，不需要处理玩家此时是否处于冻结状态
			getExistUserNoCareFrozen(userId);
			
			/**
			 * 1.用户未登陆，直接冻结，用户登录时提示已被冻结
			 * 2.用户在登录大厅页面被冻结
			 * 3.用户在好友场或者俱乐部没有在桌子坐下被冻结
			 * 4.用户在好友场或者俱乐部桌子坐下之后被冻结（托管处理）
			 */
			//好友场游戏房间处理
			taurusGameAction().bindUserTable(userId);
			
			//俱乐部游戏房间处理
			GameSession session = BaseServer.getUserSession(userId);
			if(session != null)
			{
				OutputMessage om = new OutputMessage(true);
				session.sendMessage(ClubGameServer.PROTOCOL_Ser_Frozen, om);
				
				TaurusTable taurusTable = BaseServer.getUserTable(userId);
				//冻结玩家时，玩家已经在桌子坐下，玩家托管处理
				if(taurusTable != null)
				{
					taurusTable.getUserSeat(userId).setAutoAction(true); 
					taurusTable.getUserSeat(userId).getUser().setIsFrozen((byte)statue);
				}
				//没有坐下的玩家 移除
				else
				{
					TaurusRoom game_room = BaseServer.getUserRoom(userId);
					if(game_room != null)
					{
						BaseServer.unbindUserRoom(userId);
						game_room.removeSession(userId);
					}
					
					TaurusClub taurusClub = BaseClubServer.getUserClub(userId);
					if(taurusClub != null)
					{
						BaseClubServer.unbindUserClub(userId);
						taurusClub.removeUserSession(userId);
					}
					BaseServer.removeUser(session);
				}
			}
			
			userAction().setUserFrozenStatue(userId, statue);
			
			JSONObject js = new JSONObject();
			js.put("ret", (short)0);
			response.content(js.toString());
			response.end();
			logger.info("冻结和解冻玩家，userId is "+userId+",状态 statue is "+statue+",success(1--冻结 ，0--解冻)");
			
		}
		catch (GameException e)
		{
			JSONObject json = new JSONObject();
			json.put("ret", e.getId());
			response.content(json.toString());
			response.end();
			logger.error(e.getMessage(), e);
		}	
		catch (Exception e) 
		{
			JSONObject json = new JSONObject();
			json.put("ret", 500);
			response.content(json.toString());
			response.end();
			logger.error(e.getMessage(), e);
		}
		
	}
	
	//后台更改公告
	public void  updateNoticeContent(HttpRequest request, HttpResponse response)
	{
		String body = request.body();
		try
		{
			JSONObject jsonParam = JSONObject.fromObject(body);
			String content = jsonParam.getString("content");
			int id = jsonParam.getInt("id");
			content = content == null ? "":content;
			logger.info("update notice ,content is "+content+", id is "+id);
			commonAction().updateNoticeContent(content, id);
			
			JSONObject js = new JSONObject();
			js.put("ret", (short)0);
			response.content(js.toString());
			response.end();
			logger.info("update notice content success,content is "+content+", id is "+id);
		}
		catch (GameException e)
		{
			JSONObject json = new JSONObject();
			json.put("ret", e.getId());
			response.content(json.toString());
			response.end();
			logger.error(e.getMessage(), e);
		}	
		catch (Exception e) 
		{
			JSONObject json = new JSONObject();
			json.put("ret", 500);
			response.content(json.toString());
			response.end();
			logger.error(e.getMessage(), e);
		}
		
	}
	
	//跑马灯更改
	 public void  updateMarquee(HttpRequest request, HttpResponse response)
	 {   
		 String body = request.body();
		 try 
		 {
			 JSONObject jsonParam = JSONObject.fromObject(body);
			 String content = jsonParam.getString("content");
			 int id = jsonParam.getInt("id");
			 int voild = jsonParam.getInt("voild");
			 
			 logger.info("update marquee, id is "+id+",content is "+content+",voild is "+voild);
			 commonAction().updateMarquee(id, content, voild);
			 
			 JSONObject js = new JSONObject();
			 js.put("ret", (short)0);
			 response.content(js.toString());
			 response.end();
			 logger.info("update marquee content success,id is "+id+",content is "+content+",voild is "+voild);
		 } 
		 catch (GameException e)
		 {
			 JSONObject json = new JSONObject();
			 json.put("ret", e.getId());
			 response.content(json.toString());
			 response.end();
			 logger.error(e.getMessage(), e);
		 }	
		 catch (Exception e) 
		 {
			 JSONObject json = new JSONObject();
			 json.put("ret", 500);
			 response.content(json.toString());
			 response.end();
			 logger.error(e.getMessage(), e);
		 }
		 	
	 }
	
	
}
