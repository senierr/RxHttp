package com.senierr.http.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.senierr.http.RxHttp;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 封装进度回调的响应体
 *
 * @author zhouchunjie
 * @date 2017/9/9
 */
public final class ProgressResponseBody extends ResponseBody {

    private @NonNull ResponseBody delegate;
    private @Nullable BufferedSource bufferedSource;
    private @NonNull OnProgressListener listener;

    public ProgressResponseBody(@NonNull ResponseBody responseBody,
                                @NonNull OnProgressListener listener) {
        this.delegate = responseBody;
        this.listener = listener;
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

        private long totalBytesRead;
        private long contentLength;
        private long lastRefreshTime;

        private CountingSource(Source delegate) {
            super(delegate);
        }

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            long bytesRead = super.read(sink, byteCount);
            if (contentLength <= 0) {
                contentLength = contentLength();
            }

            totalBytesRead += bytesRead != -1 ? bytesRead : 0;
            final long curTime = System.currentTimeMillis();
            if (curTime - lastRefreshTime >= RxHttp.REFRESH_MIN_INTERVAL || totalBytesRead == contentLength) {
                int percent;
                if (contentLength <= 0) {
                    percent = 100;
                } else {
                    percent = (int) (totalBytesRead * 100 / contentLength);
                }
                listener.onProgress(new Progress(Progress.TYPE_DOWNLOAD, contentLength, totalBytesRead, percent));
                lastRefreshTime = System.currentTimeMillis();
            }
            return bytesRead;
        }
    }
}