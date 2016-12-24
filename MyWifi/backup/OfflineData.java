package com.example.eric.mywifi.Locating;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Eric on 2016/8/9 0009.
 */
public class OfflineData {

    ArrayList<ArrayList<Map<String, Double>>> offRssList=new ArrayList<>();
    ArrayList<Map<String, Double>> avgRssList=new ArrayList<>();
    ArrayList<Map<String, Integer>>eachPosRssCountList=new ArrayList<Map<String, Integer>>();
    ArrayList <String> aplist=new ArrayList<>();

    private int size;

    private int neglect_frequency=5;
    private int collecting_times=25;//每个采集点的采集次数
    private static double defaultRSS=-100.0;
    private static double availableRSS=-80.0;

    OfflineData(int s){
        size=s;
    }


}
