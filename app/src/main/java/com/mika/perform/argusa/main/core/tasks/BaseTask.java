package com.mika.perform.argusa.main.core.tasks;

import com.mika.perform.argusa.main.core.IInfo;

/**
 * @Author: mika
 * @Time: 2020/3/13 3:04 PM
 * @Description:
 */
public abstract class BaseTask implements ITask {

    public boolean mIsCanWork = true;

    @Override
    public void start() {

    }

    @Override
    public boolean isCanWork() {
        return mIsCanWork;
    }

    @Override
    public void setCanWork(boolean canWork) {
        mIsCanWork = canWork;
    }

    @Override
    public boolean save(IInfo info) {
        return false;
    }

    @Override
    public void stop() {

    }
}
