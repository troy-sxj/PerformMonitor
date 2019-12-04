package com.mika.pm.android.core.plugin;

import android.content.Context;

import com.mika.pm.android.core.report.Issue;

/**
 * @Author: mika
 * @Time: 2019-11-01 17:59
 * @Description:
 */
public class DefaultPluginListener implements PluginListener {

    private final Context context;

    public DefaultPluginListener(Context context) {
        this.context = context;
    }

    @Override
    public void onInit(Plugin plugin) {

    }

    @Override
    public void onStart(Plugin plugin) {

    }

    @Override
    public void onStop(Plugin plugin) {

    }

    @Override
    public void onDestroy(Plugin plugin) {

    }

    @Override
    public void onReportIssue(Issue issue) {

    }
}
