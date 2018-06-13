package com.linyun.common.action.impl;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.linyun.bottom.cached.RedisResource;
import com.linyun.bottom.common.exception.GameException;
import com.linyun.common.action.PrivateRoomAction;
import com.linyun.common.entity.PrivateRoom;
import com.linyun.data.job.SqlSessionFactoryUtil;
import com.linyun.data.mapper.PrivateRoomMapper;

public class PrivateRoomActionImpl implements PrivateRoomAction 
{
	private static final String ROOM_PRIFIX = "room_create";
	
	@Override
	public void createPrivateRoom(PrivateRoom room)
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			PrivateRoomMapper roomMapper = session.getMapper(PrivateRoomMapper.class);
			roomMapper.createPrivateRoom(room);
			session.commit();
			
			RedisResource.set(generateRoomId(room.getRoomNum()), room);
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
	public PrivateRoom getPrivateRoom(int roomNum)
	{
		PrivateRoom room  = RedisResource.get(generateRoomId(roomNum));
		if (room == null)
		{
			SqlSession session = null;
			try
			{
				session = SqlSessionFactoryUtil.ssf.openSession();
				PrivateRoomMapper roomMapper = session.getMapper(PrivateRoomMapper.class);
				room = roomMapper.getPrivateRoom(roomNum);
				
				if (room != null)
				{
					RedisResource.set(generateRoomId(room.getRoomNum()), room);
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
		return room;
	}
	
	@Override
	public PrivateRoom getPrivateRoom(String roomNum)
	{
		int iroomNum = Integer.valueOf(roomNum);
		return getPrivateRoom(iroomNum);
	}

	@Override
	public void deletePrivateRoom(int roomNum)
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			PrivateRoomMapper roomMapper = session.getMapper(PrivateRoomMapper.class);
			roomMapper.deletePrivateRoom(roomNum);
			session.commit();
			
			RedisResource.del(generateRoomId(roomNum));
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
	public void startGame(int roomNum) 
	{
		PrivateRoom room = RedisResource.get(generateRoomId(roomNum));
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			PrivateRoomMapper roomMapper = session.getMapper(PrivateRoomMapper.class);
			roomMapper.startGame(roomNum);
			
			if (room == null)
			{
				room = roomMapper.getPrivateRoom(roomNum);
			}
			session.commit();
			
			room.setRoomStatus((byte)1);
			RedisResource.set(generateRoomId(roomNum), room);
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
	public List<String> getTimeoutRoomList() 
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			PrivateRoomMapper roomMapper = session.getMapper(PrivateRoomMapper.class);
			List<String> roomList = roomMapper.getTimeoutList();
			return roomList;
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
	public void addOnePlayer(int roomNum) 
	{
		PrivateRoom room = RedisResource.get(generateRoomId(roomNum));
		if (room == null)
		{
			throw new GameException(GameException.ROOM_NOT_EXIST, "房间的坐下人数加一的时候 房间不存在，房间号：" + roomNum);
		}
		
		room.setSitDownNum(room.getSitDownNum()+1);
		RedisResource.set(generateRoomId(roomNum), room);
	}

	@Override
	public void subOnePlayer(int roomNum) 
	{
		PrivateRoom room = RedisResource.get(generateRoomId(roomNum));
		if (room == null)
		{
			throw new GameException(GameException.ROOM_NOT_EXIST, "房间的坐下人数减一的时候 房间不存在，房间号：" + roomNum);
		}
		
		room.setSitDownNum(room.getSitDownNum()-1);
		RedisResource.set(generateRoomId(roomNum), room);
	}

	@Override
	public void updateRoundNum(int roomNum, int roundNum) 
	{
		PrivateRoom room = RedisResource.get(generateRoomId(roomNum));
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			PrivateRoomMapper roomMapper = session.getMapper(PrivateRoomMapper.class);
			if (room == null)
			{
				room = roomMapper.getPrivateRoom(roomNum);
			}
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("roomNum", roomNum);
			map.put("roundNum", roundNum);
			roomMapper.updateRoundNum(map);
			session.commit();
			
			room.setRoundNum((byte)roundNum);
			RedisResource.set(generateRoomId(roomNum), room);
		}
		finally
		{
			if (session != null)
			{
				session.close();
			}
		}
	}
	
	public String generateRoomId(int roomNum)
	{
		return ROOM_PRIFIX + "_" + String.valueOf(roomNum);
	}

	@Override
	public List<Integer> getClubTimeOutRoomList()
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			PrivateRoomMapper roomMapper = session.getMapper(PrivateRoomMapper.class);
			List<Integer> roomList = roomMapper.getClubTimeOutRoomList();
			return roomList;
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
	public List<PrivateRoom> getClubRoomList(int clubId)
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			PrivateRoomMapper roomMapper = session.getMapper(PrivateRoomMapper.class);
			List<PrivateRoom> roomList = roomMapper.getClubRoomList(clubId);
			return roomList;
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
	public void deleteAllRooms() 
	{
		SqlSession session = null;
		try
		{
			session = SqlSessionFactoryUtil.ssf.openSession();
			PrivateRoomMapper roomMapper = session.getMapper(PrivateRoomMapper.class);
			roomMapper.deleteAllRooms();
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
}
