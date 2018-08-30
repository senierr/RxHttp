package com.senierr.http.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.LinkedHashMap;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Http请求体
 *
 * 规则优先级：
 *  1. requestBody
 *  2. multipart
 *  3. form
 *  4. string
 *  5. byte
 *
 * @author zhouchunjie
 * @date 2018/8/29
 */
public final class HttpRequestBody {

    public static final String MEDIA_TYPE_PLAIN = "text/plain; charset=utf-8";
    public static final String MEDIA_TYPE_XML = "text/xml; charset=utf-8";
    public static final String MEDIA_TYPE_JSON = "application/json; charset=utf-8";
    public static final String MEDIA_TYPE_STREAM = "application/octet-stream";

    private @Nullable RequestBody requestBody;
    private @Nullable MediaType mediaType;
    private @NonNull LinkedHashMap<String, File> fileParams = new LinkedHashMap<>();
    private @NonNull LinkedHashMap<String, String> stringParams = new LinkedHashMap<>();
    private @Nullable String stringContent;
    private @Nullable byte[] bytes;

    public void addRequestParam(@NonNull String key, @NonNull String value) {
        stringParams.put(key, value);
    }

    public void addRequestStringParams(@NonNull LinkedHashMap<String, String> params) {
        for (String key: params.keySet()) {
            stringParams.put(key, params.get(key));
        }
    }

    public void addRequestParam(@NonNull String key, @NonNull File file) {
        fileParams.put(key, file);
    }

    public void addRequestFileParams(@NonNull LinkedHashMap<String, File> params) {
        for (String key: params.keySet()) {
            fileParams.put(key, params.get(key));
        }
    }

    public void setRequestBody4JSon(@NonNull String jsonStr) {
        stringContent = jsonStr;
        mediaType = MediaType.parse(MEDIA_TYPE_JSON);
    }

    public void setRequestBody4Text(@NonNull String textStr) {
        stringContent = textStr;
        mediaType = MediaType.parse(MEDIA_TYPE_PLAIN);
    }

    public void setRequestBody4Xml(@NonNull String xmlStr) {
        stringContent = xmlStr;
        mediaType = MediaType.parse(MEDIA_TYPE_XML);
    }

    public void setRequestBody4Byte(@NonNull byte[] bytes) {
        this.bytes = bytes;
        mediaType = MediaType.parse(MEDIA_TYPE_STREAM);
    }

    public void setRequestBody(@NonNull RequestBody requestBody) {
        this.requestBody = requestBody;
    }

    public @Nullable RequestBody generateRequestBody() {
        if (requestBody != null) {
            return requestBody;
        } else if (!fileParams.isEmpty()) {
            MultipartBody.Builder multipartBodybuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            for (String key: fileParams.keySet()) {
                File value = fileParams.get(key);
                RequestBody fileBody = RequestBody.create(guessMimeType(value.getPath()), value);
                multipartBodybuilder.addFormDataPart(key, value.getName(), fileBody);
            }
            if (!stringParams.isEmpty()) {
                for (String key: stringParams.keySet()) {
                    String value = stringParams.get(key);
                    multipartBodybuilder.addFormDataPart(key, value);
                }
            }
            return multipartBodybuilder.build();
        } else if (!stringParams.isEmpty()) {
            FormBody.Builder bodyBuilder = new FormBody.Builder();
            for (String key : stringParams.keySet()) {
                String value = stringParams.get(key);
                bodyBuilder.add(key, value);
            }
            return bodyBuilder.build();
        } else if (stringContent != null && mediaType != null) {
            return RequestBody.create(mediaType, stringContent);
        } else if (bytes != null && mediaType != null) {
            return RequestBody.create(mediaType, bytes);
        } else {
            return null;
        }
    }

    private static @Nullable MediaType guessMimeType(@NonNull String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        path = path.replace("#", "");   //解决文件名中含有#号异常的问题
        String contentType = fileNameMap.getContentTypeFor(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return MediaType.parse(contentType);
    }
}
