package com.mika.perform.argusa.main.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import androidx.annotation.NonNull;

/**
 * @Author: mika
 * @Time: 2020/3/13 3:13 PM
 * @Description:
 */
public class AsyncThreadTask {

    private final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private final int DEFAULT_THREAD_COUNT = CPU_COUNT + 3;
    private final int KEEP_ALIVE = 3;
    private ExecutorService mThreadPool;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {

        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "AsyncThreadTask #" + mCount.getAndIncrement());
            thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
            return thread;
        }
    };

    private static AsyncThreadTask instance;

    public static AsyncThreadTask getInstance() {
        if (instance == null) {
            synchronized (AsyncThreadTask.class) {
                if (instance == null) {
                    instance = new AsyncThreadTask();
                }
            }
        }
        return instance;
    }

    private AsyncThreadTask() {
        mThreadPool = new ThreadPoolExecutor(
                DEFAULT_THREAD_COUNT,
                DEFAULT_THREAD_COUNT,
                KEEP_ALIVE,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(1000),
                sThreadFactory,
                new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    private void executeRunnable(Runnable runnable){
        mThreadPool.execute(runnable);
    }

    private void executeRunnableDelay(final Runnable runnable, long delayTime){
        getHandler().postDelayed(runnable, delayTime);
    }

    public static void executeDelayed(Runnable runnable, long delayTime){
        AsyncThreadTask.getInstance().executeRunnableDelay(runnable, delayTime);
    }

    private InternalHandler mHandler;

    private Handler getHandler(){
        synchronized (this){
            if(mHandler == null){
                mHandler = new InternalHandler();
            }
            return mHandler;
        }
    }

    private static class InternalHandler extends Handler {
        public InternalHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    }
}
