package com.linyun.common.action.impl;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.linyun.common.action.TaurusRoundLogAction;
import com.linyun.common.entity.TaurusRoundLog;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.TaurusRoundLogMapper;
import com.linyun.data.server.ActionAware;

public class TaurusRoundLogActionImpl extends ActionAware implements TaurusRoundLogAction {
 
	@Override
	public void addOneRoundRecord(TaurusRoundLog u)
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			TaurusRoundLogMapper handCardsMapper = session.getMapper(TaurusRoundLogMapper.class);
			handCardsMapper.addOneRoundRecord(u);
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
    
	//根据房间号得到玩家一场（10 or 20）局的局数战绩记录
	@Override
	public List<TaurusRoundLog> getUserEveryRoundHandCardLog(int roomNum)
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			TaurusRoundLogMapper roundMapper = session.getMapper(TaurusRoundLogMapper.class);
			List<TaurusRoundLog> userHandCardList = roundMapper.getUserEveryRoundHandCardLog(roomNum);
			return userHandCardList;
		}
		finally
		{
			if (session != null)
			{
				session.close();
			}
		}
	}

	//通过每局游戏的唯一索引得到一局局数记录的对象
	@Override
	public TaurusRoundLog getHandCardByRoomIndex(String roomIndex)
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			TaurusRoundLogMapper roundMapper = session.getMapper(TaurusRoundLogMapper.class);
			TaurusRoundLog trl = roundMapper.getHandCardByRoomIndex(roomIndex);
			return trl;
		}
		finally
		{
			if (session != null)
			{
				session.close();
			}
		}
	}
}
