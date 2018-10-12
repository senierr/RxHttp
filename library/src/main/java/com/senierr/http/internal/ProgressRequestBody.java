package com.senierr.http.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.senierr.http.RxHttp;

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

    private @NonNull RequestBody delegate;
    private @Nullable OnProgressListener listener;

    public ProgressRequestBody(@NonNull RequestBody requestBody, @Nullable OnProgressListener listener) {
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

        private long bytesWritten;
        private long contentLength;
        private long lastRefreshTime;

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
            if (curTime - lastRefreshTime >= RxHttp.REFRESH_MIN_INTERVAL || bytesWritten == contentLength) {
                int percent;
                if (contentLength <= 0) {
                    percent = 100;
                } else {
                    percent = (int) (bytesWritten * 100 / contentLength);
                }
                listener.onProgress(new Progress(contentLength, bytesWritten, percent, curTime));
                lastRefreshTime = System.currentTimeMillis();
            }
        }
    }
}