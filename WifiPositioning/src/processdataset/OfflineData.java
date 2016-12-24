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
	 * ÿһ����ÿһ�βɼ�ÿһ��AP �洢�������� �ı��㷨ʱ ���������ȡ �����µ�avgRssList
	*/
	List<List<Map<String, Double>>> allRss=new ArrayList<>(130);//����ֻ��ʼ��һ��
	List <Integer[]> apVectors=new ArrayList<>();/**0-n������ɵ������¼AP�Ƿ�������ɴ�*/

	List<Map<String, Double>> penaltyList=new ArrayList<>();/**��ƽ��ֵ����� ��¼ÿ����ÿ��AP�Ŀ��Ŷ�(1-P(miss)) */
	List<Map<String, Double>> avgRssList=new ArrayList<>();/**ƽ��ֵ*/
	
	/**ÿ����ÿ��AP���ֵ�RSS�Լ���Ӧ�Ĵ��� ÿһ���С��130*density-26-n 
	 * ʹ��TreeMap��֤RSS������� TreeMap��list��AP˳�����*/
	List <List<TreeMap<Double, Integer>>> rssVectors=new ArrayList<>();
	
	public Point[] points=new Point[Constant.OFF_POS_ARR.length];
	public static double[] XArr=Constant.OFF_X_ARR;
	public static double[] YArr=Constant.OFF_Y_ARR;
	
	public List<Point> centerPoints=new ArrayList<>();
	List<List<Map<String, Double>>> centerPosRss=new ArrayList<>();
	
	public int number=130;
	
	private int neglect_frequency=0;//KNN���?ƫ����С  22
	private double posDensity;//�ɼ����ܶ�
	private double timeDensity;//�ɼ������ܶ�
	private static double defaultRSS=-95.0;//Ĭ��missed APΪ-100
	private static double availableRSS=-1000;//��ÿ�εĿ���ֵ����ƽ��ֵ���ã�
	
	/**
	 * �����������ݵĲ���
	 */
	public static class Options{
		double pDensity=1.0;//�ɼ����ܶ�
		double tDensity=1.0;//�ɼ������ܶ�
		
		int neglect_frequency=0;//���˴����µ�AP RSS��Ϊdefault
		double defaultRSS=-95.0;//Ĭ��missed AP��RSS
		double availableRSS=-1000.0;//��ʹ�ô��ڸ�ֵ��RSS Ĭ��Ϊ��С��ΪRSS������ ��ֵ��������������
		
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
		buildRssVectorList();//build����ȫ�������
		buildPenaltyList();

		if(posDensity==1 && timeDensity==1){
			generateAvgRss(allRss);
//			buildCenterPointsInfo();//�˺��������޸�avgRSSlist����
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
			List<Map<String, Double>> eachPosRss = null;//��С110���Σ�
			Integer[] apVector=new Integer[aplist.size()];//0-n����
			while((line=br.readLine())!=null ){
				Matcher newline_matcher=Constant.newline_pattern.matcher(line);
				if(newline_matcher.find()&&line.contains(Constant.fixedid)){
					eachTimeRss=new HashMap<>();
					String target=line.replace(Constant.fixedid, "");
					Matcher rm=Constant.rss_pattern.matcher(target);
					while(rm.find()){//ֻ�洢���ֵ�ap
						String each_ap="00:"+rm.group(1);//m1 mac��ַ m2��rss
						double rss=Double.valueOf(rm.group(2));
						
						eachTimeRss.put(each_ap, rss);
						apVector[aplist.indexOf(each_ap)]++;

					}
					eachPosRss.add(eachTimeRss);//�����	
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
			if(pset.contains(allRss.indexOf(maplist))){//130���е�һЩ���
				eachPosRss=new ArrayList<>();//��СΪ110*td
				
				poslist.add(Constant.OFF_POS_ARR[allRss.indexOf(maplist)]);
				
				for(Map<String,Double> map:maplist){
					if(tset.contains(maplist.indexOf(map))){//110���е�һЩ��� �������� apVector/rssVectorɶ��Ҳ�ڱ�
						eachTimeRss=new HashMap<>();
						eachPosRss.add(eachTimeRss);//�����	
						
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
//					if(availableRSS<=-100){//ʹ����availableRSS Ƶ��һ�㲻��̫��
						eachavgrss.put(aplist.get(j), count[j]<neglect_frequency ? defaultRSS : avg);
//					} 
//					else {//ʹ��availableRSSb ���ƽ��ֵ-81 ʵ��79 ���ͻ�ܴ�
//						if(avg<-availableRSS) continue;
//						else eachavgrss.put(aplist.get(j), avg);
//					}
				}
			}
			Tools.cleanArr(count, sum);
		}
	}
	
	public void buildRssVectorList(){
		for(List<Map<String, Double>> onePosRss:allRss){//130��
			List<TreeMap<Double, Integer>> apRssList=new ArrayList<>(27);
			for(String ap:aplist){//27��
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
	 * ֱ�Ӷ�apVector���� ͨ������missed AP�ĸ��ʼ���ÿһ����ÿһ��ap�Ŀ��Ŷ� �ô˷��� ��λ�������
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
	 * TODO ��Ϊʵ��ͼ������ ��Щ���ĵ�Ҫ��ͼ����Ӧ����51�����ҵ��ĸ���ȷ�������ĵ� �˺�����һЩСȱ��
	 * �����centerPointInfo 
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
						&& nearx-x>=0 && neary-y>=0) //����ȫ�Ե��ж�����
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
//		System.out.println(centerPoints.size()+" center points");//������50��
	}
	
	public void buildBetterRSS(double quality){	}
	
	public void buildWeightRSSList(){}
	
	public double caculateProbility(){return 0;}
	
	
	
	
	/**
	 * ���º����������
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
