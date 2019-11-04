package com.mika.pm.android.core.plugin;

import android.app.Application;

/**
 * @Author: mika
 * @Time: 2019-11-01 17:42
 * @Description:
 */
public interface IPlugin {

    Application getApplication();

    void init(Application application, PluginListener pluginListener);

    void start();

    void stop();

    void destroy();

    String getTag();

    void onForeground(boolean isForeground);
}
