package com.senierr.sehttp.internal;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.BaseCallback;
import com.senierr.sehttp.entity.FileMap;
import com.senierr.sehttp.util.HttpUtil;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
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
 * @date 2017/3/29
 */

public class RequestBodyWrapper extends RequestBody {

    public static final String MEDIA_TYPE_PLAIN = "text/plain; charset=utf-8";
    public static final String MEDIA_TYPE_XML = "text/xml; charset=utf-8";
    public static final String MEDIA_TYPE_JSON = "application/json; charset=utf-8";
    public static final String MEDIA_TYPE_STREAM = "application/octet-stream";

    private RequestBody requestBody;
    private MediaType mediaType;
    private FileMap fileParams;
    private LinkedHashMap<String, String> stringParams;
    private String stringContent;
    private byte[] bytes;

    private boolean isMultipart = false;

    private RequestBody delegate;
    private BaseCallback callback;

    public RequestBodyWrapper() {
        super();
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

        public CountingSink(Sink delegate) {
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
                if (callback != null) {
                    SeHttp.getInstance().getMainScheduler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onUploadProgress(contentLength, bytesWritten, (int) (bytesWritten * 100 / contentLength));
                            }
                        }
                    });
                }
                lastRefreshUiTime = System.currentTimeMillis();
            }
        }
    }

    public RequestBody getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public FileMap getFileParams() {
        return fileParams;
    }

    public void setFileParams(FileMap fileParams) {
        this.fileParams = fileParams;
    }

    public LinkedHashMap<String, String> getStringParams() {
        return stringParams;
    }

    public void setStringParams(LinkedHashMap<String, String> stringParams) {
        this.stringParams = stringParams;
    }

    public String getStringContent() {
        return stringContent;
    }

    public void setStringContent(String stringContent) {
        this.stringContent = stringContent;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public boolean isMultipart() {
        return isMultipart;
    }

    public void setMultipart(boolean multipart) {
        isMultipart = multipart;
    }

    /**
     * 创建请求体
     *
     * 规则优先级：
     *  1. requestBody
     *  2. multipart
     *  3. form
     *  4. string
     *  5. byte
     *
     * @return
     */
    private RequestBody buildRequestBody() {
        if (requestBody != null) {
            return requestBody;
        } else if (fileParams != null && !fileParams.isEmpty()) {
            MultipartBody.Builder multipartBodybuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            for (int i = 0; i < fileParams.size(); i++) {
                File file = fileParams.fileAt(i);
                RequestBody fileBody = RequestBody.create(HttpUtil.guessMimeType(file.getPath()), file);
                multipartBodybuilder.addFormDataPart(fileParams.keyAt(i), file.getName(), fileBody);
            }
            if (stringParams != null && !stringParams.isEmpty()) {
                for (String key: stringParams.keySet()) {
                    String value = stringParams.get(key);
                    multipartBodybuilder.addFormDataPart(key, value);
                }
            }
            return multipartBodybuilder.build();
        } else if (stringParams != null && !stringParams.isEmpty()) {
            if (isMultipart) {
                MultipartBody.Builder multipartBodybuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                for (String key: stringParams.keySet()) {
                    String value = stringParams.get(key);
                    multipartBodybuilder.addFormDataPart(key, value);
                }
                return multipartBodybuilder.build();
            } else {
                FormBody.Builder bodyBuilder = new FormBody.Builder();
                for (String key : stringParams.keySet()) {
                    String value = stringParams.get(key);
                    bodyBuilder.add(key, value);
                }
                return bodyBuilder.build();
            }
        } else if (stringContent != null && mediaType != null) {
            return RequestBody.create(mediaType, stringContent);
        } else if (bytes != null && mediaType != null) {
            return RequestBody.create(mediaType, bytes);
        } else {
            return null;
        }
    }

    /**
     * 创建
     *
     * @return
     */
    public RequestBodyWrapper build(BaseCallback callback) {
        this.callback = callback;
        this.delegate = buildRequestBody();
        if (delegate == null) {
            return null;
        } else {
            return this;
        }
    }
}
