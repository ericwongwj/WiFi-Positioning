package mydata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class OfflineData {

	private File file;
	private BufferedReader br;

	ArrayList<ArrayList<Map<String, Double>>> offRssList=new ArrayList<>();
	ArrayList<Map<String, Double>> avgRssList=new ArrayList<>();
	ArrayList<Map<String, Integer>>eachPosRssCountList=new ArrayList<Map<String, Integer>>();
	ArrayList <String> aplist=new ArrayList<>();
	
	//应当在读取文件之后再填充
	public Double[] Xlist={
			0.0,0.0,2.0,2.0,4.0,4.0,6.0,6.0,
			8.0,8.0,10.0,10.0,12.0,12.0,};
	public Double[] Ylist={
			0.0,2.0,0.0,2.0,0.0,2.0,0.0,2.0,
			0.0,2.0,0.0,2.0,0.0,2.0,
			};
	
	public int number=Xlist.length;
	
	private int neglect_frequency=25;//KNN设成25偏差最小
	private int collecting_times=8;//每个采集点的采集次数
	private static double defaultRSS=-100.0;
	private static double availableRSS=-80.0;

	public void setNeglectFrequency(int f){
		neglect_frequency=f;
	}
	
	public OfflineData(String path) {
		initBufferReader(path);
		Tools.initAP(aplist);
		initRSSData();
//		Tools.displayAllRSS(offRssList, aplist);
		calculateOffAvgRss();
	}
	
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
					eachPosRss=new ArrayList<>();
					offRssList.add(eachPosRss);
					cnt++;times=0;
				}else if(tm.find()){//次数 time:
					eachTimeRss=new HashMap<>();
					eachPosRss.add(eachTimeRss);
					times++;
				}else if(rm.find()){//RSS
					if(!aplist.contains(rm.group(1)))//抛出异常说明有问题
						throw new IOException();
					eachTimeRss.put(rm.group(1), Double.valueOf(rm.group(2)));
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void calculateOffAvgRss(){
		int []count=new int [aplist.size()];
		double []sum=new double[aplist.size()];int t=0;
		for(ArrayList<Map<String,Double>> eachpos : offRssList){
//			System.out.println(Constant.offtxts[t++]);
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
					double avg=(count[j]<neglect_frequency ? defaultRSS : sum[j]/count[j]);//检测到的次数太少
					eachavgrss.put(aplist.get(j), avg);
//					System.out.println(aplist.get(j)+" "+avg);
				}
			}
			Tools.cleanArr(count, sum);
		}
	}
	
	public static void main(String[] args) {
		new OfflineData(Constant.OFFPATH);
	}
	
	private void initBufferReader(String path){
		file=new File(path);
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr=new InputStreamReader(fis);
			br=new BufferedReader(isr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
