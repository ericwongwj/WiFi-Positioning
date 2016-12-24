package processdataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ProbabilityMethods {
//	static void positionNN(int c){
//		ArrayList<Integer> searchField=narrowSearchField(c);//其实可以不用narrow...试试看
//		ArrayList<Double> distanceList=new ArrayList<>();//存放和各个offline点的欧氏距离
//		ArrayList<Double> narrowedXList=new ArrayList<>();//记录narrow之后位置的xy坐标
//		ArrayList<Double> narrowedYList=new ArrayList<>();
//		
//		for(int i=0;i<searchField.size();i++){/***对缩小之后的范围中的点 计算欧氏距离***/
//			String pos=offPositionlist.get(searchField.get(i));
//			ArrayList<Double> rssvector=offRssVectorlist.get(searchField.get(i));
//			narrowedXList.add(offXlist.get(searchField.get(i)));
//			narrowedYList.add(offYlist.get(searchField.get(i)));
//			
//			double distance=0;
//			double sum=0;
//			for(int j=0;j<rssvector.size();j++){//这两个向量是否是同维的？
//				double offrss=rssvector.get(j);
//				double onrss=onRssVectorlist.get(c).get(j);
//				sum+=(offrss-onrss)*(offrss-onrss);
//			}
//			distance=Math.sqrt(sum);
//			distanceList.add(distance);
////			System.out.println(i+" distance "+pos+": "+distance);
//		}
//		
//		int []nearestpoints=findNNearest(1, distanceList);//数组长度为1 得到distancelist当中的位置
//		double x=narrowedXList.get(nearestpoints[0]);//offXlist的下标不是narrow之后的下标
//		double y=narrowedYList.get(nearestpoints[0]);
//				
//		double deviationX=x-onXlist.get(c);//计算误差
//		double deviationY=y-onYlist.get(c);
//		double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
//		deviationArr[c]=deviation;
//		System.out.println(c+" result:"+x+","+y+"   true position:"+Constant.ON_POS_ARR[c]
//							+" deviation:"+deviation);
//	}
	//利于封装 也许应该加一个定位的信号空间
//		static void positionKWNN(int c, int k, double p){
//			knn=k;
//			ArrayList<Integer> searchField=narrowSearchField(c);//参数c代表第c个点
//			ArrayList<Double> distanceList=new ArrayList<>();
//			ArrayList<Double> narrowedXList=new ArrayList<>();//记录narrow之后位置的xy坐标
//			ArrayList<Double> narrowedYList=new ArrayList<>();
//			
//			for(int i=0;i<searchField.size();i++){/***对缩小之后的范围中的点 计算欧氏距离***/
//				String pos=offPositionlist.get(searchField.get(i));
//				ArrayList<Double> rssvector=offRssVectorlist.get(searchField.get(i));
//				narrowedXList.add(offXlist.get(searchField.get(i)));
//				narrowedYList.add(offYlist.get(searchField.get(i)));
//				
//				double distance=0;
//				double sum=0;
//				for(int j=0;j<rssvector.size();j++){//这两个向量是否是同维的？
//					double offrss=rssvector.get(j);
//					double onrss=onRssVectorlist.get(c).get(j);
//					sum+=(offrss-onrss)*(offrss-onrss);
//				}
//				distance=Math.sqrt(sum);
//				distanceList.add(distance);
////				System.out.println(i+" distance "+pos+": "+distance);
//			}
//			
//			int []nearestpoints=findNNearest(k, distanceList);//第一个参数为取点的个数 返回distancelist当中的位置数组
//			double []nearestdistances=new double[k];
//			double []xarr=new double[k];
//			double []yarr=new double[k];
//			
//			for(int a=0;a<nearestpoints.length;a++){
//				double x=narrowedXList.get(nearestpoints[a]);//offXlist的下标不是narrow之后的下标
//				double y=narrowedYList.get(nearestpoints[a]);
//				xarr[a]=x;
//				yarr[a]=y;
//				nearestdistances[a]=distanceList.get(nearestpoints[a]);
////				System.out.println(a+" "+nearestpoints[a]+" "+x+","+y+" "+nearestdistances[a]);
//			}
//			
//			double []result=calculatePosition(xarr, yarr, nearestdistances, knn, p);/***定位得到结果***/
//			double deviationX=result[0]-onXlist.get(c);//计算误差
//			double deviationY=result[1]-onYlist.get(c);
//			double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
//			deviationArr[c]=deviation;
//			System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+Constant.ON_POS_ARR[c]
//								+" deviation:"+deviation);
//		}
//		
//		//有用narrow  	distance_0 = w_0*distance(ap_0,ap_i) distance^2 = sigma(distance_0^2,...,distance_m^2)
//		static void positionUsingPenalty(int c){
//			ArrayList<Integer> searchField=narrowSearchField(c);//参数c代表第c个点
//			ArrayList<Double> distanceList=new ArrayList<>();
//			ArrayList<Double> narrowedXList=new ArrayList<>();//记录narrow之后位置的xy坐标
//			ArrayList<Double> narrowedYList=new ArrayList<>();
//			ArrayList<Map<String, Double>>penaltyList=ReadOffline.penaltyList;
//
//			for(int i=0;i<searchField.size();i++){/***对缩小之后的范围中的线下点 计算欧氏距离***/
//				String pos=offPositionlist.get(searchField.get(i));
//				ArrayList<Double> rssvector=offRssVectorlist.get(searchField.get(i));
//				Map<String,Double>map=penaltyList.get(searchField.get(i));
//				narrowedXList.add(offXlist.get(searchField.get(i)));
//				narrowedYList.add(offYlist.get(searchField.get(i)));
//				double distance=0;
//				double sum=0;
//				double weight=0;
//				for(int j=0;j<rssvector.size();j++){//这两个向量是否是同维的？
//					double offrss=rssvector.get(j);
//					double onrss=onRssVectorlist.get(c).get(j);
//					if(map.get(aplist.get(j))!=null)
//						weight=map.get(aplist.get(j));
//					sum+=weight*(offrss-onrss)*(offrss-onrss);//每一个ap的维度上
//				}
//				distance=Math.sqrt(sum);
//				distanceList.add(distance);
////				System.out.println(i+" distance "+pos+": "+distance);
//			}
//			
//			int []nearestpoints=findNNearest(4, distanceList);//第一个参数为取点的个数 返回distancelist当中的位置数组
//			double []nearestdistances=new double[4];
//			double []xarr=new double[4];
//			double []yarr=new double[4];
//			
//			for(int a=0;a<nearestpoints.length;a++){
//				double x=narrowedXList.get(nearestpoints[a]);//offXlist的下标不是narrow之后的下标
//				double y=narrowedYList.get(nearestpoints[a]);
//				xarr[a]=x;
//				yarr[a]=y;
//				nearestdistances[a]=distanceList.get(nearestpoints[a]);
//			}
//			
//			double []result=calculatePosition(xarr, yarr, nearestdistances, knn, 1);
//			double deviationX=result[0]-onXlist.get(c);//计算误差
//			double deviationY=result[1]-onYlist.get(c);
//			double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
//			deviationArr[c]=deviation;
//			System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+Constant.ON_POS_ARR[c]
//								+" deviation:"+deviation);
//		}
//		
//		static void positionRSSWeight(int c){
//			
//		}
//		
//		//不narrow
//		static void positionKWNNSingle(int c, int k, double p){
//			knn=k;
//			ArrayList<Double> distanceList=new ArrayList<>();
//			ArrayList<Double> xList=new ArrayList<>();//记录narrow之后位置的xy坐标
//			ArrayList<Double> yList=new ArrayList<>();
//			
//			for(int i=0;i<offPositionlist.size();i++){/***对缩小之后的范围中的点 计算欧氏距离***/
//				String pos=offPositionlist.get(i);
//				ArrayList<Double> rssvector=offRssVectorlist.get(i);
//				xList.add(offXlist.get(i));
//				yList.add(offYlist.get(i));
//				
//				double distance=0;
//				double sum=0;
//				for(int j=0;j<rssvector.size();j++){//这两个向量是否是同维的？
//					double offrss=rssvector.get(j);
//					double onrss=onRssVectorlist.get(c).get(j);
//					sum+=(offrss-onrss)*(offrss-onrss);
//				}
//				distance=Math.sqrt(sum);
//				distanceList.add(distance);
////				System.out.println(i+" distance "+pos+": "+distance);
//			}
//			
//			int []nearestpoints=findNNearest(k, distanceList);//第一个参数为取点的个数 返回distancelist当中的位置数组
//			double []nearestdistances=new double[k];
//			double []xarr=new double[k];
//			double []yarr=new double[k];
//			
//			for(int a=0;a<k;a++){
//				double x=xList.get(nearestpoints[a]);//offXlist的下标不是narrow之后的下标
//				double y=yList.get(nearestpoints[a]);
//				xarr[a]=x;
//				yarr[a]=y;
//				nearestdistances[a]=distanceList.get(nearestpoints[a]);
////				System.out.println(a+" "+nearestpoints[a]+" "+x+","+y+" "+nearestdistances[a]);
//			}
//			
//			double []result=calculatePosition(xarr, yarr, nearestdistances, knn, p);/***定位得到结果***/
//			double deviationX=result[0]-onXlist.get(c);//计算误差
//			double deviationY=result[1]-onYlist.get(c);
//			double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
//			deviationArr[c]=deviation;
//			System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+onPositionlist.get(c)
//								+" deviation:"+deviation);
//		}
//		
//		//没narrow？
//		static void positionUsingBetterRSS(int c){
//			ArrayList<Map<String, Double>>eachPosQuaRssList=ReadOffline.eachPosQuaRssList;
//			Map<String, Double> mapon=onEachPosRssList.get(c);
//			ArrayList<Double> distanceList=new ArrayList<>();
//			Map<Double,Integer> dindexmap=new HashMap<>();//存放距离和对应的index
//
//			//计算距离
//			for(int i=0;i<eachPosQuaRssList.size();i++){
//				Map<String, Double> mapoff=eachPosQuaRssList.get(i);
//				double sum=0;
//				double distance=0;
//				for(int j=0;j<aplist.size();j++){
//					double on=mapon.get(aplist.get(j));
//					if(j!=aplist.size()-1)//注意mapoff向量少一维
//						if(mapoff.get(aplist.get(j))!=null){
//							double off=mapoff.get(aplist.get(j));
//							sum+=(on-off)*(on-off);
//						}
//				}
//				distance=Math.sqrt(sum);
//				distanceList.add(distance);
//				dindexmap.put(distance, i);
////				System.out.println(i+" "+distance);
//			}
//			//找到最近的距离 默认knn=4
//			Collections.sort(distanceList);
//			double []ndist=new double[4];
//			double []xarr=new double[4];
//			double []yarr=new double[4];
//			for(int i=0;i<4;i++){
//				ndist[i]=distanceList.get(i);
//				int index=dindexmap.get(ndist[i]);
////				System.out.println("distance:"+ndist[i]+" "+offPositionlist.get(dindexmap.get(ndist[i])));	
//				xarr[i]=offXlist.get(index);
//				yarr[i]=offYlist.get(index);
//			}
//			
//			double []result=calculatePosition(xarr, yarr, ndist, knn, 1);
//			double deviationX=result[0]-onXlist.get(c);//计算误差
//			double deviationY=result[1]-onYlist.get(c);
//			double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
//			deviationArr[c]=deviation;
//			System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+onPositionlist.get(c)
//								+" deviation:"+deviation);
//		}
//		
//		//k此处默认为4
//		static void positionKWNNWithOffDensity(int c){
//			
//			ArrayList<Map<String, Double>>densityRssList=ReadOffline.densityRssList;
//			ArrayList<Integer>rpilist=ReadOffline.randposindexlist;
//			ArrayList<Double> distanceList=new ArrayList<>();
//			Map<Double,Integer> dindexmap=new HashMap<>();//存放距离和对应的index
//
//			for(int i=0;i<densityRssList.size();i++){//对于信号空间中的所有点 计算对应的欧氏距离 一次循环产生一个距离
//												// 线下第i个点的信号向量        当前线上点的信号向量		
//				double distance=calculateDistance(densityRssList.get(i),onEachPosRssList.get(c));
//				distanceList.add(distance);//对线上信号空间内的点产生距离列表
//				int index=rpilist.get(i);
//				dindexmap.put(distance, index);//densityRssList中的第i个向量
//			}
//			Collections.sort(distanceList);
//			//for(double d:distanceList)
//				//System.out.println("distance:"+d+" "+dindexmap.get(d));	
//			
//			//接下来寻找最近的点 对最近的距离 找出对应的poslist中的点 以及xy坐标 形成两个数组 传入函数当中
//			double []ndist=new double[4];
//			double []xarr=new double[4];
//			double []yarr=new double[4];
//			for(int i=0;i<4;i++){
//				ndist[i]=distanceList.get(i);
//				int index=dindexmap.get(ndist[i]);
//				xarr[i]=offXlist.get(index);
//				yarr[i]=offYlist.get(index);
//			}
//			
//			double []result=calculatePosition(xarr, yarr, ndist, knn, 1);
//			double deviationX=result[0]-onXlist.get(c);//计算误差
//			double deviationY=result[1]-onYlist.get(c);
//			double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
//			deviationArr[c]=deviation;
//			System.out.println(c+" result:"+result[0]+","+result[1]+"   true position:"+Constant.ON_POS_ARR[c]
//								+" deviation:"+deviation);
//		}
}
