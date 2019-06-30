package com.senierr.http.listener

/**
 * 进度回调
 *
 * @author zhouchunjie
 * @date 2017/3/28
 */
interface OnProgressListener {

    companion object {
        // 进度回调最小间隔时长(ms)
        const val REFRESH_MIN_INTERVAL: Long = 100
    }

    fun onProgress(totalSize: Long, currentSize: Long, percent: Int)
}
