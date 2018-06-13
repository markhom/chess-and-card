package com.linyun.common.action;

import java.util.List;

import com.linyun.common.entity.ClubRoomLog;

/**
*  @Author walker
*  @Since 2018年4月14日
**/

public interface ClubRoomLogAction 
{

    public void addGameRoomLog(ClubRoomLog c);
    
    public List<ClubRoomLog> getOnlyRoomNumInfourDays();
    
    public void updatePlayedRound(int roomNum,int playedRound);
    
    ClubRoomLog getClubRoomLogByRoomId( int roomNum);
    
}
