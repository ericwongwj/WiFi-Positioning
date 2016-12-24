package com.example.eric.mywifi.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.eric.mywifi.R;
import com.example.eric.mywifi.Storage.PrefUtils;

public class SettingActivity extends AppCompatActivity {

    EditText et_freq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initViews();
        prepareData();
    }

    void initViews(){
        et_freq=(EditText)findViewById(R.id.edittext_update_frequency);
        et_freq.setText(PrefUtils.getInt(getApplicationContext(),PrefUtils.Keys.FREQUENCY)+"");
        findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!((et_freq.getText().equals("")||et_freq.getText()==null))){
                    int updateFrequency = Integer.parseInt(et_freq.getText().toString());
                    PrefUtils.putInt(getApplicationContext(),PrefUtils.Keys.FREQUENCY,updateFrequency);
                    SettingActivity.this.finish();
                }
            }
        });
    }

    void prepareData(){
        int f=PrefUtils.getInt(getApplicationContext(),PrefUtils.Keys.FREQUENCY);
       // et_freq.setText(f);
    }

}
