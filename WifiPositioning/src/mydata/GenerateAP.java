package mydata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateAP {
	
	private static ArrayList<String> aplist =new ArrayList<>();

	public static void main(String[] args) {
		try {
			Pattern rss_pattern=Pattern.compile(Constant.RSS_REGEX);
			Pattern pos_pattern=Pattern.compile(Constant.POS_REGEX);
			
			File file=new File(Constant.OFFPATH);
			BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line;
			while((line=br.readLine())!=null){
				Matcher rm=rss_pattern.matcher(line);
				Matcher pm=pos_pattern.matcher(line);
				if(pm.find())
					System.out.println(line);
				
//				System.out.println(line);
				if(rm.find()){//RSS
					if(!aplist.contains(rm.group(1)))
						aplist.add(rm.group(1));
				}
			}
			showList(aplist);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static void showList(List<String> list){
		System.out.println("size="+list.size());
		for(int i=0;i<list.size();i++){
			System.out.println(list.get(i));
		}
	}
}
