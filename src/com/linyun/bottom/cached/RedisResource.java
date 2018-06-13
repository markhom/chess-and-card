package com.linyun.bottom.cached;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

import com.linyun.bottom.log.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

public class RedisResource {
	private static Logger logger = LoggerFactory.getLogger(RedisResource.class);
	
	private static JedisPool pool ;
	private final static String JEDIS_PASSWORD = "barracat170701";
	public static final int REDIS_KEY_TIMEOUT_SECONDS = 60*60*24;//redis存储的信息的超时时间，单位是秒
	public static final int REDIS_CACHED_DB_INDEX = 6;//我们的缓存数据使用的redis的数据库的索引值
	public static final int REDIS_CACHED_DB_DIVIDE = 7 ; //redis分库,好友房拉取房间列表
	public static final int REDIS_CACHES_GET_AII_CLUBS = 8;//拉取玩家个人俱乐部列表
	private static final String USER_ID_PREFIX = "data_user_id" ;
	private static final String ROOM_NUM_PREFIX = "data_room_num" ;
	private static final String CLUB_PREFIX = "club_data";
	
	public static void getInstance()
	{   
		try 
		{
			JedisPoolConfig config = new JedisPoolConfig();
		     config.setMaxTotal(1000);
		     config.setMaxIdle(256);
		     config.setMinIdle(32);
		     config.setMaxWaitMillis(5000L);
		     config.setTestOnBorrow(true);
		     config.setTestOnReturn(true);
		     config.setTestWhileIdle(true);
		     config.setMinEvictableIdleTimeMillis(60000L);
		     config.setTimeBetweenEvictionRunsMillis(3000L);
		     config.setNumTestsPerEvictionRun(-1);
		     
			if(pool == null)
			{
				 pool = new JedisPool(config ,"localhost", 6379, 60000, JEDIS_PASSWORD);
			}
		} 
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		
	}
	
	public static String generateUserId(String userId)
	{
		return CLUB_PREFIX +"_"+userId ;
	}
	public static String generateId(String userId)
	{
		return USER_ID_PREFIX + "_" + userId ;
	}
	
	public static String generateRoomNumId(String roomNum)
	{
		return ROOM_NUM_PREFIX + "_" + roomNum ;
	}
	
	//
	public static <T> void set (String key , T t)
	{
		Jedis jedis = null ;
		try
		{
			jedis = pool.getResource();
			jedis.select(REDIS_CACHED_DB_INDEX);
			jedis.setex(key.getBytes(), REDIS_KEY_TIMEOUT_SECONDS, Transcoder(t)) ;
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{
			if(null != jedis)
			{
				jedis.close();
			}
		}
	}
	
	public static <T> T get(String key)
	{
		Jedis jedis = null ;
		T t = null ;
		try
		{
			jedis = pool.getResource();
			jedis.select(REDIS_CACHED_DB_INDEX);
			byte[] bs = jedis.get(key.getBytes());
			t = Resolver(bs);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{
			if(null != jedis)
			{
				jedis.close();
			}
		}
		return t ;
	}
	
	public static <T> void del(String key)
	{
		Jedis jedis = null ;
		try
		{
			jedis = pool.getResource();
			jedis.select(REDIS_CACHED_DB_INDEX);
			jedis.del(key.getBytes());
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{
			if(null != jedis)
			{
				jedis.close();
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public static <T> T Resolver(byte[] bytes) throws IOException
	{
		ByteArrayInputStream bos = null ;
		ObjectInputStream os = null ;
		T t = null ;
		try
		{
			if(bytes != null)
			{
				bos = new ByteArrayInputStream(bytes);
				os = new ObjectInputStream(bos) ;
				Object readObject = os.readObject(); 
				t = (T) readObject ;
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{
			if(null != os)
			{
				os.close();
			}
			if(null != bos)
			{
				bos.close();
			}
		}
		return  t ;
	}
	
	public static <T> byte[] Transcoder(T t) throws IOException
	{   
		 byte[] bytes = null ;
		 ByteArrayOutputStream bos = null;  
         ObjectOutputStream os = null;  
         try
         {  
             bos = new ByteArrayOutputStream();  
             os = new ObjectOutputStream(bos); 
             if(t != null)
             {
            	 os.writeObject(t);
            	 bytes = bos.toByteArray(); 
             }
             
         }
		catch(Exception e)
		{
			logger.error(e.getMessage(), e);
		}
        finally
        {    
        	if(null != os)
            {
        		os.close();  
            }
        	if(null != bos)
        	{
        		bos.close();  
        	}
		}
		return bytes ;
	}
	
	public static void remove(String key) 
	{   
		Jedis jedis = null ;
		try
		{   
			jedis = pool.getResource();
			jedis.select(REDIS_CACHED_DB_INDEX);
			jedis.del(key);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{
			close(jedis);
		}
	}

	private static void close(Jedis jedis)
	{
		if (jedis != null)
		{
			jedis.close();
		}
	}

	//创建房间，插入redis的data
	public static void createRoomSetData(String userId,String roomNum)
	{
		 Jedis jedis = null ;
		 Transaction tx = null;
		 try
		 {   
			 jedis = pool.getResource();
			 jedis.select(REDIS_CACHED_DB_DIVIDE);
			 tx = jedis.multi();
			 //redis.hash
			 tx.hset(generateId(userId), roomNum, String.valueOf(System.currentTimeMillis())); 
			 //redis.set the only name
			 String setNameByOnly = userId+System.currentTimeMillis() ;
			 //reids.set
			 tx.set(generateRoomNumId(roomNum), setNameByOnly);
			 tx.sadd(setNameByOnly, userId);
			 tx.exec();
			 logger.info("玩家:userId="+userId+"创建房间:"+roomNum+"，玩家数据插入redis success");
		 } 
		 catch (Exception e) 
		 {
			 logger.error(e.getMessage(), e);
		 }
		 finally
		 {  
		    if(null != tx)
		    {
		    	tx.close();
		    }
			if(null != jedis)
			{
				jedis.close();
			}
		 }
	}

	//-----------------------------hash---------------------------------------
	public static void hset(byte[] key, byte[] field, byte[] value) 
	{
		Jedis jedis = null;
		try 
		{
			jedis = pool.getResource();
			jedis.hset(key, field, value);
		} 
		catch (Exception e) 
		{	            
			e.printStackTrace();
		}
		finally 
		{
			//返还到连接池
			close(jedis);
		}
	}

	public static void hset(String key, String field, String value) 
	{
		Jedis jedis = null;
		try
		{
			jedis = pool.getResource();
			jedis.hset(key, field, value);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			//返还到连接池
			close(jedis);
		}
	}

	//加入房间时，插入房间的data
	public static void joinRoomSetData(String userId , String roomNum)
	{    
		 Jedis jedis = null ;
		 Transaction tx = null ;
		 try
		 {   
			 jedis = pool.getResource();
			 jedis.select(REDIS_CACHED_DB_DIVIDE);
			 String userIdSet = jedis.get(generateRoomNumId(roomNum)); 
			 //begin Transaction
			 tx = jedis.multi();
			 //redis.hash
			 tx.hset(generateId(userId), roomNum, String.valueOf(System.currentTimeMillis()));
			 //redis.set
			 tx.sadd(userIdSet, userId);
			 tx.exec();
			 logger.info("玩家userId="+userId+"加入房间："+roomNum+"玩家数据插入redis success");
		 }
		 catch (Exception e)
		 {
			 logger.error(e.getMessage(), e);
		 }
		 finally 
		 {   
			if(null != tx)
		    {
		    	tx.close();
		    }
			if(null != jedis)
			{
				jedis.close();
			}
		 }
	}
	
	
	//拉取玩家的房间列表集合
	public static Map<String,String> getDataFromRedis(String userId)
	{
		Jedis jedis = null ;
		Map<String, String> roomNumTimeMap = null ;
		try
		{
			jedis = pool.getResource();
			jedis.select(REDIS_CACHED_DB_DIVIDE);
			roomNumTimeMap = jedis.hgetAll(generateId(userId));
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{
			if(null != jedis)
			{
				jedis.close();
			}
		}
		return roomNumTimeMap ;
	}
	
	//房间解散，删除数据
	public static void deleteDataFromRedis(String roomNum)
	{   
		Jedis jedis = null ;
		Response<Set<String>> repsonse = null ;
		Transaction tx = null ;
		try 
		{    
			 jedis = pool.getResource();
			 jedis.select(REDIS_CACHED_DB_DIVIDE);
			 repsonse = selectUserIdListFromSetDissolutionRoom(roomNum,jedis);
			 Set<String> userIdSet = repsonse.get();
			 //delete redis.hash
			 String setOnlyName = jedis.get(generateRoomNumId(roomNum));
			 tx = jedis.multi();
			 for (String userId : userIdSet)
			 {
				 tx.hdel(generateId(userId),roomNum);
			 }
			 //delete redis set
			 tx.del(setOnlyName);
			 //delete redis  key-->roomNum  value --->userIdList
			 tx.del(generateRoomNumId(roomNum));
			 tx.exec();
			 logger.info("房间roomNum="+roomNum+"解散，redis中该房间内玩家数据，清除success!");
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{   
			if(null != tx)
		    {
		    	tx.close();
		    }
			if(null != jedis)
			{
				jedis.close();
			}
		}
	}
	
	
	public static Response<Set<String>> selectUserIdListFromSetDissolutionRoom(String roomNum,Jedis jedis)
	{   
		Pipeline pipelined = null ;
		Response<Set<String>> responseSet = null ;
		try
		{  
			pipelined = jedis.pipelined(); 
			String set = jedis.get(generateRoomNumId(roomNum)); 
			//开启链接的管道，多条命令开启一次链接
			responseSet = pipelined.smembers(set);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{
			if(null != pipelined)
			{
				pipelined.close();
			}
		}
		return responseSet ;
	}
	
	
	 
	public static String hget(String key, String field) 
	{
		String value = null;
		Jedis jedis = null;
		try 
		{
			jedis = pool.getResource();
			value = jedis.hget(key, field);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		finally 
		{
			close(jedis);
		}

		return value;
	}

	public static byte[] hget(byte[] key, byte[] field) 
	{
		byte[] value = null;
		Jedis jedis = null;
		try 
		{
			jedis = pool.getResource();
			value = jedis.hget(key, field);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		} 
		finally
		{
			close(jedis);
		}

		return value;
	}

	public static void hdel(byte[] key, byte[] field)
	{
		Jedis jedis = null;
		try 
		{
			jedis = pool.getResource();
			jedis.hdel(key, field);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			close(jedis);
		}
	}
	
	public static Map<String, String> hgetAll(String key)
	{
		Map<String, String> map = null;
		Jedis jedis = null;
		try 
		{
			jedis = pool.getResource();
			map = jedis.hgetAll(key);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			close(jedis);
		}
		return map;
	}
	
	public static Map<byte[], byte[]> hgetAll(byte[] key)
	{
		Map<byte[], byte[]> map = null;
		Jedis jedis = null;
		try 
		{
			jedis = pool.getResource();
			map = jedis.hgetAll(key);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			close(jedis);
		}
		return map;
	}
	
	 /*** set start */
    public static void sadd(byte[] key, byte[] ... valueList)
    {
    	 Jedis jedis = null;
         try 
         {
        	 if (valueList.length > 0)
        	 {
	             jedis = pool.getResource(); 
	             jedis.sadd(key, valueList);
        	 }
         }
         catch (Exception e) 
         {
         	e.printStackTrace();
         } 
         finally
         {
             //返还到连接池
             close(jedis);
         }
    }
	//-----------------------------hash---end------------------------------------
	public static Jedis getJedis()
	{
		return pool.getResource();
	}
	
	
	/*************************************玩家个人所有的俱乐部redis数据***********************************/
	
	//玩家成功加入俱乐部，数据写入缓存
	public static void setUserClubData(String userId,String clubId)
	{
		Jedis jedis = null;
		try
		{   
			jedis = pool.getResource();
			jedis.select(REDIS_CACHES_GET_AII_CLUBS);
			jedis.sadd(generateUserId(userId), clubId);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			close(jedis);
		}
	}
	   //进入俱乐部大厅，刷新缓存
		public static void flushUserClubData(String userId,List<String> clubIds)
		{
			Jedis jedis = null;
			try
			{   
				jedis = pool.getResource();
				jedis.select(REDIS_CACHES_GET_AII_CLUBS);
				String[] arr = new String[clubIds.size()];
				jedis.sadd(generateUserId(userId), clubIds.toArray(arr));
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			finally 
			{
				close(jedis);
			}
		}
	
	
	//获取玩家所有的私人俱乐部
	public static Set<String> getAllClubs(String userId)
	{
		Jedis jedis = null ;
		Set<String> clubIdMap = null ;
		try
		{
			jedis = pool.getResource();
			jedis.select(REDIS_CACHES_GET_AII_CLUBS);
			clubIdMap = jedis.smembers(generateUserId(userId));
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{
			if(null != jedis)
			{
				jedis.close();
			}
		}
		return clubIdMap ;
	}
	
	//玩家退出或被踢出俱乐部
	public static void delUserClubInfo(String userId,String clubId)
	{
		Jedis jedis = null;
		try
		{   
			jedis = pool.getResource();
			jedis.select(REDIS_CACHES_GET_AII_CLUBS);
			jedis.srem(generateUserId(userId), clubId);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			close(jedis);
		}
	}
	
	
} 
