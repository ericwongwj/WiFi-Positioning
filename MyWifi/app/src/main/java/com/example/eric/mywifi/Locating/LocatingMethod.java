package com.example.eric.mywifi.Locating;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Eric on 2016/8/9 0009.
 */
public class LocatingMethod {

    public static Double[] posXlist={
            0.0,0.0,2.0,2.0,4.0,4.0,6.0,6.0,8.0,8.0,
            10.0,10.0,12.0,12.0,14.0,14.0,16.0,16.0,18.0,18.0,
            20.0,20.0,22.0,22.0,24.0,24.0,26.0,26.0,28.0,28.0,
            30.0,30.0,32.0,32.0,34.0,34.0,36.0,36.0,38.0,38.0,
            40.0,40.0,42.0,42.0,};
    public static Double[] posYlist={
            0.0,2.0,0.0,2.0,0.0,2.0,0.0,2.0,0.0,2.0,
            0.0,2.0,0.0,2.0,0.0,2.0,0.0,2.0,0.0,2.0,
            0.0,2.0,0.0,2.0,0.0,2.0,0.0,2.0,0.0,2.0,
            0.0,2.0,0.0,2.0,0.0,2.0,0.0,2.0,0.0,2.0,
            0.0,2.0,0.0,2.0,0.0};

    public static double[] KNN(OfflineData offlineData, Map<String, Double> onrss, ArrayList<String> aplist, int k){
        ArrayList<Double> distanceList=new ArrayList<>(offlineData.offRssList.size());
        for(int i=0;i<posXlist.length;i++){//对线下的所有点
            Map<String,Double> offrss=offlineData.avgRssList.get(i);
            double distance,sum=0;
            for(int j=0;j<aplist.size();j++){
                double off,on;
                if(offrss.get(aplist.get(j))!=null)
                    off=offrss.get(aplist.get(j));
                else off=-100.0;
                if(onrss.get(aplist.get(j))!=null)
                    on=onrss.get(aplist.get(j));
                else on=-100.0;
                sum+=(off-on)*(off-on);
            }
            distance=Math.sqrt(sum);
            distanceList.add(distance);
        }

        int []nearestPoints=Tools.findNNearest(k, distanceList);//第一个参数为取点的个数 返回distancelist当中的位置
        double []nearestDistances=new double[k];
        double xsum=0.0,ysum=0.0;
        for(int a=0;a<k;a++){

            xsum+=posXlist[nearestPoints[a]];
            ysum+=posYlist[nearestPoints[a]];
            nearestDistances[a]=distanceList.get(nearestPoints[a]);
            System.out.println(posXlist[nearestPoints[a]]+","+posYlist[nearestPoints[a]]+" d="+nearestDistances[a]);
        }


        double[] arr=new double[2+k*3];

        return new double[]{};
    }

    public class LocatingResult{
        public double X;
        public double Y;
        public double distance;
    }

//    public static void WKNN(Map<String,Double> onrss, int k,int index){
//        //计算和线下的所有点的距离
//        List<Double> distanceList=new ArrayList<>(offline.offRssList.size());
//        for(int i=0;i<posXlist.length;i++){
//            Map<String,Double> offrss=offline.avgRssList.get(i);
//            double distance=0,sum=0;
//            for(int j=0;j<aplist.size();j++){
//                double off,on;
//                if(offrss.get(aplist.get(j))!=null)
//                    off=offrss.get(aplist.get(j));
//                else off=-100.0;
//                if(onrss.get(aplist.get(j))!=null)
//                    on=onrss.get(aplist.get(j));
//                else on=-100.0;
//                sum+=(off-on)*(off-on);
//            }
//            distance=Math.sqrt(sum);
//            distanceList.add(distance);
////			System.out.println(Constant.txts[index]+" distance "+": "+distance);//输出off与on的距离
//        }
//        //找到最近k个点的位置 得到结果
//        int []nearestpoints=Tools.findNNearest(k, distanceList);//第一个参数为取点的个数 返回distanceList当中的位置
//        double []weights=new double[k];
//        double x=0.0,y=0.0,weight=0.0,total=0.0;
//        for(int a=0;a<k;a++){
//            int pos=nearestpoints[a];
//            weights[a]=1/distanceList.get(pos);
//            weight+=1/distanceList.get(pos);
//            System.out.println(posXlist[pos]+","+posYlist[pos]+" d="+distanceList.get(pos)+" weight="+weights[a]);
//        }
//        System.out.println("total weight="+weight);
//        for(int b=0;b<k;b++){
//            int pos=nearestpoints[b];
//            x+=posXlist[pos]*weights[b]/weight;
//            y+=posYlist[pos]*weights[b]/weight;
////			System.out.println("x="+x+" y="+y+" "+posXlist[b]+" "+posY);
//        }
//        double []result={x,y};
//
//        //计算误差
//        double deviationX=result[0]-onXlist[index];
//        double deviationY=result[1]-onYlist[index];
//        double deviation=Math.sqrt(deviationX*deviationX+deviationY*deviationY);
//        deviationArr[index]=deviation;
//        System.out.println("result:"+result[0]+","+result[1]+"   true position:"+Constant.onPos[index]
//                +" deviation:"+deviation);
//    }
}
