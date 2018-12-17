package com.example.administrator.calculator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class history extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ActivityCollector.addActivity(this);//////////////////
        Intent intent = getIntent();
        String his = intent.getStringExtra("history");
        //String [] history = his.split(" ");
        String [] h = his.split(" ");
        String [] histroy = new String[h.length];
        for(int i = 0;i<h.length;i++)
        {
            histroy[i] = h[h.length-1-i];
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(history.this,android.R.layout.simple_list_item_1,histroy);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
    }
}
