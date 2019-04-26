package com.senierr.http.listener;

/**
 * 进度回调
 *
 * @author zhouchunjie
 * @date 2017/3/28
 */
public interface OnProgressListener {

    // 进度回调最小间隔时长(ms)
    long REFRESH_MIN_INTERVAL = 100;

    void onProgress(long totalSize, long currentSize, int percent);
}
