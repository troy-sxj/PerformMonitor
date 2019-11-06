package com.mika.pm.android.memory.watcher;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

/**
 * @Author: mika
 * @Time: 2019-11-05 10:48
 * @Description:
 */
public class RetryableTaskExecutor {

    private final Handler mBackgroundHandler;
    private final Handler mMainHandler;
    private final long mDelayMillis;

    public interface RetryableTask{
        enum Status{
            DONE, RETRY
        }

        Status execute();
    }

    public RetryableTaskExecutor(long delayMillis, HandlerThread handlerThread){
        mBackgroundHandler = new Handler(handlerThread.getLooper());
        mMainHandler = new Handler(Looper.getMainLooper());
        mDelayMillis = delayMillis;
    }

    public void executeInMainThread(final RetryableTask task){
        postToMainThreadWithDelay(task, 0);
    }

    public void executeInBackground(final RetryableTask task){
        postToBackgroundWithDelay(task, 0);
    }

    public void quit(){
        clearTasks();
    }

    public void clearTasks(){
        mBackgroundHandler.removeCallbacksAndMessages(null);
        mMainHandler.removeCallbacksAndMessages(null);
    }

    private void postToMainThreadWithDelay(final RetryableTask task, final int failedAttempts){
        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                RetryableTask.Status status = task.execute();
                if(status == RetryableTask.Status.RETRY){
                    postToMainThreadWithDelay(task, failedAttempts +1);
                }
            }
        }, mDelayMillis);
    }

    private void postToBackgroundWithDelay(final RetryableTask task, final int failedAttempts){
        mBackgroundHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                RetryableTask.Status status = task.execute();
                if(status == RetryableTask.Status.RETRY){
                    postToBackgroundWithDelay(task, failedAttempts +1);
                }
            }
        }, mDelayMillis);
    }

}
