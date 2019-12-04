package com.mika.perform;

import android.app.Application;
import android.view.View;

import com.mika.pm.android.core.PerformMonitor;
import com.mika.pm.android.memory.MemoryPlugin;
import com.mika.pm.android.memory.config.MemoryConfig;

import java.util.ArrayList;

/**
 * @Author: mika
 * @Time: 2019-11-04 15:30
 * @Description:
 */
public class App extends Application {

    public ArrayList<View> mLeakedViews = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        MemoryPlugin memoryPlugin = new MemoryPlugin(new MemoryConfig.Builder()
                .enableDumpHprof(true)
                .build());

        PerformMonitor.Builder builder = new PerformMonitor.Builder(this).plugin(memoryPlugin);

        PerformMonitor.init(builder.build());

    }
}
