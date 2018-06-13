package com.linyun.middle.common.taurus.utils;

import java.util.Random;

public class Utils
{
	private static Random random = new Random();
	
	public static int getRandomInt(int n)
	{
		return random.nextInt(n);
	}
}
