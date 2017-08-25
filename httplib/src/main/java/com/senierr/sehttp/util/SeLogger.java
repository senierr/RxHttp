package com.senierr.sehttp.util;

import android.util.Log;

/**
 * 日志工具类
 *
 * @author zhouchunjie
 * @date 2017/3/29
 */

public class SeLogger {

    private static boolean isLog = false;
    private static String logTag = SeLogger.class.getSimpleName();

    public static void init(String tag) {
        init(true, tag);
    }

    public static void init(boolean isEnable, String tag) {
        isLog = isEnable;
        logTag = tag;
    }

    public static void v(String logStr) {
        if (isLog) {
            Log.v(logTag, logStr);
        }
    }

    public static void i(String logStr) {
        if (isLog) {
            Log.i(logTag, logStr);
        }
    }

    public static void d(String logStr) {
        if (isLog) {
            Log.d(logTag, logStr);
        }
    }

    public static void w(String logStr) {
        if (isLog) {
            Log.w(logTag, logStr);
        }
    }

    public static void e(String logStr) {
        if (isLog) {
            Log.e(logTag, logStr);
        }
    }

    public static void e(Exception e) {
        if (isLog) {
            e.printStackTrace();
        }
    }
}
