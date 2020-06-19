package com.mika.perform.argusa.main.core.job.fps;

import android.util.Log;
import android.view.Choreographer;
import android.widget.Chronometer;

import com.mika.perform.argusa.main.core.tasks.BaseTask;
import com.mika.perform.argusa.main.utils.AsyncThreadTask;

/**
 * @Author: mika
 * @Time: 2020/3/13 3:10 PM
 * @Description:
 */
public class FpsTask extends BaseTask implements Choreographer.FrameCallback {


    private long mLastFrameTimeNanos = 0;
    private long mFrameTimeNanos = 0;

    private int mCurrentCount = 0;
    private int mFpsCount = 0;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!isCanWork()) {
                mCurrentCount = 0;
                return;
            }
            calculateFPS();
            mCurrentCount++;
            // 1s采集一次
            AsyncThreadTask.executeDelayed(runnable, 1000);
        }
    };

    private void calculateFPS() {
        if (mLastFrameTimeNanos == 0) {
            mLastFrameTimeNanos = mFrameTimeNanos;
            return;
        }
        float costTime = (float) (mFrameTimeNanos - mLastFrameTimeNanos) / 1000000.0F;
        if (mFpsCount <= 0 && costTime <= 0.0F) {
            return;
        }

        int fpsResult = (int) (mFpsCount * 1000 / costTime);
        Log.i(getTaskName(), "calculateFPS ==== mFpsCount : "+ mFpsCount + " , fpsResult : " + fpsResult);
        mLastFrameTimeNanos = mFrameTimeNanos;
        mFpsCount = 0;
    }

    @Override
    public void start() {
        super.start();
        AsyncThreadTask.executeDelayed(runnable, (int) (Math.round(Math.random() * 1000)));
        Choreographer.getInstance().postFrameCallback(this);
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        Log.i(getTaskName(), "doFrame");
        mFpsCount++;
        mFrameTimeNanos = frameTimeNanos;
        Choreographer.getInstance().postFrameCallback(this);
    }


    @Override
    public String getTaskName() {
        return "FpsTask";
    }
}
