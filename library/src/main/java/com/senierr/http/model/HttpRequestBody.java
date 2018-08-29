package com.senierr.http.model;

import com.senierr.http.util.Utils;

import java.io.File;
import java.util.LinkedHashMap;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.ByteString;

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

    private RequestBody requestBody;
    private MediaType mediaType;
    private LinkedHashMap<String, File> fileParams = new LinkedHashMap<>();
    private LinkedHashMap<String, String> stringParams = new LinkedHashMap<>();
    private String stringContent;
    private byte[] bytes;

    public void addRequestParam(String key, String value) {
        stringParams.put(key, value);
    }

    public void addRequestStringParams(LinkedHashMap<String, String> params) {
        if (params == null) return;
        for (String key: params.keySet()) {
            stringParams.put(key, params.get(key));
        }
    }

    public void addRequestParam(String key, File file) {
        fileParams.put(key, file);
    }

    public void addRequestFileParams(LinkedHashMap<String, File> params) {
        if (params == null) return;
        for (String key: params.keySet()) {
            fileParams.put(key, params.get(key));
        }
    }

    public void setRequestBody4JSon(String jsonStr) {
        stringContent = jsonStr;
        mediaType = MediaType.parse(MEDIA_TYPE_JSON);
    }

    public void setRequestBody4Text(String textStr) {
        stringContent = textStr;
        mediaType = MediaType.parse(MEDIA_TYPE_PLAIN);
    }

    public void setRequestBody4Xml(String xmlStr) {
        stringContent = xmlStr;
        mediaType = MediaType.parse(MEDIA_TYPE_XML);
    }

    public void setRequestBody4Byte(byte[] bytes) {
        this.bytes = bytes;
        mediaType = MediaType.parse(MEDIA_TYPE_STREAM);
    }

    public void setRequestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
    }

    public void setRequestBody(MediaType contentType, File file) {
        this.requestBody = RequestBody.create(contentType, file);
    }

    public void setRequestBody(MediaType contentType, byte[] content, int offset, int byteCount) {
        this.requestBody = RequestBody.create(contentType, content, offset, byteCount);
    }

    public void setRequestBody(MediaType contentType, byte[] content) {
        this.requestBody = RequestBody.create(contentType, content);
    }

    public void setRequestBody(MediaType contentType, ByteString content) {
        this.requestBody = RequestBody.create(contentType, content);
    }

    public void setRequestBody(MediaType contentType, String content) {
        this.requestBody = RequestBody.create(contentType, content);
    }

    public RequestBody generateRequestBody() {
        if (requestBody != null) {
            return requestBody;
        } else if (!fileParams.isEmpty()) {
            MultipartBody.Builder multipartBodybuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            for (String key: fileParams.keySet()) {
                File value = fileParams.get(key);
                RequestBody fileBody = RequestBody.create(Utils.guessMimeType(value.getPath()), value);
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
}
