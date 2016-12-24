package com.example.eric.mywifi.activities;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.eric.mywifi.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocatingActivity extends AppCompatActivity {

    WifiManager wifi;
    List<String> infolist=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locating);
    }

    public void initWifiManager(){
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled())
            if (wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLING)
                wifi.setWifiEnabled(true);
    }

    public void normalTimerTask(){
        wifi.startScan();
        List<ScanResult> results=wifi.getScanResults();
        infolist.clear();

        for(ScanResult result :results){
            String info=result.BSSID +":"+result.level+"dBm";
            infolist.add(info);
        }
        Collections.sort(infolist);
        Message msg=new Message();
        msg.what=1;
//        handler.sendMessage(msg);
    }
}
