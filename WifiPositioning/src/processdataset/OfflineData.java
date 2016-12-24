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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;

public class OfflineData {

	private File file;
	private BufferedReader br;

	public static List<String> aplist= Arrays.asList(Constant.AP_ARR);//27
	public static List<String> poslist= Arrays.asList(Constant.OFF_POS_ARR);

	/**
	 * 每一个点每一次采集每一个AP 存储所有数据 改变算法时 在这上面读取 产生新的avgRssList
	*/
	List<List<Map<String, Double>>> allRss=new ArrayList<>(130);//二者只初始化一次
	List <Integer[]> apVectors=new ArrayList<>();/**0-n向量组成的数组记录AP是否出现若干次*/

	List<Map<String, Double>> penaltyList=new ArrayList<>();/**和平均值相对于 记录每个点每个AP的可信度(1-P(miss)) */
	List<Map<String, Double>> avgRssList=new ArrayList<>();/**平均值*/
	
	/**每个点每个AP出现的RSS以及对应的次数 每一层大小：130*density-26-n 
	 * 使用TreeMap保证RSS是升序的 TreeMap的list按AP顺序添加*/
	List <List<TreeMap<Double, Integer>>> rssVectors=new ArrayList<>();
	
	public Point[] points=new Point[Constant.OFF_POS_ARR.length];
	public static double[] XArr=Constant.OFF_X_ARR;
	public static double[] YArr=Constant.OFF_Y_ARR;
	
	public List<Point> centerPoints=new ArrayList<>();
	List<List<Map<String, Double>>> centerPosRss=new ArrayList<>();
	
	public int number=130;
	
	private int neglect_frequency=0;//KNN设成?偏差最小  22
	private double posDensity;//采集点密度
	private double timeDensity;//采集次数密度
	private static double defaultRSS=-95.0;//默认missed AP为-100
	private static double availableRSS=-1000;//是每次的可用值还是平均值可用？
	
	/**
	 * 设置线下数据的参数
	 */
	public static class Options{
		double pDensity=1.0;//采集点密度
		double tDensity=1.0;//采集次数密度
		
		int neglect_frequency=0;//将此次数下的AP RSS置为default
		double defaultRSS=-95.0;//默认missed AP的RSS
		double availableRSS=-1000.0;//仅使用大于该值的RSS 默设为极小认为RSS都可用 该值和以上两个互斥
		
		public Options() {}
		public Options(String what, double value) {
			switch(what){
			case "pos":
				pDensity=value;
				break;
			case "time":
				tDensity=value;
				break;
			case "neglect":
				neglect_frequency=(int)value;
				break;
			case "default":
				defaultRSS=value;
				break;
			case "available":
				availableRSS=value;
				break;
			default:
				System.out.println("wrong");
			}
		}
		public Options(double p, double t, int nf,double deRss, double avRss) {
			pDensity=p;
			tDensity=t;
			neglect_frequency=nf;
			defaultRSS=deRss;
			availableRSS=avRss;
		}
	} 
	
	public static void main(String[] args) {
		Options ops=new Options();
//		ops.pDensity=0.1;
//		ops.tDensity=0.1;
		ops.availableRSS=-100;
		OfflineData offline=new OfflineData(Constant.OFF_PATH, ops);
		
//		Tools.displayAllRSS(offline.allRssList, offline.aplist);
//		showMapList(offline.avgRssList);
//		showMapList(offline.penaltyList);
//		Tools.showList(aplist);
//		Tools.showList(poslist);
//		showApVectorList(offline.apVectorlist);
//		showRssVectorList(offline.rssVectorlist);
	}

	public OfflineData(String path, Options options) {
		posDensity=options.pDensity;
		timeDensity=options.tDensity;
		defaultRSS=options.defaultRSS;
		availableRSS=options.availableRSS;
		neglect_frequency=options.neglect_frequency;
		initPoints();
		initBufferReader(path);
		initRSSData();
		buildRssVectorList();//build的是全部的情况
		buildPenaltyList();

		if(posDensity==1 && timeDensity==1){
			generateAvgRss(allRss);
//			buildCenterPointsInfo();//此函数会间接修改avgRSSlist本身
		}else if(posDensity<1 || timeDensity<1){
			List<List<Map<String, Double>>> tempRss=initRandPosAndTimeRss(posDensity,timeDensity);
			generateAvgRss(tempRss);
		}else System.out.println("Wrong density!");
	}
	
	public void initPoints(){
		for(int i=0;i<Constant.OFF_X_ARR.length;i++){
			points[i]=new Point(Constant.OFF_X_ARR[i], Constant.OFF_Y_ARR[i]);
//			System.out.println(points[i]);
		}
	}
	
	public void initRSSData(){
		try {
			String line;
			Map<String,Double> eachTimeRss = null;
			List<Map<String, Double>> eachPosRss = null;//大小110（次）
			Integer[] apVector=new Integer[aplist.size()];//0-n向量
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
	
	
	public List<List<Map<String, Double>>> initRandPosAndTimeRss(double pd,double td){
		List<List<Map<String, Double>>> randRssList=new ArrayList<>();
		TreeSet<Integer> pset=Tools.generateRandArr(pd, 130);
		TreeSet<Integer> tset=Tools.generateRandArr(td, 110);

		poslist=new ArrayList<>();
		List<Map<String, Double>> eachPosRss = null;
		Map<String,Double> eachTimeRss = null;
		for(List<Map<String, Double>> maplist:allRss){
			if(pset.contains(allRss.indexOf(maplist))){//130次中的一些随机
				eachPosRss=new ArrayList<>();//大小为110*td
				
				poslist.add(Constant.OFF_POS_ARR[allRss.indexOf(maplist)]);
				
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
		}
		return randRssList;
	}
	
	private void generateAvgRss(List<List<Map<String, Double>>> rssList){
		int []count=new int [aplist.size()];
		double []sum=new double[aplist.size()];
		for(List<Map<String, Double>> eachpos : rssList){
			Map<String,Double> eachavgrss=new HashMap<>();
			avgRssList.add(eachavgrss);
			for(Map<String,Double> eachTimeRss : eachpos){
				for(int i=0;i<aplist.size();i++){
					String ap=aplist.get(i);
					Double rss=eachTimeRss.get(ap);
					if(rss!=null && rss>availableRSS){
						sum[i]+=eachTimeRss.get(ap);
						count[i]++;
					}
				}
			}	
			for(int j=0;j<aplist.size();j++){
				if(count[j]!=0){
					double avg = sum[j]/count[j];
//					if(availableRSS<=-100){//使用了availableRSS 频率一般不会太少
						eachavgrss.put(aplist.get(j), count[j]<neglect_frequency ? defaultRSS : avg);
//					} 
//					else {//使用availableRSSb 如果平均值-81 实际79 误差就会很大
//						if(avg<-availableRSS) continue;
//						else eachavgrss.put(aplist.get(j), avg);
//					}
				}
			}
			Tools.cleanArr(count, sum);
		}
	}
	
	public void buildRssVectorList(){
		for(List<Map<String, Double>> onePosRss:allRss){//130次
			List<TreeMap<Double, Integer>> apRssList=new ArrayList<>(27);
			for(String ap:aplist){//27次
				TreeMap<Double, Integer> oneApRss=new TreeMap<>();
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
	
	/**
	 * 直接对apVector处理 通过计算missed AP的概率计算每一个点每一个ap的可信度 用此方法 定位精度最高
	 */
	public void buildPenaltyList(){
		for(Integer[] apVector:apVectors){
			Map<String,Double> map=new HashMap<>();
			for(int i=0;i<apVector.length;i++){
				double prob=apVector[i]/110.0;
				map.put(aplist.get(i), prob);
			}
			penaltyList.add(map);
		}
	}

	
	/**
	 * TODO 因为实际图形所限 有些中心点要按图形算应该有51个左右的四个点确定的中心点 此函数有一些小缺陷
	 * 这里的centerPointInfo 
	 */
	public void buildCenterPointsInfo(){	
		for(int j=0;j<Constant.OFF_POS_ARR.length;j++){
			double x=XArr[j], y=YArr[j];
			Set<Integer> nSet=new HashSet<>();
			Map<String, Double> centerRss=avgRssList.get(j);
			
			for(int i=0;i<XArr.length;i++){
				double nearx=XArr[i];
				double neary=YArr[i];
				if((nearx-x)*(nearx-x)+(neary-y)*(neary-y)<6 && (nearx-x)*(nearx-x)+(neary-y)*(neary-y)>0
						&& nearx-x>=0 && neary-y>=0) //不完全对的判定条件
					nSet.add(i);
			}
			if(nSet.size()==3){
				double sumx = XArr[j],sumy = YArr[j];
				for(int k:nSet){
					sumx+=XArr[k];
					sumy+=YArr[k];
					
					Map<String, Double> nRss=avgRssList.get(k);
					for(String ap:centerRss.keySet()){
						double rss1 = centerRss.get(ap)==null?defaultRSS:centerRss.get(ap);					
						double rss2 = nRss.get(ap)==null?defaultRSS:nRss.get(ap);
						centerRss.put(ap,rss1+rss2);	
					}
				}
				
				centerPoints.add(new Point(sumx/4,sumy/4));
//				System.out.println(new Point(sumx/4,sumy/4));	
				
				for(String ap:centerRss.keySet()){
					double rss=centerRss.get(ap);						
					centerRss.put(ap,rss/4);
//					System.out.println(ap+" "+rss/4);
				}
			}
		}
//		System.out.println(centerPoints.size()+" center points");//现在是50个
	}
	
	public void buildBetterRSS(double quality){	}
	
	public void buildWeightRSSList(){}
	
	public double caculateProbility(){return 0;}
	
	
	
	
	/**
	 * 以下函数便与输出
	 */
	public static void showMapList(List<Map<String,Double>> list){
		System.out.println("size="+list.size());
		int i=0;
		for(Map<String,Double> map:list){
			System.out.println(poslist.get(i++));
			for(String ap:map.keySet())
				System.out.println(ap+" "+map.get(ap));
		}
	}
	
	public static void showApVectorList(List<Integer[]> list){
		int i=0;
		for(Integer[] arr:list){
			System.out.println(Constant.OFF_POS_ARR[i++]);
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
	
	public static void showRssVectorList(List <List<TreeMap<Double, Integer>>> list){
		System.out.println("size="+list.size());
		int i=0;
		for(List<TreeMap<Double, Integer>> apRssList:list){
			System.out.println(Constant.OFF_POS_ARR[i++]+"  ****************************************");
			int j=0;
			for(Map<Double, Integer> map:apRssList){
				if(!map.isEmpty()){
					System.out.println(aplist.get(j));
					int total=0;
					for(double rss:map.keySet()){
						total+=map.get(rss);
						System.out.println("rss="+rss+" times="+map.get(rss));
					}
					System.out.println("total times="+total+"---------------------------------------");
				}				
				j++;
			}
		}
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
