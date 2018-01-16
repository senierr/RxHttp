package com.senierr.sehttp.util;

import android.text.TextUtils;
import android.util.Log;

/**
 * 日志工具类
 *
 * @author zhouchunjie
 * @date 2017/10/26
 */

public class LogUtil {

    private static boolean isDebug = false;
    private static String debugTag = "SeHttp";

    public static void openDebug(String tag) {
        isDebug = true;
        if (!TextUtils.isEmpty(tag)) {
            debugTag = tag;
        }
    }

    public static void logV(String msg) {
        if (isDebug && !TextUtils.isEmpty(msg)) {
            Log.v(debugTag, msg);
        }
    }

    public static void logD(String msg) {
        if (isDebug && !TextUtils.isEmpty(msg)) {
            Log.d(debugTag, msg);
        }
    }

    public static void logI(String msg) {
        if (isDebug && !TextUtils.isEmpty(msg)) {
            Log.i(debugTag, msg);
        }
    }

    public static void logW(String msg) {
        if (isDebug && !TextUtils.isEmpty(msg)) {
            Log.w(debugTag, msg);
        }
    }

    public static void logE(String msg) {
        if (isDebug && !TextUtils.isEmpty(msg)) {
            Log.e(debugTag, msg);
        }
    }
}
