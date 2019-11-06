package com.mika.pm.android.memory.watcher;

import android.content.Context;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.mika.pm.android.core.util.PMLog;
import com.mika.pm.android.memory.model.HeapDump;

import java.io.File;
import java.io.IOException;

/**
 * @Author: mika
 * @Time: 2019-11-05 11:37
 * @Description:
 */
public class AndroidHeapDumper {

    private static final String TAG = "AndroidHeapDumper";

    private final Context mContext;
    private final DumpStorageManager mDumpStorageManager;
    private final Handler mMainHandler;


    public interface HeapDumpHandler{
        void process(HeapDump result);
    }

    public AndroidHeapDumper(Context context, DumpStorageManager dumpStorageManager){
        this(context, dumpStorageManager, new Handler(Looper.getMainLooper()));
    }

    public AndroidHeapDumper(Context context, DumpStorageManager dumpStorageManager, Handler mainHandler) {
        this.mContext = context;
        this.mDumpStorageManager = dumpStorageManager;
        this.mMainHandler = mainHandler;
    }

    public File dumpHeap(){
        final File hprofFile = mDumpStorageManager.newHprofFile();

        if (null == hprofFile) {
            PMLog.w(TAG, "hprof file is null.");
            return null;
        }

        final File hprofDir = hprofFile.getParentFile();
        if (hprofDir == null) {
            PMLog.w(TAG, "hprof file path: %s does not indicate a full path.", hprofFile.getAbsolutePath());
            return null;
        }

        if (!hprofDir.canWrite()) {
            PMLog.w(TAG, "hprof file path: %s cannot be written.", hprofFile.getAbsolutePath());
            return null;
        }

        try {
            Debug.dumpHprofData(hprofFile.getAbsolutePath());
            return hprofFile;
        }catch (IOException e){
            PMLog.printErrStackTrace(TAG, e, "failed to dump heap into file: %s.", hprofFile.getAbsolutePath());
            return null;
        }
    }
}
