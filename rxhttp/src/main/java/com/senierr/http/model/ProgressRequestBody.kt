package com.senierr.http.model

import com.senierr.http.listener.OnProgressListener
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*

/**
 * 封装进度回调的请求体
 *
 * @author zhouchunjie
 * @date 2018/05/17
 */
class ProgressRequestBody(
        private val delegate: RequestBody,
        private val listener: OnProgressListener
) : RequestBody() {

    override fun contentType(): MediaType? {
        return delegate.contentType()
    }

    override fun contentLength(): Long {
        return delegate.contentLength()
    }

    override fun writeTo(sink: BufferedSink) {
        val bufferedSink = Okio.buffer(CountingSink(sink))
        delegate.writeTo(bufferedSink)
        bufferedSink.flush()
    }

    private inner class CountingSink constructor(delegate: Sink) : ForwardingSink(delegate) {

        private var bytesWritten: Long = 0
        private var contentLength: Long = 0
        private var lastRefreshTime: Long = 0

        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            if (contentLength <= 0) {
                contentLength = contentLength()
            }

            bytesWritten += byteCount
            val curTime = System.currentTimeMillis()
            if (curTime - lastRefreshTime >= OnProgressListener.REFRESH_MIN_INTERVAL || bytesWritten == contentLength) {
                val percent = if (contentLength <= 0) 100 else (bytesWritten * 100 / contentLength).toInt()
                listener.onProgress(contentLength, bytesWritten, percent)
                lastRefreshTime = System.currentTimeMillis()
            }
        }
    }
}