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


    private PerformMonitor(Application application, HashSet<Plugin> plugins, PluginListener pluginListener) {
        this.plugins = plugins;
        this.application = application;
        this.pluginListener = pluginListener;
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
