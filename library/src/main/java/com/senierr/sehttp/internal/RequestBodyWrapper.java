package com.senierr.sehttp.internal;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.OnUploadListener;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * 请求体封装
 *
 * @author zhouchunjie
 * @date 2018/05/17
 */

public class RequestBodyWrapper extends RequestBody {

    private RequestBody delegate;
    private OnUploadListener onUploadListener;

    public RequestBodyWrapper(RequestBody requestBody, OnUploadListener onUploadListener) {
        this.delegate = requestBody;
        this.onUploadListener = onUploadListener;
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
            if (contentLength <= 0) {
                contentLength = contentLength();
            }

            bytesWritten += byteCount;

            long curTime = System.currentTimeMillis();
            if (curTime - lastRefreshUiTime >= SeHttp.REFRESH_MIN_INTERVAL || bytesWritten == contentLength) {
                if (onUploadListener != null) {
                    int progress;
                    if (contentLength <= 0) {
                        progress = 100;
                    } else {
                        progress = (int) (bytesWritten * 100 / contentLength);
                    }
                    onUploadListener.onProgress(progress, bytesWritten, contentLength);
                }
                lastRefreshUiTime = System.currentTimeMillis();
            }
        }
    }
}
