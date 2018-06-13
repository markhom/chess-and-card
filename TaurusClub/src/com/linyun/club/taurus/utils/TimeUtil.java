package com.linyun.club.taurus.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
	
	  public static long getInit()
	  {
		  Calendar c = Calendar.getInstance();
		  c.set(Calendar.HOUR_OF_DAY,24);
		  c.set(Calendar.MINUTE,0);
		  c.set(Calendar.SECOND,0);
		  c.set(Calendar.MILLISECOND,0);
		  //获取第二天凌晨的时间戳
		  long curTime = c.getTimeInMillis();  
		  return curTime ;
	  }
	  
	  public static String formatDate(Timestamp t)
	  {
		  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		  String format2 = format.format(new Date(t.getTime()));
		  return format2;
	  }
	  
}
