package com.senierr.http.model

/**
 * 带进度的返回
 *
 * @author zhouchunjie
 * @date 2018/8/30
 */
class ProgressResponse<T> constructor(
        private val type: Int,
        private val totalSize: Long,
        private val currentSize: Long,
        private val percent: Int,
        private val result: T?
) {
    companion object {

        const val TYPE_UPLOAD = -1
        const val TYPE_DOWNLOAD = 1
        const val TYPE_RESULT = 0

        fun <T> upload(totalSize: Long, currentSize: Long, percent: Int): ProgressResponse<T> {
            return ProgressResponse<T>(TYPE_UPLOAD, totalSize, currentSize, percent, null)
        }

        fun <T> download(totalSize: Long, currentSize: Long, percent: Int): ProgressResponse<T> {
            return ProgressResponse<T>(TYPE_DOWNLOAD, totalSize, currentSize, percent, null)
        }

        fun <T> result(t: T): ProgressResponse<T> {
            return ProgressResponse(TYPE_RESULT, 0, 0, 0, t)
        }
    }

    fun type(): Int {
        return type
    }

    fun totalSize(): Long {
        return totalSize
    }

    fun currentSize(): Long {
        return currentSize
    }

    fun percent(): Int {
        return percent
    }

    fun result(): T? {
        return result
    }

    override fun toString(): String {
        return "ProgressResponse{" +
                "type=" + type +
                ", totalSize=" + totalSize +
                ", currentSize=" + currentSize +
                ", percent=" + percent +
                ", result=" + result +
                '}'.toString()
    }
}