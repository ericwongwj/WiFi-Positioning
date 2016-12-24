package processdataset;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class KMeans {
	int k;
	void setK(int k){
		this.k=k;
	}
	
	static List <String> aplist= Arrays.asList(Constant.AP_ARR);//aplist必须统一使用offline的
	
	/**
	 * 
	 * @param onrss
	 * @param k
	 * @param usePenalty
	 * @param index
	 */
	public static void KNN(OfflineData offline, OfflineData online, Map<String,Double> onrss, double[] deviationArr, int k, boolean usePenalty, int index){
		//narrow 改为得到一个排除的数组 循环的时候直接判断 
		List<Integer> invalidField=Tools.reduceField(offline.apVectors, online.apVectors.get(index));//参数c代表第c个点
		Map<Double,Integer> distanceMap=new TreeMap<>();//位置（0-129）-距离
		System.out.println("invalid field size:"+invalidField.size());

		for(int i=0;i<Constant.OFF_POS_ARR.length;i++){
//			if(invalidField.contains(i)) continue;//对限定范围后的所有点 其实并没有用
			//计算距离 只计算线下出现的ap 线上线下只出现一个的 默认另一个为-100
			Map<String,Double> offrss=offline.avgRssList.get(i);
			Map<String,Double> penaltyMap=offline.penaltyList.get(i);
			double distance, sum=0;
			for(int j=0;j<aplist.size();j++){
				double penalty=penaltyMap.get(aplist.get(j));
				double off,on;
				if(offrss.get(aplist.get(j))!=null)
					off=offrss.get(aplist.get(j));
				else off=-100.0;
				
				if(onrss.get(aplist.get(j))!=null)
					on=onrss.get(aplist.get(j));
				else on=-100.0;
				sum += usePenalty ? penalty*(off-on)*(off-on) : (off-on)*(off-on);
			}
			distance=Math.sqrt(sum);
			distanceMap.put(distance,i);
		}
		
		double xsum=0.0,ysum=0.0;
		int kk=0;
		for(Double d:distanceMap.keySet()){
			if(kk++==k)
				break;
			int pos=distanceMap.get(d);
			xsum+=offline.points[pos].x;
			ysum+=offline.points[pos].y;
			System.out.println(offline.points[pos]+" d="+d);
		}
		Point result=new Point(xsum/k,ysum/k);
		deviationArr[index]=online.points[index].distance(result);
		System.out.println("result:"+result+"   real:"+online.points[index]+" deviation:"+deviationArr[index]);
	}
	
	public static void WKNN(OfflineData offline, OfflineData online,Map<String,Double> onrss, double[] deviationArr,int k, double exp, boolean usePenalty, int index){
		Map<Double,Integer> distanceMap=new TreeMap<>();//位置（0-129）-距离
		for(int i=0;i<Constant.OFF_POS_ARR.length;i++){
			//计算距离 只计算线下出现的ap 线上线下只出现一个的 默认另一个为-100
			Map<String,Double> offrss=offline.avgRssList.get(i);
			Map<String,Double> penaltyMap=offline.penaltyList.get(i);
			double distance, sum=0;
			for(int j=0;j<aplist.size();j++){
				double penalty=penaltyMap.get(aplist.get(j));
				double off,on;
				if(offrss.get(aplist.get(j))!=null)
					off=offrss.get(aplist.get(j));
				else off=-100.0;
				
				if(onrss.get(aplist.get(j))!=null)
					on=onrss.get(aplist.get(j));
				else on=-100.0;
				sum += usePenalty ? penalty*(off-on)*(off-on) : (off-on)*(off-on);
			}
			distance=Math.sqrt(sum);
			distanceMap.put(distance,i);
//			System.out.println(Constant.OFF_POS_ARR[p++]+" distance "+": "+distance);//输出off与on的距离
		}
		
		double x=0.0,y=0.0,weight=0.0;
		double []weights=new double[k];
		Iterator<Map.Entry<Double, Integer>> iterator = distanceMap.entrySet().iterator();
		for(int a=0;a<k;a++){
			Map.Entry<Double, Integer> entry = iterator.next();  
			weights[a]=Math.pow(1/entry.getKey(), exp);
			weight+=Math.pow(1/entry.getKey(), exp);
		}
		System.out.println("total weight="+weight);
		Iterator<Map.Entry<Double, Integer>> entries = distanceMap.entrySet().iterator();

		for(int b=0;b<k;b++){
			Map.Entry<Double, Integer> entry = entries.next();  
			int pos=entry.getValue();
			x+=offline.points[pos].x*weights[b]/weight;
			y+=offline.points[pos].y*weights[b]/weight;
			System.out.println(Constant.OFF_POS_ARR[pos]+" d="+entry.getKey()+" w="+weights[b]);
		}
		Point result=new Point(x,y);
		deviationArr[index]=online.points[index].distance(result);
		System.out.println("result:"+result+"   real:"+online.points[index]+" deviation:"+deviationArr[index]);
				
	}	
}
