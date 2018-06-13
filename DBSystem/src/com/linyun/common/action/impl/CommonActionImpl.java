package com.linyun.common.action.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.session.SqlSession;

import com.linyun.bottom.cached.RedisResource;
import com.linyun.bottom.common.exception.GameException;
import com.linyun.common.action.CommonAction;
import com.linyun.common.entity.CustomService;
import com.linyun.common.entity.DomainConfig;
import com.linyun.common.entity.FieldConfig;
import com.linyun.common.entity.GameConfig;
import com.linyun.common.entity.GiftCode;
import com.linyun.common.entity.Marquee;
import com.linyun.common.entity.Notice;
import com.linyun.common.entity.PayConfig;
import com.linyun.common.entity.SectionConfig;
import com.linyun.common.entity.UserNickName;
import com.linyun.common.utils.ConstantsUtils;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.CommonMapper;
import com.linyun.data.mapper.UserMapper;
import com.linyun.data.server.ActionAware;

public class CommonActionImpl  extends ActionAware implements CommonAction
{	
	//绑定邀请码送钻石
	@Override
	public GameConfig getBindRefCodePresentDiamond()
	{ 
		GameConfig gc = RedisResource.get(ConstantsUtils.INVITE_CODE_DIAMOND_INDEX);
		if (gc == null)
		{
			
			SqlSession session = null;
			try
			{
				session = SqlSessionFactoryUtil.ssf.openSession();
				CommonMapper commonMapper = session.getMapper(CommonMapper.class);
				gc = commonMapper.getBindRefCodePresentDiamond();
				if (gc != null)
				{
					RedisResource.set(ConstantsUtils.INVITE_CODE_DIAMOND_INDEX, gc);
				}
			}
			finally
			{
				if (session != null)
				{
					session.close();
				}
			}
		}
		return gc;
	}

	//拉取公告内容
	@Override
	public Notice getNoticeContent()
	{
		Notice notice = RedisResource.get(ConstantsUtils.NOTICE_INDEX);
		if (notice == null)
		{
			SqlSession session = null;
			try
			{
				session = SqlSessionFactoryUtil.ssf.openSession();
				CommonMapper commonMapper = session.getMapper(CommonMapper.class);
				notice = commonMapper.getNoticeContent();
				if (notice != null)
				{
					RedisResource.set(ConstantsUtils.NOTICE_INDEX, notice);
				}
			}
			finally
			{
				if (session != null)
				{
					session.close();
				}
			}
		}
		
		return notice;
	}
	
	@Override
	public List<GiftCode> getGiftCodeBindInfoBySql()
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			CommonMapper commonMapper = session.getMapper(CommonMapper.class);
			List<GiftCode> list = commonMapper.getAllGiftCode();
			return list;
		}
		finally
		{
			if (session != null)
			{
				session.close();
			}
		}
	}
	
	//拉取跑马灯
	@Override
	public List<Marquee> getMarqueeList() 
	{   
		List<Marquee> list = RedisResource.get(ConstantsUtils.MARQUEE_INDEX);
		if (list == null || list.isEmpty())
		{
			SqlSession session = null;
			try
			{
				session = SqlSessionFactoryUtil.ssf.openSession();
				CommonMapper commonMapper = session.getMapper(CommonMapper.class);
				list = commonMapper.getMarqueeList(ConstantsUtils.STATUS_ON);
				if (list!=null && !list.isEmpty())
				{
					RedisResource.set(ConstantsUtils.MARQUEE_INDEX, list);
				}
			}
			finally
			{
				if (session != null)
				{
					session.close();
				}
			}
		}
		return list;
	}

	//拉取邀请码,玩家邀请码也就是玩家的id
	@Override
	public Set<Integer> getInviteCodeList()
	{   
		Set<Integer> set = RedisResource.get(ConstantsUtils.INVITE_CODE_INDEX);
		if ((set == null) || (set.isEmpty()))
		{
			SqlSession session = null;
			try
			{
				session = SqlSessionFactoryUtil.ssf.openSession();
				UserMapper userMapper = session.getMapper(UserMapper.class);
				set = userMapper.getAllUserIdList();
				if (set!=null && !set.isEmpty())
				{
					RedisResource.set(ConstantsUtils.INVITE_CODE_INDEX, set);
				}
			}
			finally
			{
				if (session != null)
				{
					session.close();
				}
			}
		}
		return set;
	}

	@Override
	public List<UserNickName> getUserNickName() 
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			CommonMapper commonMapper = session.getMapper(CommonMapper.class);
			List<UserNickName> nickNameList = commonMapper.getUserNickName();
			return nickNameList;
		}
		finally
		{
			if (session != null)
			{
				session.close();
			}
		}
	}

	@Override
	public CustomService getCustomServiceInfo()
	{
		CustomService service = RedisResource.get(ConstantsUtils.CUSTOM_SERVICE_INDEX);
		if (service == null)
		{
			SqlSession session = null;
			try
			{
				session = SqlSessionFactoryUtil.ssf.openSession();
				CommonMapper commonMapper = session.getMapper(CommonMapper.class);
				service = commonMapper.getCustomServiceInfo();
				if (service != null)
				{
					RedisResource.set(ConstantsUtils.CUSTOM_SERVICE_INDEX, service);
				}
			}
			finally
			{
				if (session != null)
				{
					session.close();
				}
			}
		}
		return service;
	}

	@Override
	public void updateNoticeContent(String content,int id)
	{   
		SqlSession session = null ;
        try
        {
		    session = SqlSessionFactoryUtil.ssf.openSession();
		    CommonMapper commonMapper = session.getMapper(CommonMapper.class);
		    Notice notice = RedisResource.get(ConstantsUtils.NOTICE_INDEX);
		    if(notice == null)
		    {
		    	notice = commonMapper.getNoticeContent();
		    }
        	notice.setContent(content);
        	Map<String,Object> paramMap = new HashMap<String,Object>();
        	paramMap.put("id",id);
        	paramMap.put("content",content);
         	commonMapper.updateNoticeContent(paramMap);
         	RedisResource.set(ConstantsUtils.NOTICE_INDEX, notice);
         	session.commit();
		}
        finally
        {
        	if (session != null)
			{
				session.close();
			}
        }
		
	}

	@Override
	public void updateMarquee(int id, String content, int voild)
	{   
		SqlSession session = null ;
		List<Marquee> list = null ;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			CommonMapper commonMapper = session.getMapper(CommonMapper.class);
			
			//最少保留一条跑马灯是启动状态
			list = commonMapper.getMarqueeList(ConstantsUtils.STATUS_ON);
			if(list.size() == 1 && voild == ConstantsUtils.STATUS_OFF)
			{
				throw new GameException(GameException.MARQUEE_ABLE_REMAINING_ONE,"更改跑马灯时，最少保留一条是启动状态");
			}
			Marquee marquee = commonMapper.getMarqueeById(id);
			marquee.setContent(content);
			marquee.setVoild(voild);
			commonMapper.updateMarquee(marquee);
			session.commit();
			//查询所有没有禁用的
			list = commonMapper.getMarqueeList(ConstantsUtils.STATUS_ON);
			RedisResource.set(ConstantsUtils.MARQUEE_INDEX, list);
		}
		finally
		{
			if (session != null)
			{
				session.close();
			}
		}
		
	}

	@Override
	public List<FieldConfig> selectAllFieldConfig() 
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			CommonMapper commonMapper = session.getMapper(CommonMapper.class);
			List<FieldConfig> fieldConfigs = commonMapper.selectAllFieldConfig();
			return fieldConfigs;
		}
		finally
		{
			if (session != null)
			{
				session.close();
			}
		}
	}

	@Override
	public List<SectionConfig> selectAllSectionConfig() 
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			CommonMapper commonMapper = session.getMapper(CommonMapper.class);
			List<SectionConfig> fieldConfigs = commonMapper.selectAllSectionConfig();
			return fieldConfigs;
		}
		finally
		{
			if (session != null)
			{
				session.close();
			}
		}
	}

	@Override
	public List<PayConfig> selectAllPayConfig() 
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			CommonMapper commonMapper = session.getMapper(CommonMapper.class);
			List<PayConfig> payConfigs = commonMapper.selectAllPayConfig();
			return payConfigs;
		}finally
		{
			if(session != null)
			{
				session.close();
			}
		}
	}

	@Override
	public void updateFieldConfig(FieldConfig f) 
	{

		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			CommonMapper commonMapper = session.getMapper(CommonMapper.class);
			commonMapper.updateFieldConfig(f);
			session.commit();
			
		}finally
		{
			if(session != null)
			{
				session.close();
			}
		}
		
	}

	
}
