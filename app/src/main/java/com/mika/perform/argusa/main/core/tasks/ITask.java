package com.mika.perform.argusa.main.core.tasks;

import com.mika.perform.argusa.main.core.IInfo;

/**
 * @Author: mika
 * @Time: 2020/3/13 2:53 PM
 * @Description:
 */
public interface ITask {

    String getTaskName();

    void start();

    boolean isCanWork();

    void setCanWork(boolean canWork);

    boolean save(IInfo info);

    void stop();
}
