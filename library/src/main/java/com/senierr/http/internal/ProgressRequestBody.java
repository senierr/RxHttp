package com.senierr.http.internal;

import com.senierr.http.RxHttp;
import com.senierr.http.listener.OnProgressListener;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * 带进度回调的请求体
 *
 * @author zhouchunjie
 * @date 2018/05/17
 */
public final class ProgressRequestBody extends RequestBody {

    private RequestBody delegate;
    private OnProgressListener listener;

    public ProgressRequestBody(RequestBody requestBody, OnProgressListener listener) {
        this.delegate = requestBody;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return delegate.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        BufferedSink bufferedSink = Okio.buffer(new CountingSink(sink));
        delegate.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    private final class CountingSink extends ForwardingSink {

        private long bytesWritten = 0;
        private long contentLength = 0;
        private long lastRefreshUiTime;

        private CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            if (listener == null) return;

            if (contentLength <= 0) {
                contentLength = contentLength();
            }

            bytesWritten += byteCount;

            long curTime = System.currentTimeMillis();
            if (curTime - lastRefreshUiTime >= RxHttp.REFRESH_INTERVAL || bytesWritten == contentLength) {
                int progress;
                if (contentLength <= 0) {
                    progress = 100;
                } else {
                    progress = (int) (bytesWritten * 100 / contentLength);
                }
                listener.onProgress(progress, bytesWritten, contentLength);
                lastRefreshUiTime = System.currentTimeMillis();
            }
        }
    }
}
