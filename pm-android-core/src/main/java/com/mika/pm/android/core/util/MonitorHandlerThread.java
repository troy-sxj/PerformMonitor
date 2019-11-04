package com.mika.pm.android.core.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Printer;

import com.mika.pm.android.core.BuildConfig;
import com.mika.pm.android.core.listener.IAppForeground;

import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: mika
 * @Time: 2019-11-04 10:59
 * @Description:
 */
public class MonitorHandlerThread {

    private static final String Tag = "MonitorHandlerThread";
    private static final String MONITOR_THREAD_NAME = "MonitorHandlerThread";

    private static volatile HandlerThread defaultHandlerThread;
    private static volatile Handler defaultHandler;
    private static volatile Handler defaultMainHandler = new Handler(Looper.getMainLooper());
    private static HashSet<HandlerThread> handlerThreads = new HashSet<>();
    private static boolean isDebug = BuildConfig.DEBUG;

    public static Handler getMainHandler() {
        return defaultMainHandler;
    }

    public static HandlerThread getDefaultHandlerThread() {
        if (null == defaultHandlerThread) {
            defaultHandlerThread = new HandlerThread(MONITOR_THREAD_NAME);
            defaultHandlerThread.start();
            defaultHandler = new Handler(defaultHandlerThread.getLooper());
            defaultHandlerThread.getLooper().setMessageLogging(isDebug ? new LooperPrinter() : null);
            PMLog.i(Tag, "create default handler thread, isDebug %s", isDebug);
        }
        return defaultHandlerThread;
    }

    public static Handler getDefaultHandler(){
        return defaultHandler;
    }

    public static HandlerThread getNewHandlerThread(String name){
        for(Iterator<HandlerThread> i = handlerThreads.iterator(); i.hasNext();){
            HandlerThread element =i.next();
            if(!element.isAlive()){
                i.remove();
                PMLog.w(Tag, "waring: remove dead handler thread with name %s ", element.getName());
            }
        }
        HandlerThread handlerThread = new HandlerThread(name);
        handlerThread.start();
        handlerThreads.add(handlerThread);
        PMLog.w(Tag, "waring: create new handler thread with name %s, alive thread size %d ", name, handlerThreads.size());
        return handlerThread;
    }


    private static final class LooperPrinter implements Printer, IAppForeground {

        private ConcurrentHashMap<String, Info> hashMap = new ConcurrentHashMap<>();
        private boolean isForeground;

        @Override
        public void println(String s) {
            if (s.charAt(0) == '>') {
                int start = s.indexOf("}");
                int end = s.indexOf("@", start);
                if (start < 0 || end < 0) {
                    return;
                }
                String content = s.substring(start, end);
                Info info = hashMap.get(content);
                if (info == null) {
                    info = new Info();
                    info.key = content;
                    hashMap.put(content, info);
                }
                ++info.count;
            }
        }

        @Override
        public void onForeground(boolean isForeground) {

        }

        class Info {
            String key;
            int count;

            @Override
            public String toString() {
                return "Info{" +
                        "key='" + key + '\'' +
                        ", count=" + count +
                        '}';
            }
        }
    }
}
