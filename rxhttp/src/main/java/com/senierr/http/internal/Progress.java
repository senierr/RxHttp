package com.senierr.http.internal;

/**
 * 进度
 *
 * @author zhouchunjie
 * @date 2018/8/30
 */
public final class Progress {

    public static final int TYPE_UPLOAD = -1;
    public static final int TYPE_DOWNLOAD = 1;

    private final int type;
    private final long totalSize;
    private final long currentSize;
    private final int percent;

    public Progress(int type, long totalSize, long currentSize, int percent) {
        this.type = type;
        this.totalSize = totalSize;
        this.currentSize = currentSize;
        this.percent = percent;
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

    @Override
    public String toString() {
        return "Progress{" +
                "type=" + type +
                ", totalSize=" + totalSize +
                ", currentSize=" + currentSize +
                ", percent=" + percent +
                '}';
    }
}