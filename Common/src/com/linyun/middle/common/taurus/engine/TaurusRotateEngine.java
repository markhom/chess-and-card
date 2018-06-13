package com.linyun.middle.common.taurus.engine;

import org.apache.log4j.Logger;

import com.linyun.bottom.log.LoggerFactory;
import com.linyun.middle.common.taurus.player.TaurusPlayer;
import com.linyun.middle.common.taurus.room.TaurusRoom;
import com.linyun.middle.common.taurus.table.TaurusSeat;
import com.linyun.middle.common.taurus.table.TaurusTable;

/**
 * 轮庄牛牛
 * @author liangbingbing
 * */
public class TaurusRotateEngine extends GameEngine
{
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(TaurusRotateEngine.class);
	
	private static final int MOST_UP_BANKER_TIME = 3;//每个玩家上庄的最大次数

	public TaurusRotateEngine(TaurusTable table, TaurusRoom room)
	{
		super(table, room);
	}

	@Override
	public void sureFirstRoundBanker()
	{
		if (this.m_cur_round == 1)
		{//第一局，需要确认庄家
			for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
			{
				TaurusSeat seat = m_table.getSeat(i);
				if (!seat.isJoinGame())
				{//未加入游戏，跳过
					continue;
				}
				
				//找到庄家，开始上庄
				m_table.setCurBankerSeatNum(seat.getId());
				seat.getPlayer().setIsBanker(true);
				break;
			}
		}
	}
	
	//轮庄牛牛计算庄家
	@Override
	public void calcBanker()
	{
		final TaurusPlayer banker = m_table.getSeat(m_table.getCurBankerSeatNum()-1).getPlayer();
		banker.setUpBankerTime(banker.getUpBankerTime() + 1);
		m_table.setPrevBankerSeatNum(m_table.getCurBankerSeatNum());
		
		//----------------------进行庄家判断-------------------------
		if (banker.getUpBankerTime() == MOST_UP_BANKER_TIME)
		{
			//庄家位置上的玩家下庄
			banker.setIsBanker(false);
			//查找下一个庄家，如果所有玩家都当过庄了，则解散房间
			for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
			{
				TaurusSeat seat = m_table.getSeat(i);
				if (!seat.isJoinGame())
				{//空位置 跳过
					continue;
				}
				if (seat.getPlayer().getUpBankerTime()>0)
				{//已经当过庄家，跳过
					continue;
				}
				
				m_table.setCurBankerSeatNum(seat.getId());
				seat.getPlayer().setIsBanker(true);
				break;
			}
		}
		//-----------------------------------------------------------
	}
}