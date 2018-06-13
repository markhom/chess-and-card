package com.linyun.common.utils;

import java.util.Random;

public class IndexUtil {
       
	 private static final int defalut_length = 3 ;
	 public static IndexUtil indexUtil  = new IndexUtil();
	 public static final char[] arr = {
		'0','1','2','3','4','5',
		'6','7','8','9','A','B',
		'C','D','E','F','G','H',
		'I','J','K','L','M','N',
		'O','P','Q','R','S','T',
		'U','V','W','X','Y','Z'
	 } ;
	 
	 public static IndexUtil getInstance()
	 {
		 return indexUtil ;
	 }
	
	 //生成36进制的方法
	 public String getLongConversion(long number)
	 {    
		 if(number < 0)
		 {
			 number = - number ;
		 }
		  String str = Long.toString(number,36).toUpperCase();
		  return str ;
	 }
	 
	 //生成不同局数的long值   round-表示当前局数  roomNum--房间号
	 public String[] getDiffRoundIndex(int round, int roomNum)
	 {   
		 //当前时间hashcode值的10倍
		 long cur = System.currentTimeMillis();
		 int hc = String.valueOf(cur).hashCode() ;
		 long time = hc*10 ;
		 //房间号和局数的乘积的10倍
		 int rn = roomNum;
		 String strPrefix = getIndexPrefix() ;
		 //将生成的全局索引放进数组
		 String[] arrayIndex = new String[round] ;
		 for (int i=1; i<arrayIndex.length+1; ++i)
		 {
			 long number = i*rn*10 ;
			 String str = getLongConversion(time + number);
			 String ret =  strPrefix + str ;
			 System.out.println(ret);
			 arrayIndex[i-1] = ret;
		 }
		 
		 return arrayIndex;
	 }
	 
	 //前缀的生成方法,生成3个
	 public String generalPrefix(int length)
	 {
		 Random random = new Random();
		 StringBuffer sb = new StringBuffer();
		 for(int i = 0 ; i < length ;i++)
		 {
			 int index = random.nextInt(arr.length) ;
			 sb.append(arr[index]);
		 }
		 return sb.toString() ;
	 }
	 
	 //对前缀String进行排序
	 public String getSortString(String str)
	 {
		 StringBuffer numberString = new StringBuffer();
		 StringBuffer letterString = new StringBuffer();
		 char[] chars = str.toCharArray();
		 for (char c : chars)
		 {
			 if(c >= '0' && c <= '9')
			 {
				 numberString.append(c); 
			 }
			 else
			 {
				 letterString.append(c) ;
			 }
		 }
		 
		 return  letterString.toString()+numberString.toString() ;
	 }
	 
	 //生成整个索引前缀的字母在前的排序
	 public String getIndexPrefix()
	 {
		return getSortString(generalPrefix(defalut_length));
	 }
	 
	 public static void main(String[] args)
	 {
		 IndexUtil.getInstance().getDiffRoundIndex(10 , 775986);
//		 System.out.println(IndexUtil.getInstance().getLongConversion(new Date().));
//		 System.out.println(IndexUtil.getInstance().getLongConversion(707806));
	 }
	    
}
