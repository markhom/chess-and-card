package com.linyun.common.action.impl;

import org.apache.ibatis.session.SqlSession;

import com.linyun.common.action.ClubDiamondLogAction;
import com.linyun.common.entity.ClubDiamondLog;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.ClubDiamondLogMapper;

public class ClubDiamondLogActionImpl implements ClubDiamondLogAction {

	@Override
	public void addOneRecordwithCostDiamond(ClubDiamondLog cdl)
	{

		SqlSession session = null ;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			ClubDiamondLogMapper mapper = session.getMapper(ClubDiamondLogMapper.class);
			mapper.addOneRecordwithCostDiamond(cdl);
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

}
