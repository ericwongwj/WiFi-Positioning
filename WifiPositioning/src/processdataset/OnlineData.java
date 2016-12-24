package processdataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;


public class OnlineData {
	
	private File file;
	private BufferedReader br;

	public static List<String> aplist= Arrays.asList(Constant.AP_ARR);//27

	/**
	 * 每一个点每一次采集每一个ap 存储所有数据 改变算法时 在这上面读取 产生新的avgRssList
	*/
	List<List<Map<String, Double>>> allRss=new ArrayList<>(46);//二者只初始化一次
	List <Integer[]> apVectors=new ArrayList<>(46);/**0-n向量组成的数组记录ap是否出现若干次*/

	List<Map<String, Double>> avgRssList=new ArrayList<>();/**平均值*/
	List <List<Map<Double, Integer>>> rssVectors=new ArrayList<>();/**每个点每个ap出现的rss以及对应的次数 130*density-26-n*/
	
	public Point[] points=new Point[Constant.ON_POS_ARR.length];
		
	public int number=46;
	private double availableRSS=-80.0;	
	private double timeDensity=1.0;//采集次数密度0.0~1.0
	
	public static void main(String[] args) {
//		OnlineData on=new OnlineData(Constant.ON_PATH,1.0);			
//		showApVectorList(on.apVectorlist);
//		showRssVectorList(on.rssVectorlist);		
//		Tools.displayAllRSS(on.allRssList, aplist);
//		showMapList(on.avgRssList);
	}
	
	
	public OnlineData(String path, double td) {
		timeDensity=td;
		initPoints();
		initBr(path);
		initRSSData();
		buildRssVectorList();//build的是全部的情况

		if(timeDensity==1)
			generateAvgRss(allRss);
		else if(timeDensity<1){
			List<List<Map<String, Double>>> tempRss=initRandTimeRss(timeDensity,timeDensity);
			generateAvgRss(tempRss);
		}
	}
	
	public void initPoints(){
		for(int i=0;i<Constant.ON_X_ARR.length;i++){
			points[i]=new Point(Constant.ON_X_ARR[i], Constant.ON_Y_ARR[i]);
		}
	}
	
	/**
	 * eachTimeRss中只put了能侦测到的ap以及对应的rss
	 */
	public void initRSSData(){
		try {
			String line;
			Map<String,Double> eachTimeRss = null;
			List<Map<String, Double>> eachPosRss = null;//大小110（次）
			Integer[] apVector=new Integer[aplist.size()];//0-1向量
			while((line=br.readLine())!=null ){
				Matcher newline_matcher=Constant.newline_pattern.matcher(line);
				if(newline_matcher.find()&&line.contains(Constant.fixedid)){
					eachTimeRss=new HashMap<>();
					String target=line.replace(Constant.fixedid, "");
					Matcher rm=Constant.rss_pattern.matcher(target);
					while(rm.find()){//只存储出现的ap
						String each_ap="00:"+rm.group(1);//m1 mac地址 m2：rss
						double rss=Double.valueOf(rm.group(2));
						eachTimeRss.put(each_ap, rss);
						
						if(aplist.contains(each_ap))//因为有线下不存在的ap
							apVector[aplist.indexOf(each_ap)]++;
					}
					eachPosRss.add(eachTimeRss);//先添加	
				}
				Matcher starttime_matcher=Constant.starttime_pattern.matcher(line);
				if(starttime_matcher.find()){
					eachPosRss=new ArrayList<>(110);
					apVector=new Integer[aplist.size()];
					Tools.cleanArr(apVector);;
				}
				Matcher endtime_matcher=Constant.endtime_pattern.matcher(line);
				if(endtime_matcher.find()){
					allRss.add(eachPosRss);
					apVectors.add(apVector);
				}
			}			
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<List<Map<String, Double>>> initRandTimeRss(double pd,double td){
		List<List<Map<String, Double>>> randRssList=new ArrayList<>();
		TreeSet<Integer> tset=Tools.generateRandArr(td, 110);

		List<Map<String, Double>> eachPosRss = null;
		Map<String,Double> eachTimeRss = null;
		for(List<Map<String, Double>> maplist:allRss){
			eachPosRss=new ArrayList<>();//大小为110*td
			
			for(Map<String,Double> map:maplist){
				if(tset.contains(maplist.indexOf(map))){//110次中的一些随机 次数减少 apVector/rssVector啥的也在变
					eachTimeRss=new HashMap<>();
					eachPosRss.add(eachTimeRss);//先添加	
					
					for(String ap:map.keySet())
						eachTimeRss.put(ap, map.get(ap));
					
				}
			}
			
			randRssList.add(eachPosRss);	
		}
		return randRssList;
	}

	private void generateAvgRss(List<List<Map<String, Double>>> rssList){
		int []count=new int [aplist.size()];
		double []sum=new double[aplist.size()];int t=0;
		for(List<Map<String, Double>> eachpos : rssList){
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
					double avg=sum[j]/count[j];//检测到的次数太少 (count[j]<neglect_frequency ? defaultRSS : sum[j]/count[j]);
					eachavgrss.put(aplist.get(j), avg);
				}
			}
			Tools.cleanArr(count, sum);
		}
	}
	
	/**
	 * 有一些ap在同一行中重复出现 所以数据会少
	 */
	public void buildRssVectorList(){
		for(List<Map<String, Double>> onePosRss:allRss){//130次
			List<Map<Double, Integer>> apRssList=new ArrayList<>(27);
			for(String ap:aplist){//27次
				Map<Double, Integer> oneApRss=new HashMap<>();
				for(Map<String,Double> oneTimeRss:onePosRss){
					
					if(oneTimeRss.get(ap)!=null){
						double rss=oneTimeRss.get(ap);
						if(oneApRss.containsKey(rss)){
							int times=oneApRss.get(rss);
							oneApRss.put(rss, ++times);
						}else
							oneApRss.put(rss, 1);
					}
					
				}
				apRssList.add(oneApRss);
			}
			rssVectors.add(apRssList);
		}
	}
	

	public static void showMapList(List<Map<String,Double>> list){
		System.out.println("size="+list.size());
		int i=0;
		for(Map<String,Double> map:list){
			System.out.println(Constant.ON_POS_ARR[i++]);
			for(String ap:map.keySet())
				System.out.println(ap+" "+map.get(ap));
		}
	}
	
	public static void showApVectorList(List<Integer[]> list){
		int i=0;
		for(Integer[] arr:list){
			System.out.println(Constant.ON_POS_ARR[i++]);
			int k=0;
			for(int j=0;j<arr.length;j++){
				if(arr[j]!=0){
					System.out.println(aplist.get(j)+" "+arr[j]);
					k++;
				}
			}
			System.out.println("ap number at this point is "+k);
		}
	}
	
	public static void showRssVectorList(List <List<Map<Double, Integer>>> list){
		System.out.println("size="+list.size());
		int i=0;
		for(List<Map<Double, Integer>> apRssList:list){
			System.out.println(Constant.ON_POS_ARR[i++]+"  ****************************************");
			int j=0;
			for(Map<Double, Integer> map:apRssList){
				if(!map.isEmpty()){
					System.out.println(aplist.get(j));
					int total=0;
					for(double rss:map.keySet()){
						total+=map.get(rss);
						System.out.println("rss="+rss+" times="+map.get(rss));
					}
					System.out.println("total times="+total);
				}				
				j++;
			}
		}
	}
	
	private void initBr(String path){
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
