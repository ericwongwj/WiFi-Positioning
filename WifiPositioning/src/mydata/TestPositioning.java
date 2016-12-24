package mydata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestPositioning {
	static OfflineData offline=new OfflineData(Constant.OFFPATH);
	static OnlineData online=new OnlineData(Constant.ONPATH);
	static ArrayList<String> aplist=offline.aplist;
	static Double[] posXlist=offline.Xlist;
	static Double[] posYlist=offline.Ylist;
	static Double[] onXlist=online.Xlist;
	static Double[] onYlist=online.Ylist;
	
	static double[] deviationArr=new double [onXlist.length];

	/**
	 * main��Ӧ�������޸�offlinedata��onlinedata�ĸ��ֲ��� ���޸�֮��������ݳ�Ա��ֵ�ͻ���֮�����仯
	 * @param args
	 */
	public static void main(String[] args) {
		
		int point=0;
		for(Map<String,Double>onRss:online.avgRssList){
			double[] result=KMeans.KNN(offline,onRss,4,point);
			getDeviation(result,point++);
		}
		double deviation=0;
		for(double d:deviationArr)
			deviation+=d;
//		System.out.println("average deviation:"+deviation/deviationArr.length);
//				positionUsingEveryTime();
		
//		List<List<Map<Double,Integer>>> histogram=createHistogram(offline.offRssList);
//		positioningALL(histogram, online);
	}
	
	public static void positionUsingEveryTime(){
		for(int i=0;i<online.onRssList.size();i++){
			for(Map<String,Double> map:online.onRssList.get(i))
				KMeans.KNN(offline,map, 4, i);
		}
	}
	
	public static void getDeviation(double[] result, int index){
		double deviationX=result[0]-onXlist[index];//�������
		double deviationY=result[1]-onYlist[index];
		double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
		deviationArr[index]=deviation;
		System.out.println("result:"+result[0]+","+result[1]+"   true position:"+Constant.onPos[index]
							+" deviation:"+deviation);
	}

	//�����㷨 ֱ��ͼ���Ը�������1�ķ�Χ
	public static List<List<Map<Double,Integer>>> createHistogram(ArrayList<ArrayList<Map<String, Double>>> offRssList){//����
		List<List<Map<Double,Integer>>> histogram=new ArrayList<>();
		
		for(int i=0;i<offRssList.size();i++){//
			ArrayList<Map<String, Double>> onePosRss=offRssList.get(i);
			List<Map<Double,Integer>> onePosHistogram=new ArrayList<>();//��СΪ����⵽ap�ĸ���
			//����ȷ���ж��ٸ�����⵽��ap
			List<String>tempApList=new ArrayList<>();
			for(Map<String,Double> oneTimeRss:onePosRss){
				for(String ap:oneTimeRss.keySet())//��Ӧ��value��Ϊnull
					if(!tempApList.contains(ap))
						tempApList.add(ap);				
			}
			//�����ܲ⵽��ap������list�����Ӧ������map
			for(int a=0;a<tempApList.size();a++){
				Map<Double,Integer> oneApHistogram=new HashMap<Double,Integer>();
				oneApHistogram.put(-1.0, aplist.indexOf(tempApList.get(a)));//��Ҫ֪��ÿ��map��Ӧ�ĸ�ap ��key=-1.0��Ӧ��valueֵ��ʾaplist�е�λ��
				onePosHistogram.add(oneApHistogram);
//				System.out.println("index"+oneApHistogram.get(-1.0));
			}
//			System.out.println(Constant.offtxts[i++]+" ap number="+onePosHistogram.size());
			
			for(String ap:tempApList){
				Map<Double,Integer> oneApHistogram=findMapByAp(onePosHistogram, ap);
				for(Map<String,Double> oneTimeRss:onePosRss){//��Ӧ��value��Ϊnull
					Double rss=oneTimeRss.get(ap);//�е�Ϊnull
					if(rss!=null){
						if(oneApHistogram.containsKey(rss)){
							int f=oneApHistogram.get(rss);
							oneApHistogram.put(rss, f+1);
						}
						else{
							oneApHistogram.put(rss, 1);
						}
					}
				}
//				showOneApHistogram(oneApHistogram);
			}
			histogram.add(onePosHistogram);
		}
		showHistogram(histogram);
		return histogram;
	}
	
	public static Map<Double,Integer> findMapByAp(List<Map<Double,Integer>> onePosHistogram, String ap){//????
		int index=aplist.indexOf(ap);
		for(Map<Double,Integer>oneApHistogram:onePosHistogram){
			if(oneApHistogram.get(-1.0)==index){
				return oneApHistogram;
			}
		}
		return null;
	}
	
	public static void showHistogram(List<List<Map<Double,Integer>>> histogram){
		int i=0;
		for(List<Map<Double,Integer>> onePosHistogram:histogram){
			System.out.println(Constant.offtxts[i++]+"*************************************************");
			for(Map<Double,Integer> oneApHistogram:onePosHistogram){
				System.out.println("ap"+oneApHistogram.get(-1.0)+"-----------------------------------------");
				for(Double rss:oneApHistogram.keySet()){
					if(rss!=-1.0)
						System.out.println("rss="+rss+" freq="+oneApHistogram.get(rss));
				}
			}
		}
	}
	
	public static void showOneApHistogram(Map<Double,Integer> map){
		System.out.println("ap"+map.get(-1.0)+" histogram "+aplist.get(map.get(-1.0)));
		for(Double rss : map.keySet()){
			if(rss!=-1.0)
				System.out.println(rss+" "+map.get(rss));
		}
	}
	
	/**
	 * ��oneline��ÿһ����ÿһ�εĶ�λ����������
	 * @param histogram �������е�offlineֱ��ͼ
	 * @param online ��������
	 */
	public static void positioningALL(List<List<Map<Double,Integer>>> histogram, OnlineData online){
		ArrayList<ArrayList<Map<String, Double>>> onRssList=online.onRssList;
		for(int i=4;i<5;i++){//onRssList.size()
			ArrayList<Map<String, Double>> onePosRss=onRssList.get(i);
			System.out.println("Pos: "+online.Xlist[i]+","+online.Ylist[i]);
			for(Map<String,Double>oneTimeRss:onePosRss){
				positionUsingHistogram(histogram, oneTimeRss);
			}
		}
	}
	
	public static void positionUsingHistogram(List<List<Map<Double,Integer>>> histogram,Map<String,Double>oneTimeRss){
		double pArr[]=new double[histogram.size()];//��¼ÿ����ĸ���
		Tools.cleanArr(pArr);
		for(int i=0;i<histogram.size();i++){
			List<Map<Double,Integer>> onePosHistogram=histogram.get(i);
			double p=1.0;//�����ĸ���
			for(String ap:oneTimeRss.keySet()){//ֻ�ȶ� �������¶��е� ���Կ��Ƕ�ȱʧ�ĵ���д���
				double onrss=oneTimeRss.get(ap);
				Map<Double,Integer> oneApHistogram=findMapByAp(onePosHistogram, ap);//�����Ҳ������ap
//				System.out.println(oneApHistogram.get(onrss));
				if(oneApHistogram!=null)
					if(oneApHistogram.get(onrss)!=null)
						p+=oneApHistogram.get(onrss);//����д�Ǵ���֮��
			}
			pArr[i]=p;
		}
		Tools.showArr(pArr);
		int []maxIndex=getNMax(pArr,4);
		double x = 0,y = 0;
		for(int i=0;i<maxIndex.length;i++){
			x+=offline.Xlist[maxIndex[i]];
			y+=offline.Ylist[maxIndex[i]];
		}
		System.out.println(maxIndex[0]+" "+maxIndex[1]+" "+maxIndex[2]+" "+maxIndex[3]);
		System.out.println("result:"+x/4+","+y/4);
	}
	
	//����pArr�е�index
	public static int[] getNMax(double []parr,int n){
		int []idx=new int[n];
		double []temp=new double[n];
		int i=0;
		while(i<n){//ȡn����
			int index=0;
			double max=parr[0];
			for(int j=0;j<parr.length-1;j++){//�ҵ���ǰ������ֵ����λ��
				if(max<parr[j+1]){
					max=parr[j+1];
					index=j+1;
				}
			}
			idx[i]=index;
			parr[index]=-1000.0;//���Ѿ��ҵ����������Ϊһ����С��ֵ ע���ʱԭ����ֵ�Ѿ��ı�
			i++;
		}
		return idx;
	}
	
	/*static double[] calculatePosition(int []index,double []probabilities, double p){
		double []xarr={offXlist.get(index[0]),offXlist.get(index[1])};//,offXlist.get(index[2]),offXlist.get(index[3])
		double []yarr={offYlist.get(index[0]),offYlist.get(index[1])};//,offYlist.get(index[2]),offYlist.get(index[3])

		double[] result=new double[2];//0:x 1:y
		double wtotal=0;
		for(int i=0;i<index.length;i++){
			wtotal+=Math.pow(probabilities[i], p);//��������
		}
		for(int i=0;i<index.length;i++){
			result[0]+=xarr[i]*probabilities[i]/wtotal;
			result[1]+=yarr[i]*probabilities[i]/wtotal;
		}	
		return result;
	}*/
}
