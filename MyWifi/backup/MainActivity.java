package com.example.eric.mywifi.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import com.example.eric.mywifi.R;
import com.example.eric.mywifi.Storage.PrefUtils;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button btn_collect;
    Button btn_setting;
    Button btn_locate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        prepareData();
    }

    public void initViews(){
        btn_collect=(Button)findViewById(R.id.btn_collect);
        btn_setting=(Button)findViewById(R.id.btn_setting);
        btn_locate=(Button)findViewById(R.id.btn_locate);

        btn_collect.setOnClickListener(this);
        btn_locate.setOnClickListener(this);
        btn_setting.setOnClickListener(this);
    }

    public void prepareData(){
        PrefUtils.putInt(getApplicationContext(),PrefUtils.Keys.FREQUENCY,3);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_collect:
                readyGo(SiteSurveyActivity.class);
                break;
            case R.id.btn_locate:
                readyGo(LocatingActivity.class);
                break;
            case R.id.btn_setting:
                readyGo(SettingActivity.class);
                break;
        }
    }

    void readyGo(Class<?> clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }
}
