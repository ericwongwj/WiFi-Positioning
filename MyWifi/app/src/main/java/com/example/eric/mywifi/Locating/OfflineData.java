package com.example.eric.mywifi.Locating;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class OfflineData {

	private BufferedReader br;

	public ArrayList<ArrayList<Map<String, Double>>> offRssList=new ArrayList<>();
    public ArrayList<Map<String, Double>> avgRssList=new ArrayList<>();
	ArrayList<Map<String, Integer>>eachPosRssCountList=new ArrayList<>();
    public ArrayList <String> aplist=new ArrayList<>();
	
	private int neglect_frequency=3;
	private int collecting_times=8;
	private static double defaultRSS=-100.0;
	private static double availableRSS=-80.0;

    String TAG="Mywifi";

	public OfflineData(ArrayList<String> aplist, InputStream rssis) {
		initBufferReader(rssis);
        this.aplist=aplist;
		initRSSData();
//		Tools.displayAllRSS(offRssList, aplist);
		calculateOffAvgRss();
	}

    private void initBufferReader(InputStream is){
        InputStreamReader isr=new InputStreamReader(is);
        br=new BufferedReader(isr);
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
				if(pm.find()){
					eachPosRss=new ArrayList<>(25);
					offRssList.add(eachPosRss);
					cnt++;times=0;
				}else if(tm.find()){
					eachTimeRss=new HashMap<>();
					eachPosRss.add(eachTimeRss);
					times++;
				}else if(rm.find()){
					if(!aplist.contains(rm.group(1)))
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
//			Log.i(TAG,Constant.offtxts[t++]);
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
					double avg=(count[j]<neglect_frequency ? defaultRSS : sum[j]/count[j]);
					eachavgrss.put(aplist.get(j), avg);
//					Log.i(TAG, aplist.get(j)+" "+avg);
				}
			}
			Tools.cleanArr(count, sum);
		}
	}
}
