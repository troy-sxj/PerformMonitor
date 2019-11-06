package com.mika.pm.android.memory.watcher;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mika.pm.android.core.util.MonitorHandlerThread;
import com.mika.pm.android.core.util.PMLog;
import com.mika.pm.android.memory.CanaryWorkService;
import com.mika.pm.android.memory.MemoryPlugin;
import com.mika.pm.android.memory.config.MemoryConfig;
import com.mika.pm.android.memory.model.DestroyActivityInfo;
import com.mika.pm.android.memory.model.HeapDump;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author: mika
 * @Time: 2019-11-05 10:44
 * @Description:
 */
public class ActivityRefWatcher implements Watcher {

    private static final String TAG = "ActivityRefWatcher";
    private static final String ACTIVITY_REFKEY_PREFIX = "MONITOR_MEMORY_REFKEY_";

    private final MemoryPlugin memoryPlugin;

    private final RetryableTaskExecutor mDetectExecutor;
    private final int mMaxRedetectTimes;
    private final DumpStorageManager mDumpStorageManager;
    private final AndroidHeapDumper mAndroidHeapDumper;
    private final AndroidHeapDumper.HeapDumpHandler mHeapDumpHandler;

    private final ConcurrentLinkedDeque<DestroyActivityInfo> mDestroyActivityInfos;
    private final AtomicLong mCurrentCreatedActivityCount;

    public static class ComponentFactory {

        protected RetryableTaskExecutor createDetectExecutor(MemoryConfig config, HandlerThread handlerThread) {
            return new RetryableTaskExecutor(config.getCollectInterval(), handlerThread);
        }

        protected DumpStorageManager createDumpStorageManager(Context context) {
            return new DumpStorageManager(context);
        }

        protected AndroidHeapDumper createHeapDumper(Context context, DumpStorageManager dumpStorageManager) {
            return new AndroidHeapDumper(context, dumpStorageManager);
        }

        protected AndroidHeapDumper.HeapDumpHandler createHeapDumpHandler(final Context context, MemoryConfig config) {
            return new AndroidHeapDumper.HeapDumpHandler() {
                @Override
                public void process(HeapDump result) {
                    CanaryWorkService.shrinkHprofAndReport(context, result);
                }
            };
        }
    }

    public ActivityRefWatcher(Application app, final MemoryPlugin memoryPlugin) {
        this(app, memoryPlugin, new ComponentFactory() {

            @Override
            protected DumpStorageManager createDumpStorageManager(Context context) {
                return super.createDumpStorageManager(context);
            }

            @Override
            protected AndroidHeapDumper createHeapDumper(Context context, DumpStorageManager dumpStorageManager) {
                return super.createHeapDumper(context, dumpStorageManager);
            }

            @Override
            protected AndroidHeapDumper.HeapDumpHandler createHeapDumpHandler(Context context, MemoryConfig config) {
                return super.createHeapDumpHandler(context, config);
            }
        });
    }

    private ActivityRefWatcher(Application app, MemoryPlugin memoryPlugin, ComponentFactory componentFactory) {
        this.memoryPlugin = memoryPlugin;
        final MemoryConfig config = memoryPlugin.getConfig();
        HandlerThread handlerThread = MonitorHandlerThread.getDefaultHandlerThread();
        mDetectExecutor = componentFactory.createDetectExecutor(config, handlerThread);
        mMaxRedetectTimes = config.getMaxRedetectTimes();
        mDumpStorageManager = componentFactory.createDumpStorageManager(app);
        mAndroidHeapDumper = componentFactory.createHeapDumper(app, mDumpStorageManager);
        mHeapDumpHandler = componentFactory.createHeapDumpHandler(app, config);

        mDestroyActivityInfos = new ConcurrentLinkedDeque<>();
        mCurrentCreatedActivityCount = new AtomicLong(0);
    }


    @Override
    public void start() {
        final Application app = memoryPlugin.getApplication();
        if (app != null) {
            app.registerActivityLifecycleCallbacks(mRemovedActivityMonitor);
//            scheduleDetectProcedure();
        }

        //TODO for test
        if (mAndroidHeapDumper != null) {
            File hprofFile = mAndroidHeapDumper.dumpHeap();
            if (hprofFile != null && !mDestroyActivityInfos.isEmpty()) {
                DestroyActivityInfo destroyedActivityInfo = mDestroyActivityInfos.getFirst();
                HeapDump heapDump = new HeapDump(hprofFile, destroyedActivityInfo.mKey, destroyedActivityInfo.mActivityName);
                mHeapDumpHandler.process(heapDump);
            }
        }
    }

    @Override
    public void stop() {
        final Application app = memoryPlugin.getApplication();
        if (app != null) {
            app.unregisterActivityLifecycleCallbacks(mRemovedActivityMonitor);
            unScheduleDetectProcedure();
        }
    }

    @Override
    public void destroy() {

    }

    private void scheduleDetectProcedure() {
        mDetectExecutor.executeInBackground(mScanDestroyActivitiesTask);
    }

    private void unScheduleDetectProcedure() {
        mDetectExecutor.clearTasks();
        mDestroyActivityInfos.clear();
        mCurrentCreatedActivityCount.set(0);
    }


    private void pushDestroyActivityInfo(Activity activity) {
        final String activityName = activity.getClass().getName();
        if (!mDestroyActivityInfos.contains(activity)) {
            UUID uuid = UUID.randomUUID();
            StringBuilder keyBuilder = new StringBuilder();
            keyBuilder.append(ACTIVITY_REFKEY_PREFIX)
                    .append('_')
                    .append(Long.toHexString(uuid.getMostSignificantBits()))
                    .append(Long.toHexString(uuid.getLeastSignificantBits()));
            final DestroyActivityInfo destroyActivityInfo = new DestroyActivityInfo(keyBuilder.toString(), activity,
                    activityName, mCurrentCreatedActivityCount.get());
            mDestroyActivityInfos.add(destroyActivityInfo);
        }
    }

    private final Application.ActivityLifecycleCallbacks mRemovedActivityMonitor = new ActivityLifeCycleCallbacksAdapter() {

        private int mAppStatusCounter = 0;


        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
            mCurrentCreatedActivityCount.incrementAndGet();
            //TODO for test
            pushDestroyActivityInfo(activity);
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            super.onActivityStopped(activity);
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            pushDestroyActivityInfo(activity);
            synchronized (mDestroyActivityInfos) {
                mDestroyActivityInfos.notifyAll();
            }
        }
    };

    private final RetryableTaskExecutor.RetryableTask mScanDestroyActivitiesTask = new RetryableTaskExecutor.RetryableTask() {
        @Override
        public Status execute() {
            while (mDestroyActivityInfos.isEmpty()) {
                synchronized (mDestroyActivityInfos) {
                    try {
                        mDestroyActivityInfos.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            WeakReference<Object> sentinelRef = new WeakReference<>(new Object());
            triggerGc();
            if (sentinelRef.get() == null) {
                PMLog.d(TAG, "system ignore our gc request, wait for next detection. ");
                return Status.RETRY;
            }
            final Iterator<DestroyActivityInfo> iterator = mDestroyActivityInfos.iterator();
            while (iterator.hasNext()) {
                final DestroyActivityInfo destroyedActivityInfo = iterator.next();

                if (destroyedActivityInfo.mActivityRef.get() == null) {
                    iterator.remove();
                    continue;
                }
                ++destroyedActivityInfo.mDetectedCount;

                long createdActivityCountFromDestroy = mCurrentCreatedActivityCount.get() - destroyedActivityInfo.mLastCreatedActivityCount;
                if (createdActivityCountFromDestroy < 2) {
                    continue;
                }

                if (mAndroidHeapDumper != null) {
                    File hprofFile = mAndroidHeapDumper.dumpHeap();
                    if (hprofFile != null) {
                        HeapDump heapDump = new HeapDump(hprofFile, destroyedActivityInfo.mKey, destroyedActivityInfo.mActivityName);
                        mHeapDumpHandler.process(heapDump);
                    }
                }
            }
            return null;
        }
    };

    private void triggerGc() {
        Runtime.getRuntime().gc();
        Runtime.getRuntime().runFinalization();
    }
}
