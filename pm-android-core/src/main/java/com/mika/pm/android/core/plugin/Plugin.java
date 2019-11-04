package com.mika.pm.android.core.plugin;

import android.app.Application;

/**
 * @Author: mika
 * @Time: 2019-11-01 17:42
 * @Description:
 */
public abstract class Plugin implements IPlugin {

    private PluginListener pluginListener;
    private Application application;


    @Override
    public void init(Application application, PluginListener pluginListener) {
        this.application = application;
        this.pluginListener = pluginListener;
    }

    @Override
    public Application getApplication() {
        return application;
    }

    @Override
    public String getTag() {
        return getClass().getName();
    }

    @Override
    public void start() {
        if(pluginListener == null){
            throw new RuntimeException("Plugin start, plugin listener is null");
        }
        pluginListener.onStart(this);
    }

    @Override
    public void stop() {
        if(pluginListener == null){
            throw new RuntimeException("Plugin stop, plugin listener is null");
        }
        pluginListener.onStop(this);
    }

    @Override
    public void destroy() {
        if(pluginListener == null){
            throw new RuntimeException("Plugin destroy, plugin listener is null");
        }
        pluginListener.onDestory(this);
    }

    @Override
    public void onForeground(boolean isForeground) {

    }
}
