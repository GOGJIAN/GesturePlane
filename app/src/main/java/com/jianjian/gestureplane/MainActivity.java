package com.jianjian.gestureplane;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.jianjian.gestureview.GesturePlane;


public class MainActivity extends AppCompatActivity {

    private GesturePlane mGesturePlane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGesturePlane = findViewById(R.id.gesture);
        mGesturePlane.setResultListener(new GesturePlane.ResultListener() {
            @Override
            public void onResult(String result) {
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
                if (!result.equals("123")) {
                    mGesturePlane.setError();
                }
            }
        });

    }
}
