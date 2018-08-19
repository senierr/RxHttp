package com.senierr.sehttp.util;

import android.os.Handler;
import android.os.Looper;

import com.senierr.sehttp.SeHttp;

import java.util.concurrent.Executor;

/**
 * 主线程执行器
 *
 * @author zhouchunjie
 * @date 2018/8/19
 */
public class MainThreadExecutor implements Executor {

    // 异步刷新间隔
    private int refreshInterval = SeHttp.REFRESH_MIN_INTERVAL;
    // 主线程
    private Handler handler = new Handler(Looper.getMainLooper());

    private MainThreadExecutor() {}

    private static MainThreadExecutor sInstance = null;

    public static MainThreadExecutor getInstance() {
        if (sInstance == null) {
            synchronized (MainThreadExecutor.class) {
                if (sInstance == null)
                    sInstance = new MainThreadExecutor();
            }
        }
        return sInstance;
    }

    @Override
    public void execute(Runnable command) {
        handler.post(command);
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }
}
