package com.mysiga.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mysiga.demo.view.NumWheelView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NumWheelView numWheelView = (NumWheelView) findViewById(R.id.num_wheel);
        List numList = new ArrayList();
        for (int i = 1; i < 100; i++) {
            numList.add(String.valueOf(i));
        }
        numWheelView.setItems(numList);
        numWheelView.setOnSelectedListener(new NumWheelView.OnSelectedListener() {
            @Override
            public void onSelected(int selectedIndex, String item) {

            }
        });

    }

}
