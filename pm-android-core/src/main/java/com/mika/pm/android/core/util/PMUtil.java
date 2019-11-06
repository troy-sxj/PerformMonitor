package com.mika.pm.android.core.util;

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;

/**
 * @Author: mika
 * @Time: 2019-11-05 17:59
 * @Description:
 */
public class PMUtil {

    private static final String TAG = "PMUtil";

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            Log.w(TAG, "Failed to close resource", e);
        }
    }
}
