package com.senierr.http.internal;

import com.senierr.http.RxHttp;
import com.senierr.http.listener.OnProgressListener;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 带进度回调的响应体
 *
 * @author zhouchunjie
 * @date 2017/9/9
 */
public final class ProgressResponseBody extends ResponseBody {

    private ResponseBody delegate;
    private BufferedSource bufferedSource;
    private OnProgressListener callback;

    public ProgressResponseBody(ResponseBody responseBody, OnProgressListener callback) {
        this.delegate = responseBody;
        this.callback = callback;
    }

    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public long contentLength() {
        return delegate.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(new CountingSource(delegate.source()));
        }
        return bufferedSource;
    }

    private final class CountingSource extends ForwardingSource {

        private long totalBytesRead = 0;
        private long contentLength = 0;
        private long lastRefreshUiTime;

        private CountingSource(Source delegate) {
            super(delegate);
        }

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            long bytesRead = super.read(sink, byteCount);
            if (callback == null) {
                return bytesRead;
            }

            if (contentLength <= 0) {
                contentLength = contentLength();
            }

            totalBytesRead += bytesRead != -1 ? bytesRead : 0;

            long curTime = System.currentTimeMillis();
            if (curTime - lastRefreshUiTime >= RxHttp.REFRESH_INTERVAL || totalBytesRead == contentLength) {
                int progress;
                if (contentLength <= 0) {
                    progress = 100;
                } else {
                    progress = (int) (totalBytesRead * 100 / contentLength);
                }
                callback.onProgress(progress, totalBytesRead, contentLength);
                lastRefreshUiTime = System.currentTimeMillis();
            }
            return bytesRead;
        }
    }
}
