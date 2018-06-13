package com.linyun.bottom.util;

public class MemLeakCheck
{
	public static String ServerName = "";
	public static long start = 0;

	public static void Init(String Name)
	{
		ServerName = Name;
	}

	public static long StartCheck()
	{
		start = Runtime.getRuntime().totalMemory()
				- Runtime.getRuntime().freeMemory();
		return start;
	}

	public static float EndCheck()
	{
		long end = Runtime.getRuntime().totalMemory()
				- Runtime.getRuntime().freeMemory();
		float dif = (end - start) / (1024.0f * 1024.0f);

		return dif;
	}
}
