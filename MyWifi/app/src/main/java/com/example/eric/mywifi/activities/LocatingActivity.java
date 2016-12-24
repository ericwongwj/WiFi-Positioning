package com.example.eric.mywifi.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eric.mywifi.Locating.LocatingMethod;
import com.example.eric.mywifi.Locating.OfflineData;
import com.example.eric.mywifi.Locating.Tools;
import com.example.eric.mywifi.R;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class LocatingActivity extends AppCompatActivity implements View.OnClickListener{

    WifiManager wifi;
    List<String> wifilist =new ArrayList<>();

    Button btnStartEnd;
    Button btnSetTime;
    Button btnMode;
    Button btnOneTime;
    TextView tvPos;
    TextView tvNearestPos;
    ListView lv;

    static ArrayAdapter<String> listadapter;

    double x=0.0;
    double y=0.0;
    int locatingFreq=2;

    public static final String tag="Mywifi";

    private OfflineData offlineData;
    static ArrayList<String> aplist=new ArrayList<>();

    private Timer timer;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    listadapter.notifyDataSetChanged();
                    tvPos.setText(x+","+y);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locating);

        InputStream offrssis=getResources().openRawResource(R.raw.offrss);
        InputStream apis=getResources().openRawResource(R.raw.aplist);

        Tools.initAP(aplist,apis);
        offlineData = new OfflineData(aplist, offrssis);
        showToast("数据初始化完毕");

        initWifiManager();
        initViews();

        timer = new Timer("apUpdates");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                normalTimerTask();
            }
        },0,2000);
    }

    public void initViews(){
//        btnSetTime=(Button)findViewById(R.id.btn_set_time);
        btnStartEnd=(Button)findViewById(R.id.btn_start_end);
        tvPos=(TextView)findViewById(R.id.tv_pos);
        btnMode=(Button)findViewById(R.id.btn_locating_mode);
        btnOneTime=(Button)findViewById(R.id.btn_onetime_pos);
        tvNearestPos=(TextView)findViewById(R.id.tv_content);
        btnMode.setOnClickListener(this);
//        btnSetTime.setOnClickListener(this);
        btnStartEnd.setOnClickListener(this);
        btnStartEnd.setText("Start");

//        lv=(ListView)findViewById(R.id.lv_pos_wifi);
        listadapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, wifilist);
        lv.setAdapter(listadapter);
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
        wifilist.clear();

        for(ScanResult result :results){
            String info=result.BSSID +":"+result.level+"dBm";
            wifilist.add(info);
        }
        Collections.sort(wifilist);
        Message msg=new Message();
        msg.what=1;
        handler.sendMessage(msg);
    }

    public void locatingTimerTask(){
        wifi.startScan();
        List<ScanResult> results=wifi.getScanResults();
        wifilist.clear();
        Map<String,Double> onrss=new HashMap<>();
        for(ScanResult result :results){
            String info=result.BSSID +":"+result.level+"dBm";
            wifilist.add(info);
            onrss.put(result.BSSID,(double)result.level);
        }
        LocatingMethod.KNN(offlineData,onrss,aplist,4);
        String content=null;
        tvNearestPos.setText(content);
        Message msg=new Message();
        msg.what=1;
        handler.sendMessage(msg);
    }

    public void beginLocating(){
        showToast("已开始定位");
        timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                locatingTimerTask();
            }
        }, 100, locatingFreq * 1000);
    }

    public void stopLocating(){
        timer.cancel();
        tvPos.setText("0,0");
        restartNormalTimer();
        showToast("已停止定位");
    }

    public void restartNormalTimer(){
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                normalTimerTask();
            }
        },0,2000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start_end://本地化的开始定位应该要先进入读取版本
                if(btnStartEnd.getText().toString().equals("Start")){
                    btnStartEnd.setText("Locating...");
                    beginLocating();
                }else if(btnStartEnd.getText().toString().equals("Locating...")){
                    btnStartEnd.setText("Start");
                    stopLocating();
                }
                break;
//            case R.id.btn_set_time:
//                setLocatingFreq();
//                break;
            case R.id.btn_locating_mode:
                showToast("Mode");
                break;
        }
    }

    private void showToast(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    //只在非定位时使用
    private void setLocatingFreq(){
        final EditText et = new EditText(this);
        new AlertDialog.Builder(this).setTitle("输入定位时间间隔")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = et.getText().toString();
                        if (input.equals("")) {
                            Toast.makeText(getApplicationContext(), "内容不能为空！" + input, Toast.LENGTH_LONG).show();
                        }
                        else {
                            locatingFreq=Integer.valueOf(input);
                            timer.cancel();
                            timer=new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    locatingTimerTask();
                                }
                            },100,2000);
                        }
                    }
                })
                .show();
    }
}
