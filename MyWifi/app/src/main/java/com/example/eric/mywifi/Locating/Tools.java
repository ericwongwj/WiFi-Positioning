package com.example.eric.mywifi.Locating;

import android.util.Log;

import com.example.eric.mywifi.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Eric on 2016/8/9 0009.
 */
public class Tools {

    static String TAG="Mywifi";

    public static String getString(InputStream inputStream) {
        StringBuffer sb = new StringBuffer("");
        try {
            InputStreamReader isr = new InputStreamReader(inputStream, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            br.close();
            isr.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void initAP(List<String> aplist, InputStream apis){
        try {
            InputStreamReader isr = new InputStreamReader(apis, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String line;
            while((line=br.readLine())!=null)
                aplist.add(line);
            br.close();
            isr.close();
            apis.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    //传入的是narrow之后的field（一个位置索引列表）
    public static int[] findNNearest(int pnum, ArrayList<Double> dlist){//param：point number
        int []narr=new int[pnum];
        int i=0;
        Double[]darr=new Double[dlist.size()];
        dlist.toArray(darr);
        while(i<pnum){
            double min=darr[0];
            for(int j=0;j<darr.length-1;j++){
                if(min>darr[j+1]){
                    min=darr[j+1];
                }
            }
            narr[i]=dlist.indexOf(min);
            darr[narr[i]]=1000.0;
            i++;
        }
        return narr;
    }

    public static double calculateDistance(Map<String, Double> mapoff, Map<String, Double> mapon, List<String> aplist){//计算两个向量之间的距离
        double sum=0;
        for(int i=0;i<aplist.size();i++){
            double on=mapon.get(aplist.get(i));
            double off=-100.0;
            if(i!=aplist.size()-1)
                off=mapoff.get(aplist.get(i));
            sum+=(on-off)*(on-off);
        }
        return Math.sqrt(sum);
    }

    public static double[] calculatePosition(double []xarr,double []yarr,double []darr,int length, double p){
        double[] result=new double[2];//0:x 1:y
        double wtotal=0;
        for(int i=0;i<length;i++){
            wtotal+=Math.pow(1/darr[i], p);//darr[i]:距离的反比
        }
        for(int i=0;i<length;i++){
            double a=Math.pow(darr[i], p);
            result[0]+=xarr[i]/(wtotal*a);
            result[1]+=yarr[i]/(wtotal*a);
        }
        return result;
    }

    public static double calculateDeviation(double xC, double yC, double xO, double yO){
        double d=(xC-xO)*(xC-xO)+(yC-yO)*(yC-yO);
        return Math.sqrt(d);
    }

    /**返回一个缩小的搜索空间的位置索引表*/
    static ArrayList<Integer> narrowSearchField(int pointid){
        ArrayList<Integer> nearestPosList;//存放的序号代表positionlist的序号
        int[] similarityArr=new int[130];//分别对29个ap出现的次数进行计数
        for(int i=0;i<130;i++)
            similarityArr[i]=0;
        int posid=0;
		/*for(ArrayList<Integer>apvector:offApVectorlist){//对130个位置进行遍历
			int k=0;
			for(Integer j:onApVectorlist.get(pointid)){//仅仅对第pointid个点的ap列表进行比较 apvector里面是代表有无的01
				if(apvector.get(k).equals(j)){
					similarityArr[posid]++;
				}
				k++;
			}
			posid++;
		}*/
        nearestPosList=findSimilarVector(similarityArr);		//找到这4orN个的点在offpositionlist当中的位置
        return nearestPosList;
    }

    static ArrayList<Integer> findSimilarVector(int[] sArr){
        ArrayList<Integer> spos=new ArrayList<>();
        for(int i=0;i<sArr.length-1;i++){
            if(sArr[i]>24){/***********这个次数是一个需要调教的参数 而且这样判断有问题 24是能跑完的临界值*************/
                spos.add(i);//c=11时 只有3个
            }
            //System.out.println(i+" similar:"+sArr[i]);
        }
        return spos;
    }

    public static void displayAllRSS(ArrayList<ArrayList<Map<String, Double>>> offRssList, ArrayList<String> aplist){
        for(int i=0;i<offRssList.size();i++){
            System.out.println(i+1+"th pos");
            ArrayList<Map<String,Double>> eachpos=offRssList.get(i);
            for(int j=0;j<eachpos.size();j++){
                Map<String,Double> eachtime=eachpos.get(j);
                System.out.println("time "+j);
                for(String ap:aplist){
                    if(eachtime.get(ap)!=null)
                        System.out.println(ap+" "+eachtime.get(ap)+"dBm");
                }

            }
        }
    }

    public static void showList(List<String> list){
        Log.i(TAG,"size=" + list.size());
        for(int i=0;i<list.size();i++){
            Log.i(TAG,list.get(i));
        }
    }

    public static void cleanArr(int[] cnt, double[] sum){
        for(int i=0;i<cnt.length;i++){
            cnt[i]=0;
            sum[i]=0;
        }
    }

}
