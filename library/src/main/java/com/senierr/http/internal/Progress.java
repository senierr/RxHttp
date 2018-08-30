package com.senierr.http.internal;

/**
 * 进度
 *
 * @author zhouchunjie
 * @date 2018/8/30
 */
public final class Progress {

    private final long totalSize;
    private final long currentSize;
    private final int percent;
    private final long refreshTime;

    public Progress(long totalSize, long currentSize, int percent, long refreshTime) {
        this.totalSize = totalSize;
        this.currentSize = currentSize;
        this.percent = percent;
        this.refreshTime = refreshTime;
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

    public long refreshTime() {
        return refreshTime;
    }

    @Override
    public String toString() {
        return "Progress{" +
                "totalSize=" + totalSize +
                ", currentSize=" + currentSize +
                ", percent=" + percent +
                ", refreshTime=" + refreshTime +
                '}';
    }
}