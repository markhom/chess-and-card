package com.linyun.common.taurus.eum;

/**
*  @Author walker
*  @Since 2018年5月23日
**/

public enum RoomType 
{
   common(1),
   senior(2),
   vip(3);
	
   public int value;
   
   private RoomType(int _value)
   {
	   this.value = _value;
   }
   
   public static RoomType valueOf(int value)
   {
	   switch(value)
	   {
	      case 1: return common;
	      case 2: return senior;
	      case 3: return vip;
	      default:return common;
	   }
   }
}
