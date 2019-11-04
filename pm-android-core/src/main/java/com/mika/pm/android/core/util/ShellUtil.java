package com.mika.pm.android.core.util;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @Author: mika
 * @Time: 2019-11-04 10:49
 * @Description:
 */
public class ShellUtil {


    @NonNull
    public static String execCmd(String cmd) {
        try {
            StringBuilder data = new StringBuilder();
            Process exec = Runtime.getRuntime().exec(cmd);
            BufferedReader infoReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(exec.getErrorStream()));

            String error = null;
            while ((error = errorReader.readLine()) != null && !error.equals("null")) {
                data.append(error).append("\n");
            }
            String info = null;
            while ((info = infoReader.readLine()) != null && !info.equals("null")) {
                data.append(info).append("\n");
            }

            return data.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
