package com.mika.perform;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @Author: mika
 * @Time: 2019-11-12 11:06
 * @Description:
 */
public class LeakActivity extends AppCompatActivity {

    private boolean isLeak = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leak);
        TextView tvLeak = findViewById(R.id.tvLeak);
        App app = (App) getApplication();
        app.mLeakedViews.add(tvLeak);
        isLeak = true;
    }
}
