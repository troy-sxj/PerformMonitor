package com.mika.pm.android.core.plugin;

import com.mika.pm.android.core.report.Issue;

/**
 * @Author: mika
 * @Time: 2019-11-01 17:58
 * @Description:
 */
public interface PluginListener {

    void onInit(Plugin plugin);

    void onStart(Plugin plugin);

    void onStop(Plugin plugin);

    void onDestory(Plugin plugin);

    void onReportIssue(Issue issue);
}
