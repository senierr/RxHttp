package com.senierr.http.internal;

import android.support.annotation.Nullable;

/**
 * 带进度的返回
 *
 * @author zhouchunjie
 * @date 2018/8/30
 */
public final class ProgressResponse<T> {

    public static final int TYPE_UPLOAD = -1;
    public static final int TYPE_DOWNLOAD = 1;
    public static final int TYPE_RESULT = 0;

    private final int type;
    private final long totalSize;
    private final long currentSize;
    private final int percent;
    private final @Nullable T result;

    public ProgressResponse(int type, long totalSize, long currentSize, int percent, @Nullable T result) {
        this.type = type;
        this.totalSize = totalSize;
        this.currentSize = currentSize;
        this.percent = percent;
        this.result = result;
    }

    public int type() {
        return type;
    }

    public long totalSize() {
        return totalSize;
    }

    public long currentSize() {
        return currentSize;
    }

    public int percent() {
        return percent;
    }

    public @Nullable T result() {
        return result;
    }
}