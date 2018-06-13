package com.linyun.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.linyun.common.entity.ClubRoomLog;

/**
*  @Author walker
*  @Since 2018年4月12日
**/

public interface ClubRoomLogMapper 
{  
   //新版H5中新建俱乐部房间记录
   void addClubRoomLog(ClubRoomLog c);
   
   void updateRoomRound(@Param("roomNum") int roomNum, @Param("playedRound") int playedRound);
   
   List<ClubRoomLog> getOnlyRoomNumInfourDays();
   
   ClubRoomLog getClubRoomLogByRoomId(@Param("roomNum") int roomNum);
   
   ClubRoomLog getGameDetail(int roomNum);
}
