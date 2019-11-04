package com.mika.pm.android.core.util;

import android.os.Debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: mika
 * @Time: 2019-11-04 15:18
 * @Description:
 */
public class DeviceUtil {

    public static int getPId() {
        return android.os.Process.myPid();
    }

    public static long getDalvikHeap() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024;   //in KB
    }

    public static long getNativeHeap() {
        return Debug.getNativeHeapAllocatedSize() / 1024;   //in KB
    }

    public static long getVmSize() {
        String status = String.format("/proc/%s/status", getPId());
        try {
            String content = getStringFromFile(status).trim();
            String[] args = content.split("\n");
            for (String str : args) {
                if (str.startsWith("VmSize")) {
                    Pattern p = Pattern.compile("\\d+");
                    Matcher matcher = p.matcher(str);
                    if (matcher.find()) {
                        return Long.parseLong(matcher.group());
                    }
                }
            }
            if (args.length > 12) {
                Pattern p = Pattern.compile("\\d+");
                Matcher matcher = p.matcher(args[12]);
                if (matcher.find()) {
                    return Long.parseLong(matcher.group());
                }
            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } finally {
            if (null != reader) {
                reader.close();
            }
        }

        return sb.toString();
    }

    public static String getStringFromFile(String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = null;
        String ret;
        try {
            fin = new FileInputStream(fl);
            ret = convertStreamToString(fin);
        } finally {
            if (null != fin) {
                fin.close();
            }
        }
        return ret;
    }
}
