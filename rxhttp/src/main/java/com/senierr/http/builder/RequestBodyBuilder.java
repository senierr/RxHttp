package com.senierr.http.builder;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.LinkedHashMap;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Http请求体构建器
 *
 * @author zhouchunjie
 * @date 2018/8/29
 */
public final class RequestBodyBuilder {

    private static final String MEDIA_TYPE_PLAIN = "text/plain; charset=utf-8";
    private static final String MEDIA_TYPE_XML = "text/xml; charset=utf-8";
    private static final String MEDIA_TYPE_JSON = "application/json; charset=utf-8";
    private static final String MEDIA_TYPE_STREAM = "application/octet-stream";

    private @Nullable RequestBody requestBody;

    private @Nullable MediaType mediaType;
    private @Nullable String stringContent;
    private @Nullable byte[] bytes;
    private @Nullable File file;

    private boolean isMultipart = false;
    private @NonNull LinkedHashMap<String, File> fileParams = new LinkedHashMap<>();
    private @NonNull LinkedHashMap<String, String> stringParams = new LinkedHashMap<>();

    public void setRequestBody(@NonNull RequestBody requestBody) {
        this.requestBody = requestBody;
    }

    public void setRequestBody4Text(@NonNull String textStr) {
        stringContent = textStr;
        mediaType = MediaType.parse(MEDIA_TYPE_PLAIN);
    }

    public void setRequestBody4JSon(@NonNull String jsonStr) {
        stringContent = jsonStr;
        mediaType = MediaType.parse(MEDIA_TYPE_JSON);
    }

    public void setRequestBody4Xml(@NonNull String xmlStr) {
        stringContent = xmlStr;
        mediaType = MediaType.parse(MEDIA_TYPE_XML);
    }

    public void setRequestBody4Byte(@NonNull byte[] bytes) {
        this.bytes = bytes;
        mediaType = MediaType.parse(MEDIA_TYPE_STREAM);
    }

    public void setRequestBody4File(@NonNull File file) {
        this.file = file;
        mediaType = guessMimeType(file.getPath());
    }

    public void isMultipart(boolean isMultipart) {
        this.isMultipart = isMultipart;
    }

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

    public @Nullable RequestBody build() {
        if (requestBody != null) {
            // 自定义
            return requestBody;
        } else if (stringContent != null && mediaType != null) {
            // 字符串
            return RequestBody.create(mediaType, stringContent);
        } else if (bytes != null && mediaType != null) {
            // 字节数组
            return RequestBody.create(mediaType, bytes);
        } else if (file != null && mediaType != null) {
            // 文件
            return RequestBody.create(mediaType, file);
        } else if (!fileParams.isEmpty()) {
            // 分片提交
            MultipartBody.Builder multipartBodybuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            for (String key: fileParams.keySet()) {
                File value = fileParams.get(key);
                if (value != null) {
                    RequestBody fileBody = RequestBody.create(guessMimeType(value.getPath()), value);
                    multipartBodybuilder.addFormDataPart(key, value.getName(), fileBody);
                }
            }
            if (!stringParams.isEmpty()) {
                for (String key: stringParams.keySet()) {
                    String value = stringParams.get(key);
                    if (value != null) {
                        multipartBodybuilder.addFormDataPart(key, value);
                    }
                }
            }
            return multipartBodybuilder.build();
        } else if (!stringParams.isEmpty()) {
            if (isMultipart) {
                // 强制分片
                MultipartBody.Builder multipartBodybuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                for (String key: stringParams.keySet()) {
                    String value = stringParams.get(key);
                    if (value != null) {
                        multipartBodybuilder.addFormDataPart(key, value);
                    }
                }
                return multipartBodybuilder.build();
            } else {
                // 默认表单
                FormBody.Builder bodyBuilder = new FormBody.Builder();
                for (String key : stringParams.keySet()) {
                    String value = stringParams.get(key);
                    if (value != null) {
                        bodyBuilder.add(key, value);
                    }
                }
                return bodyBuilder.build();
            }
        } else {
            return null;
        }
    }

    private @Nullable MediaType guessMimeType(@NonNull String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        path = path.replace("#", "");   //解决文件名中含有#号异常的问题
        String contentType = fileNameMap.getContentTypeFor(path);
        if (contentType == null) {
            contentType = MEDIA_TYPE_STREAM;
        }
        return MediaType.parse(contentType);
    }
}
