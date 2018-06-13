package com.linyun.data.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;

import com.linyun.bottom.log.LoggerFactory;
import com.linyun.data.server.ServerBootstrap;


public class SqlSessionFactoryUtil {
      
	  private static Logger logger = LoggerFactory.getLogger(SqlSessionFactoryUtil.class);
	  public static SqlSessionFactory ssf ;
	  
	  public static SqlSessionFactory getInstance()
	  {
		  try
		  {  
			 InputStream resourceAsStream = new FileInputStream(ServerBootstrap.ROOT_DIR + File.separator + "config/mybatis-config.xml");
			 if(ssf == null)
			 {
				 ssf =new SqlSessionFactoryBuilder().build(resourceAsStream);
			 }
			  
			  
		  } catch (Exception e) {
			  logger.info("读取mybatis配置信息为null",e);
		  }
		  
		  return ssf ;
	  }
}
