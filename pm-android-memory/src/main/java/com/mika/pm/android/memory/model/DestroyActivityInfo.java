package com.mika.pm.android.memory.model;

import android.app.Activity;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

/**
 * @Author: mika
 * @Time: 2019-11-05 14:34
 * @Description:
 */
public class DestroyActivityInfo {

    public final String mKey;
    public final String mActivityName;

    public final WeakReference<Activity> mActivityRef;
    public final long mLastCreatedActivityCount;
    public int mDetectedCount = 0;

    public DestroyActivityInfo(String mKey, Activity activity, String activityName, long mLastCreatedActivityCount) {
        this.mKey = mKey;
        this.mActivityName = activityName;
        this.mActivityRef = new WeakReference<>(activity);
        this.mLastCreatedActivityCount = mLastCreatedActivityCount;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof DestroyActivityInfo){
            return ((DestroyActivityInfo) obj).mKey.equals(this.mKey);
        }
        return super.equals(obj);
    }
}
