package com.example.eric.mywifi.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eric.mywifi.R;
import com.example.eric.mywifi.Storage.MyDBOpenHelper;
import com.example.eric.mywifi.Storage.PrefUtils;
import com.example.eric.mywifi.activities.RecordListActivity;
import com.example.eric.mywifi.activities.SettingActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SiteSurveyActivity extends AppCompatActivity implements View.OnClickListener{

    Timer timer;
    WifiManager wifi;

    ListView lv;
    TextView tv_tag;
    TextView tv_times;

    public List<String> aplist=new ArrayList<>();
    List<String> infolist=new ArrayList<>();
    static ArrayList<String> recordList=new ArrayList<>();

    static ArrayAdapter<String> listadapter;

    MyDBOpenHelper dbOpenHelper;
    SQLiteDatabase dbw;
    SQLiteDatabase dbr;

    String currentTable;
    static final String AP_TABLE="aptable";

    Date date;
    int recordfrequency;
    int showfrequency=1;
    static int times=0;
    final File sdcard= Environment.getExternalStorageDirectory();

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1://normal
                    listadapter.notifyDataSetChanged();
                    break;
                case 2://collect
                    listadapter.notifyDataSetChanged();
                    times++;
                    tv_times.setText(times+"");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_survey);

        preparedata();
        initViews();
        initWifiManager();

        timer = new Timer("apUpdates");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                normalTimerTask();
            }
        },0,showfrequency*1000);
    }

    public void initWifiManager(){
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled())
            if (wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLING)
                wifi.setWifiEnabled(true);
    }

    public void initViews(){
        lv=(ListView)findViewById(R.id.lv_wifilist);
        listadapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,infolist);
        lv.setAdapter(listadapter);

        findViewById(R.id.btn_record).setOnClickListener(this);
        findViewById(R.id.btn_begin).setOnClickListener(this);
        findViewById(R.id.btn_stop).setOnClickListener(this);

        tv_tag=(TextView)findViewById(R.id.tv_positiontag);
        tv_times=(TextView)findViewById(R.id.tv_times);
    }

    public void preparedata(){
        recordfrequency= PrefUtils.getInt(getApplicationContext(),PrefUtils.Keys.FREQUENCY);
        dbOpenHelper=new MyDBOpenHelper(getApplicationContext(),"wifirss.db",null,1);
        dbw=dbOpenHelper.getWritableDatabase();
        dbr=dbOpenHelper.getReadableDatabase();

//        db=dbOpenHelper.getReadableDatabase();
//        Cursor cursor=db.query("test", null, null, null, null, null, null);//第二个参数可以是new String[]{"name"}
//        while(cursor.moveToNext()){
//            double rss1=cursor.getDouble(cursor.getColumnIndex("rss1"));
//            double rss2= cursor.getDouble(cursor.getColumnIndex("rss2"));
//            Log.i("rss1="+rss1," rss2="+rss2);
//        }
    }


    public void createTable(String tname){
        dbw.execSQL("create table "+tname+" (_id integer primary key autoincrement, ap text)");
        dbw.execSQL("create table "+AP_TABLE+" (_id integer primary key autoincrement, ap text)");
    }

    public void addNewRssToDb(String tname,String colunmName){
        dbw.execSQL("ALTER TABLE "+tname+" ADD "+colunmName+" DOUBLE");
    }

    public void addNewAPToDb(String tname,String colunmName){
        dbw.execSQL("insert into "+AP_TABLE+"(ap)");
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
        handler.sendMessage(msg);
    }

    public void recordTimerTask(){
        wifi.startScan();
        List<ScanResult> results=wifi.getScanResults();
        infolist.clear();
        date=new Date();
        if(times==0){
            for(ScanResult result :results){
                ContentValues cv=new ContentValues();
                cv.put("ap",result.BSSID);
                dbw.insert("ap"+currentTable,null,cv);
                dbw.insert(currentTable,null,cv);
            }
        }
        addNewRssToDb(currentTable,"rss"+times);
        for(ScanResult result :results){
            String info=result.BSSID +":"+result.level+"dBm";
            infolist.add(info);
            ContentValues cv=new ContentValues();
            cv.put("rss"+times,result.level);
            dbw.insert(currentTable,null,cv);

            if(!aplist.contains(result.BSSID))
                aplist.add(result.BSSID);

        }
        Message msg=new Message();
        msg.what=2;
        handler.sendMessage(msg);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_begin:
                beginRecord();
                break;
            case R.id.btn_stop:
                stopRecord();
                break;
            case R.id.btn_record:
                startActivity(new Intent(this,RecordListActivity.class));
                break;
        }
    }

    public void beginRecord(){
        final EditText et = new EditText(this);
        new AlertDialog.Builder(this).setTitle("输入地点标签")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = et.getText().toString();
                        if (input.equals("")) {
                            Toast.makeText(getApplicationContext(), "内容不能为空！" + input, Toast.LENGTH_LONG).show();
                        }
                        else {
                            tv_tag.setText(input);
                            createTable(input);
                            currentTable=input;
                            timer.cancel();
                            timer=new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    recordTimerTask();
                                }
                            },100,recordfrequency*1000);
                        }
                    }
                })
                .show();
    }

    public void stopRecord(){
        timer.cancel();
        restartNormalTimer();
        times=0;
        recordList.add(currentTable);

        Cursor cursor=dbr.query("ap"+currentTable, null, null, null, null, null, null);//第二个参数可以是new String[]{"name"}
        while(cursor.moveToNext()){
            String name=cursor.getString(cursor.getColumnIndex("ap"));
            System.out.println(String.format("test:ap=%s",name));
        }

        Toast.makeText(this,"已停止",Toast.LENGTH_SHORT).show();

    }

    public void restartNormalTimer(){
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                normalTimerTask();
            }
        },0,showfrequency*1000);
    }

}
