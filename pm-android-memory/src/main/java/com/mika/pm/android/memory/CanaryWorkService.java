package com.mika.pm.android.memory;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Debug;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

import com.mika.pm.android.core.util.PMLog;
import com.mika.pm.android.memory.hproflib.HprofBufferShrinker;
import com.mika.pm.android.memory.model.HeapDump;
import com.mika.pm.android.memory.watcher.DumpStorageManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

/**
 * @Author: mika
 * @Time: 2019-11-05 15:43
 * @Description:
 */
public class CanaryWorkService extends IntentService {

    private static final String TAG = "CanaryWorkService";
    private static final String EXTRA_PARAM_HEAP_DUMP = "com.mika.pm.android.memory.work.HEAP_DUMP";

    public CanaryWorkService() {
        super(CanaryWorkService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    public static void shrinkHprofAndReport(Context context, HeapDump heapDump) {
        final Intent intent = new Intent(context, CanaryWorkService.class);
        intent.putExtra(EXTRA_PARAM_HEAP_DUMP, heapDump);
        if(Build.VERSION.SDK_INT >= 26){
            context.startForegroundService(intent);
        }else{
            context.startService(intent);
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        PMLog.d(TAG, "work thread is %s", Thread.currentThread().getName());
        HeapDump heapDump = (HeapDump) intent.getSerializableExtra(EXTRA_PARAM_HEAP_DUMP);
        if (heapDump != null) {
            doShrinkHprof(heapDump);
        }
    }

    private void doShrinkHprof(HeapDump heapDump) {
        File hprofDir = heapDump.getHprofFile().getParentFile();
        final File shrinkedHprofFile = new File(hprofDir, getShrinkHprofName(heapDump.getHprofFile()));
        final File zipHprofFile = new File(hprofDir, getResultZipName("dump_result_" + android.os.Process.myPid()));
        final File hprofFile = heapDump.getHprofFile();
        ZipOutputStream zof =null;

        try {
            long startTime = System.currentTimeMillis();
            new HprofBufferShrinker().shrink(hprofFile, shrinkedHprofFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getShrinkHprofName(File originHprof) {
        final String originHprofName = originHprof.getName();
        final int extPos = originHprofName.indexOf(DumpStorageManager.HPROF_EXT);
        final String namePrefix = originHprofName.substring(0, extPos);
        return namePrefix + "_shrink" + DumpStorageManager.HPROF_EXT;
    }

    private String getResultZipName(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append('_')
                .append(new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH).format(new Date()));
        return sb.toString();
    }
}
