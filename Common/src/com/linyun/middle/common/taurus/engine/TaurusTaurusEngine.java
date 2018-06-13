package com.linyun.middle.common.taurus.engine;

import java.util.ArrayList;
import java.util.List;

import com.linyun.common.taurus.eum.specCardType;
import com.linyun.middle.common.taurus.room.TaurusRoom;
import com.linyun.middle.common.taurus.table.TaurusSeat;
import com.linyun.middle.common.taurus.table.TaurusTable;
import com.linyun.middle.common.taurus.utils.TaurusEngineMathUtils;
import com.linyun.middle.common.taurus.utils.Utils;

/**
 * 牛牛上庄
 * @author liangbingbing
 * */
public class TaurusTaurusEngine extends GameEngine
{
	public TaurusTaurusEngine(TaurusTable table, TaurusRoom room)
	{
		super(table, room);
	}
	
	@Override
	public void Reset()
	{
		m_table.reset();
	}
	
	
	//牛牛上庄计算庄家
	@Override
	public void calcBanker()
	{//先将牌为牛牛的座位统计出来，再计算牛牛中 牛最大的玩家的座位id
		List<TaurusSeat> seatList = new ArrayList<TaurusSeat>();
		
		//把每局没有保座离桌状态的玩家统计出来，方便庄家保座离桌时随机取一个庄家
		List<TaurusSeat> randomList = new ArrayList<TaurusSeat>();
		for (int i=0; i<TaurusTable.TABLE_SEAT_NUM; ++i)
		{
			TaurusSeat seat = m_table.getSeat(i);
			if (!seat.isJoinGame())
			{
				continue;
			}
            
			if(!seat.isKeepSeatStage())
			{
				randomList.add(seat);
				if(seat.getCards().getSpecType() == specCardType.CARD_TYPE_ALL)
				{
					seatList.add(seat);
				}
			}
		}
		
		//记录上一局的庄家位置
		m_table.setPrevBankerSeatNum(m_table.getCurBankerSeatNum());
		
		if (!seatList.isEmpty())
		{//有牛牛
			//计算出闲家的牛牛中最大的牌,将其座位号设置为庄家
			TaurusSeat bankerSeat = seatList.get(0);
			if (seatList.size() > 1)
			{
				for (int i=1; i < seatList.size(); ++i)
				{
					TaurusSeat tempSeat = seatList.get(i);
					if (TaurusEngineMathUtils.compareTaurusTaurus(tempSeat.getCards(), bankerSeat.getCards()))
					{
						bankerSeat = tempSeat;
					}
				}
			}
			
			m_table.setCurBankerSeatNum(bankerSeat.getId());
		}else
		{
			if(m_table.getSeat(m_table.getCurBankerSeatNum()-1).isKeepSeatStage())
			{
				TaurusSeat taurusSeat = randomList.get(Utils.getRandomInt(randomList.size()));
				m_table.setCurBankerSeatNum(taurusSeat.getId());
			}
			
		}
	}
}
