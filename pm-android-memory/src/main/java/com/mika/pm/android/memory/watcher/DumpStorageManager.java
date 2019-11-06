package com.mika.pm.android.memory.watcher;

import android.content.Context;
import android.os.Environment;

import com.mika.pm.android.core.util.PMLog;

import java.io.File;
import java.io.FilenameFilter;
import java.util.UUID;

/**
 * @Author: mika
 * @Time: 2019-11-05 11:05
 * @Description:
 */
public class DumpStorageManager {

    private static final String TAG = "DumpStorageManager";
    public static final String HPROF_EXT = ".hprof";
    private static final int DEFAULT_MAX_STORED_HPROF_FILECOUNT = 5;

    protected final Context context;
    protected final int mMaxStoredHprofFileCount;

    public DumpStorageManager(Context context) {
        this(context, DEFAULT_MAX_STORED_HPROF_FILECOUNT);
    }

    public DumpStorageManager(Context context, int maxStoredHprofFileCount) {
        this.context = context;
        this.mMaxStoredHprofFileCount = maxStoredHprofFileCount;
    }

    public File newHprofFile() {
        final File storageDir = prepareStorageDirectory();
        if (storageDir == null) {
            return null;
        }
        final UUID uuid = UUID.randomUUID();
        final String hprofFileName = "dump_"
                + Long.toHexString(uuid.getMostSignificantBits())
                + Long.toHexString(uuid.getLeastSignificantBits())
                + HPROF_EXT;
        return new File(storageDir, hprofFileName);
    }

    private File prepareStorageDirectory() {
        final File storageDir = getStorageDirectory();
        if (!storageDir.exists() && (!storageDir.mkdir() || !storageDir.canWrite())) {
            return null;
        }
        final File[] hprofFiles = storageDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return name.endsWith(HPROF_EXT);
            }
        });
        if (hprofFiles != null && hprofFiles.length > mMaxStoredHprofFileCount) {
            for (File file : hprofFiles) {
                if (file.exists() && !file.delete()) {
                    PMLog.w(TAG, "failed to delete hprof file: " + file.getAbsolutePath());
                }
            }
        }
        return storageDir;
    }

    private File getStorageDirectory() {
        final String sdcardState = Environment.getExternalStorageState();
        File root = null;
        if (Environment.MEDIA_MOUNTED.equals(sdcardState)) {
            root = context.getExternalCacheDir();
        } else {
            root = context.getCacheDir();
        }

        final File result = new File(root, "memory_dump");
        PMLog.i(TAG, "path to store hprof and result: %s", result.getAbsolutePath());
        return result;
    }
}
