package mydata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class OnlineData {
	/**
	 * 此组织形式只是在pc上跑数据集用 实际应用应该改为单次的
	 */
	private BufferedReader br;
	
	ArrayList<ArrayList<Map<String, Double>>> onRssList=new ArrayList<>();
	ArrayList<Map<String, Double>> avgRssList=new ArrayList<>();
	ArrayList <String> aplist=new ArrayList<>();
	
	public Double[] Xlist={1.0,3.0,5.0,7.0,9.0,11.0};
	public Double[] Ylist={1.0,1.0,1.0,1.0,1.0,1.0};

	private static double availableRSS=-80.0;
	public int number=Xlist.length;
	
	public OnlineData(String path) {
		Tools.initAP(aplist);
		initBr(path);
		initRSSData();
//		Tools.displayAllRSS(onRssList, aplist);
		calculateOnAvgRss();
	}

	/**
	 * eachTimeRss中只put了能侦测到的ap以及对应的rss
	 */
	public void initRSSData(){
		try {
			String line;
			int cnt=0,times=0;
			Map<String,Double> eachTimeRss = null;
			ArrayList<Map<String, Double>> eachPosRss = null;
			while((line=br.readLine())!=null){
				Matcher pm=Constant.pos_pattern.matcher(line);
				Matcher rm=Constant.rss_pattern.matcher(line);
				Matcher tm=Constant.time_pattern.matcher(line);
				if(pm.find()){//位置
					eachPosRss=new ArrayList<>(25);
					onRssList.add(eachPosRss);
					cnt++;times=0;
				}else if(tm.find()){//次数 time:
					eachTimeRss=new HashMap<>();
					eachPosRss.add(eachTimeRss);
					times++;
				}else if(rm.find()){//RSS
//					if(!aplist.contains(rm.group(1)))//不要考虑了吧
//						aplist.add(rm.group(1));
					eachTimeRss.put(rm.group(1), Double.valueOf(rm.group(2)));
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void calculateOnAvgRss(){
		int []count=new int [aplist.size()];
		double []sum=new double[aplist.size()];int t=0;
		for(ArrayList<Map<String,Double>> eachpos : onRssList){
//			System.out.println(Constant.onPos[t++]);
			Map<String,Double> eachavgrss=new HashMap<>();
			avgRssList.add(eachavgrss);
			for(Map<String,Double> eachtime : eachpos){
				for(int i=0;i<aplist.size();i++){
					String ap=aplist.get(i);
					if(eachtime.get(ap)!=null){
						sum[i]+=eachtime.get(ap);
						count[i]++;
					}
				}
			}
			for(int j=0;j<aplist.size();j++){
				if(count[j]!=0){
					double avg= sum[j]/count[j];
					eachavgrss.put(aplist.get(j), avg);
//					System.out.println(aplist.get(j)+" "+avg);
				}
			}
			Tools.cleanArr(count, sum);
		}
	}


	public static void main(String[] args) {
		OnlineData od=new OnlineData(Constant.ONPATH);			
//		Tools.showList(od.aplist);
	}
	
	private void initBr(String path){
		try {
			FileInputStream fis = new FileInputStream(new File(path));
			InputStreamReader isr=new InputStreamReader(fis);
			br=new BufferedReader(isr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
