package com.senierr.http.internal;

/**
 * 进度回调
 *
 * @author zhouchunjie
 * @date 2017/3/28
 */
public interface OnProgressListener {

    void onProgress(long totalSize, long currentSize, int percent);
}
