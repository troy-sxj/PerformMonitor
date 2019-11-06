package com.mika.pm.android.memory;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.mika.pm.android.memory.model.HeapDump;
import com.mika.pm.android.memory.watcher.DumpStorageManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

/**
 * @Author: mika
 * @Time: 2019-11-05 15:43
 * @Description:
 */
public class CanaryWorkService extends JobIntentService {

    private static final int JOB_ID = 0xFAFBFCFD;

    private static final String ACTION_SHRINK_HPROF = "com.mika.pm.android.memory.work.SHRINK_HPROF";
    private static final String EXTRA_PARAM_HEAP_DUMP = "com.mika.pm.android.memory.work.HEAP_DUMP";

    public static void shrinkHprofAndReport(Context context, HeapDump heapDump) {
        final Intent intent = new Intent(context, CanaryWorkService.class);
        intent.setAction(ACTION_SHRINK_HPROF);
        intent.putExtra(EXTRA_PARAM_HEAP_DUMP, heapDump);
        enqueueWork(context, CanaryWorkService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        final String action = intent.getAction();
        if (ACTION_SHRINK_HPROF.equals(action)) {
            intent.setExtrasClassLoader(this.getClassLoader());
            HeapDump heapDump = (HeapDump) intent.getSerializableExtra(EXTRA_PARAM_HEAP_DUMP);
            if (heapDump != null) {
                doShrinkHprof(heapDump);
            }
        }
    }

    private void doShrinkHprof(HeapDump heapDump) {
        File hprofDir = heapDump.getHprofFile().getParentFile();
        final File shrinkedHprofFile = new File(hprofDir, getShrinkHprofName(heapDump.getHprofFile()));
        final File zipHprofFile = new File(hprofDir, getResultZipName("dump_result_" + android.os.Process.myPid()));
        final File hprofFile = heapDump.getHprofFile();
        ZipOutputStream zof =null;

        long startTime = System.currentTimeMillis();
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
