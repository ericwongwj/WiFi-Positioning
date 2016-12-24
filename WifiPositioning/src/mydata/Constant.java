package mydata;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constant {
	public final static String AP_REGEX="\\w\\w:\\w\\w:\\w\\w:"
      		+ "\\w\\w:\\w\\w:\\w\\w:";
	public final static String RSS_REGEX="(.*):(.*?)dBm";
	public final static String POS_REGEX=".txt";
	public final static String TIME_REGEX="time:";
	
	static final String OFFPATH="F:\\Lab\\2thfloorv4\\offrss.txt";
	static final String ONPATH="F:\\Lab\\2thfloorv4\\onrssv1.txt";
	static final String APLISTPATH="F:\\Lab\\2thfloorv4\\aplist.txt";
	
	static String[] offtxts={
			"000.txt","002.txt","020.txt","022.txt","040.txt","042.txt","060.txt",
			"062.txt","080.txt","082.txt","100.txt","102.txt","120.txt","122.txt",
	};
	
	static String[] onPos={
		"1,1","3,1","5,1","7,1","9,1","11,1"
	};

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
