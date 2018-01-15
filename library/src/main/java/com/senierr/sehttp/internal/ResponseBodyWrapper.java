package com.senierr.sehttp.internal;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.BaseCallback;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 响应体封装
 *
 * @author zhouchunjie
 * @date 2017/9/9
 */

public class ResponseBodyWrapper extends ResponseBody {

    private ResponseBody delegate;
    private BaseCallback callback;
    private BufferedSource bufferedSource;

    public ResponseBodyWrapper(ResponseBody responseBody, BaseCallback callback) {
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

        public CountingSource(Source delegate) {
            super(delegate);
        }

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            if (contentLength <= 0) {
                contentLength = contentLength();
            }

            long bytesRead = super.read(sink, byteCount);
            totalBytesRead += bytesRead != -1 ? bytesRead : 0;

            long curTime = System.currentTimeMillis();
            if (curTime - lastRefreshUiTime >= SeHttp.REFRESH_MIN_INTERVAL || totalBytesRead == contentLength) {
                if (callback != null) {
                    SeHttp.getInstance().getMainScheduler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                int progress;
                                if (contentLength <= 0) {
                                    progress = 100;
                                } else {
                                    progress = (int) (totalBytesRead * 100 / contentLength);
                                }
                                callback.onDownloadProgress(contentLength, totalBytesRead, progress);
                            }
                        }
                    });
                }
                lastRefreshUiTime = System.currentTimeMillis();
            }
            return bytesRead;
        }
    }
}
