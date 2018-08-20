package com.senierr.sehttp.internal;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.BaseCallback;

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
public final class RequestBodyWrapper extends RequestBody {

    private SeHttp seHttp;
    private RequestBody delegate;
    private BaseCallback callback;

    public RequestBodyWrapper(SeHttp seHttp, RequestBody requestBody, BaseCallback callback) {
        this.seHttp = seHttp;
        this.delegate = requestBody;
        this.callback = callback;
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
            if (callback == null) return;

            if (contentLength <= 0) {
                contentLength = contentLength();
            }

            bytesWritten += byteCount;

            long curTime = System.currentTimeMillis();
            if (curTime - lastRefreshUiTime >= seHttp.getDispatcher().getRefreshInterval() || bytesWritten == contentLength) {
                seHttp.getDispatcher().enqueueOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (callback == null) return;
                        int progress;
                        if (contentLength <= 0) {
                            progress = 100;
                        } else {
                            progress = (int) (bytesWritten * 100 / contentLength);
                        }
                        callback.onUpload(progress, bytesWritten, contentLength);
                    }
                });
                lastRefreshUiTime = System.currentTimeMillis();
            }
        }
    }
}
