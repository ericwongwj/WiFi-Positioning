package com.example.eric.mywifi.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.eric.mywifi.R;
import com.example.eric.mywifi.activities.MainActivity;

public class RecordListActivity extends AppCompatActivity {

    ListView lv_record;
    TextView tv_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        //应该从数据库读取 标上文件的日期
        lv_record=(ListView)findViewById(R.id.lv_record);
        tv_number=(TextView)findViewById(R.id.tv_recordnumber);

        Integer num= SiteSurveyActivity.recordList.size();
        tv_number.append(num.toString());

        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,SiteSurveyActivity.recordList);
        lv_record.setAdapter(adapter);
    }
}
