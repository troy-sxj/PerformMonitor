package com.mika.pm.android.memory;

import android.app.Application;

import com.mika.pm.android.core.plugin.Plugin;
import com.mika.pm.android.core.plugin.PluginListener;
import com.mika.pm.android.core.util.PMLog;
import com.mika.pm.android.memory.config.MemoryConfig;
import com.mika.pm.android.memory.tracer.MemoryTracer;
import com.mika.pm.android.memory.watcher.ActivityRefWatcher;

/**
 * @Author: mika
 * @Time: 2019-11-01 17:28
 * @Description:
 */
public class MemoryPlugin extends Plugin {

    private static final String TAG = MemoryPlugin.class.getSimpleName();

    private MemoryConfig mConfig;
    private ActivityRefWatcher mWatcher;
    private MemoryTracer memoryTracer;



    public MemoryPlugin(MemoryConfig config) {
        this.mConfig = config;
    }

    @Override
    public void init(Application application, PluginListener pluginListener) {
        super.init(application, pluginListener);
        PMLog.i(TAG, "Memory Plugin init, config %s", mConfig.toString());

        mWatcher = new ActivityRefWatcher(application, this);
        memoryTracer = new MemoryTracer();
    }

    @Override
    public void start() {
        super.start();
        mWatcher.start();
    }

    public MemoryConfig getConfig(){
        return mConfig;
    }
}
