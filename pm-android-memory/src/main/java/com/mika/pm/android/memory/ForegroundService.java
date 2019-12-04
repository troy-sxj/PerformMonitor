package com.mika.pm.android.memory;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * @Author: mika
 * @Time: 2019-11-07 10:34
 * @Description:
 */
public abstract class ForegroundService extends IntentService {

    public ForegroundService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("Profile Monitor")
                .setContentText("analyze dump file");

        startForeground(R.string.app_name, builder.build());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        onHandleIntentInForeground(intent);
    }

    protected abstract void onHandleIntentInForeground(Intent intent);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}
