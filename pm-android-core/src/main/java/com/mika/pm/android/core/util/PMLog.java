package com.mika.pm.android.core.util;

import android.util.Log;

/**
 * @Author: mika
 * @Time: 2019-11-01 17:45
 * @Description:
 */
public class PMLog {

    private static PMLog.PMLogImp debugLog = new PMLog.PMLogImp() {
        public void v(String tag, String format, Object... params) {
            String log = params != null && params.length != 0 ? String.format(format, params) : format;
            Log.v(tag, log);
        }

        public void i(String tag, String format, Object... params) {
            String log = params != null && params.length != 0 ? String.format(format, params) : format;
            Log.i(tag, log);
        }

        public void d(String tag, String format, Object... params) {
            String log = params != null && params.length != 0 ? String.format(format, params) : format;
            Log.d(tag, log);
        }

        public void w(String tag, String format, Object... params) {
            String log = params != null && params.length != 0 ? String.format(format, params) : format;
            Log.w(tag, log);
        }

        public void e(String tag, String format, Object... params) {
            String log = params != null && params.length != 0 ? String.format(format, params) : format;
            Log.e(tag, log);
        }

        public void printErrStackTrace(String tag, Throwable tr, String format, Object... params) {
            String log = params != null && params.length != 0 ? String.format(format, params) : format;
            if (log == null) {
                log = "";
            }

            log = log + "  " + Log.getStackTraceString(tr);
            Log.e(tag, log);
        }
    };

    private static PMLog.PMLogImp pmLogImp;

    static {
        pmLogImp = debugLog;
    }

    public static void v(String tag, String msg, Object... obj) {
        if (pmLogImp != null) {
            pmLogImp.v(tag, msg, obj);
        }

    }

    public static void e(String tag, String msg, Object... obj) {
        if (pmLogImp != null) {
            pmLogImp.e(tag, msg, obj);
        }

    }

    public static void w(String tag, String msg, Object... obj) {
        if (pmLogImp != null) {
            pmLogImp.w(tag, msg, obj);
        }

    }

    public static void i(String tag, String msg, Object... obj) {
        if (pmLogImp != null) {
            pmLogImp.i(tag, msg, obj);
        }

    }

    public static void d(String tag, String msg, Object... obj) {
        if (pmLogImp != null) {
            pmLogImp.d(tag, msg, obj);
        }

    }

    public static void printErrStackTrace(String tag, Throwable tr, String format, Object... obj) {
        if (pmLogImp != null) {
            pmLogImp.printErrStackTrace(tag, tr, format, obj);
        }

    }

    public interface PMLogImp {
        void v(String var1, String var2, Object... var3);

        void i(String var1, String var2, Object... var3);

        void w(String var1, String var2, Object... var3);

        void d(String var1, String var2, Object... var3);

        void e(String var1, String var2, Object... var3);

        void printErrStackTrace(String var1, Throwable var2, String var3, Object... var4);
    }
}
