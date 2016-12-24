package com.example.eric.mywifi.Locating;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constant {
	public final static String AP_REGEX="\\w\\w:\\w\\w:\\w\\w:"
      		+ "\\w\\w:\\w\\w:\\w\\w:";
	public final static String RSS_REGEX="(.*):(.*?)dBm";
	public final static String POS_REGEX=".txt";
	public final static String TIME_REGEX="time:";
	
	static String[] offtxts={
			"0000.txt","0002.txt","0200.txt","0202.txt","0400.txt","0402.txt","0600.txt","0602.txt","0800.txt","0802.txt",
			"1000.txt","1002.txt","1200.txt","1202.txt","1400.txt","1402.txt","1600.txt","1602.txt","1800.txt","1802.txt",
			"2000.txt","2002.txt","2200.txt","2202.txt","2400.txt","2402.txt","2600.txt","2602.txt","2800.txt","2802.txt",
			"3000.txt","3002.txt","3200.txt","3202.txt","3400.txt","3402.txt","3600.txt","3602.txt","3800.txt","3802.txt",
			"4000.txt","4002.txt","4200.txt","4202.txt"
	};
	
	static String[] onPos={
		"1,1","3,1","5,1","7,1","9,1",
		"11,1","13,1","15,1","17,1","19,1",
		"21,1","23,1","25,1","27,1","29,1",
		"31,1","33,1","35,1","37,1","39,1","41,1"
	};

	public static Double[] posXlist={
			0.0,0.0,2.0,2.0,4.0,4.0,6.0,6.0,8.0,8.0,
			10.0,10.0,12.0,12.0,14.0,14.0,16.0,16.0,18.0,18.0,
			20.0,20.0,22.0,22.0,24.0,24.0,26.0,26.0,28.0,28.0,
			30.0,30.0,32.0,32.0,34.0,34.0,36.0,36.0,38.0,38.0,
			40.0,40.0,42.0,42.0,};
	public static Double[] posYlist={
			0.0,2.0,0.0,2.0,0.0,2.0,0.0,2.0,0.0,2.0,
			0.0,2.0,0.0,2.0,0.0,2.0,0.0,2.0,0.0,2.0,
			0.0,2.0,0.0,2.0,0.0,2.0,0.0,2.0,0.0,2.0,
			0.0,2.0,0.0,2.0,0.0,2.0,0.0,2.0,0.0,2.0,
			0.0,2.0,0.0,2.0,0.0};

	static Pattern ap_pattern=Pattern.compile(Constant.AP_REGEX);
	static Pattern rss_pattern=Pattern.compile(Constant.RSS_REGEX);
	static Pattern pos_pattern=Pattern.compile(Constant.POS_REGEX);
	static Pattern time_pattern=Pattern.compile(Constant.TIME_REGEX);
	
	public static void main(String[] args) {
		String test="a8:15:4d:59:14:d6:-66dBm";
		Pattern p=Pattern.compile(RSS_REGEX);
		Matcher m=p.matcher(test);
		if(m.find())
			System.out.println(m.group(1)+" "+m.group(2));
	}
	
	
}
