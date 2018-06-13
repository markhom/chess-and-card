package com.linyun.bottom.util;

import java.util.UUID;

public class UUIDUtils 
{	
	public static String getUUID()
	{
		UUID uuid = UUID.randomUUID();
		String str = uuid.toString();
		return str;
	}
}
