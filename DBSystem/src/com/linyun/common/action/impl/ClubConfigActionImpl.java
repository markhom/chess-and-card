package com.linyun.common.action.impl;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.linyun.common.action.ClubConfigAction;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.ClubConfigMapper;

public class ClubConfigActionImpl implements ClubConfigAction {

	@Override
	public void updateClubConfigByType(int count, int type)
	{
        SqlSession session = null ;
        try
        {
        	session = SqlSessionFactoryUtil.ssf.openSession();
        	ClubConfigMapper ccMapper = session.getMapper(ClubConfigMapper.class);
        	ccMapper.updateClubConfigByType(count,type);
        	session.commit();
		} 
        finally
        {
        	if(session != null)
        	{
        		session.close();
        	}
        }
	}

	@Override
	public List<Integer> getValueFromConfig()
	{
		SqlSession session = null ;
		List<Integer> values = null ;
        try
        {
        	session = SqlSessionFactoryUtil.ssf.openSession();
        	ClubConfigMapper ccMapper = session.getMapper(ClubConfigMapper.class);
            values = ccMapper.getValueFromConfig();
		} 
        finally
        {
        	if(session != null)
        	{
        		session.close();
        	}
        }
		return values;
	}

	

}
