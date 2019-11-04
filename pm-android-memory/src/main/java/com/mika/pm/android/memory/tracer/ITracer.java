package com.mika.pm.android.memory.tracer;

import com.mika.pm.android.core.listener.IAppForeground;

/**
 * @Author: mika
 * @Time: 2019-11-04 16:23
 * @Description:
 */
public interface ITracer extends IAppForeground {

    boolean isAlive();

    void onStartTrace();

    void onCloseTrace();
}
