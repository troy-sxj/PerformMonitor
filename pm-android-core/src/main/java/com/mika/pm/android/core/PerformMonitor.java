package com.mika.pm.android.core;

import android.app.Application;

import com.mika.pm.android.core.plugin.DefaultPluginListener;
import com.mika.pm.android.core.plugin.Plugin;
import com.mika.pm.android.core.plugin.PluginListener;

import java.util.HashSet;

/**
 * @Author: mika
 * @Time: 2019-11-01 17:00
 * @Description:
 */
public final class PerformMonitor {

    private final HashSet<Plugin> plugins;
    private final Application application;
    private final PluginListener pluginListener;

    private static PerformMonitor sInstance;


    private PerformMonitor(Application application, HashSet<Plugin> plugins, PluginListener pluginListener) {
        this.plugins = plugins;
        this.application = application;
        this.pluginListener = pluginListener;
        for (Plugin plugin : plugins) {
            plugin.init(application, pluginListener);
            pluginListener.onInit(plugin);
        }
    }

    public static PerformMonitor init(PerformMonitor monitor) {
        synchronized (PerformMonitor.class) {
            if (sInstance == null) {
                sInstance = monitor;
            }
        }
        return sInstance;
    }

    public static PerformMonitor getInstance(){
        if(sInstance == null){
            throw new RuntimeException("PerformMonitor must be init first");
        }
        return sInstance;
    }

    public void startAllPlugins() {
        for (Plugin plugin : plugins) {
            plugin.start();
        }
    }

    public void stopAllPlugins() {
        for (Plugin plugin : plugins) {
            plugin.stop();
        }
    }

    public void destroyAllPlugins() {
        for (Plugin plugin : plugins) {
            plugin.destroy();
        }
    }

    public static class Builder {

        private final Application application;
        private PluginListener pluginListener;
        private HashSet<Plugin> plugins = new HashSet<>();

        public Builder(Application application) {
            this.application = application;
        }

        public Builder plugin(Plugin plugin) {
            String tag = plugin.getTag();
            for (Plugin exist : plugins) {
                if (tag.equals(exist.getTag())) {
                    return this;
                }
            }
            plugins.add(plugin);
            return this;
        }

        public Builder pluginListener(PluginListener pluginListener) {
            this.pluginListener = pluginListener;
            return this;
        }

        public PerformMonitor build() {
            if (pluginListener == null) {
                pluginListener = new DefaultPluginListener(application);
            }
            return new PerformMonitor(application, plugins, pluginListener);
        }

    }

}
