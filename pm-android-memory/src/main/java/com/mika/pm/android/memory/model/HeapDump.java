package com.mika.pm.android.memory.model;

import java.io.File;
import java.io.Serializable;

/**
 * @Author: mika
 * @Time: 2019-11-05 11:37
 * @Description:
 */
public class HeapDump implements Serializable {

    private final File mHprofFile;
    private final String mRefKey;
    private final String mActivityName;

    public HeapDump(File hprofFile, String key, String activityName) {
        mHprofFile = hprofFile;
        mRefKey = key;
        mActivityName = activityName;
    }

    public File getHprofFile() {
        return mHprofFile;
    }

    public String getRefKey() {
        return mRefKey;
    }

    public String getActivityName() {
        return mActivityName;
    }
}
