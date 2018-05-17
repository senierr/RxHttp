package com.senierr.sehttp.internal;

import com.senierr.sehttp.entity.FileMap;
import com.senierr.sehttp.util.HttpUtil;

import java.io.File;
import java.util.LinkedHashMap;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * 请求体构造器
 *
 * @author zhouchunjie
 * @date 2018/05/17
 */

public class RequestBodyBuilder {

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
    public RequestBody build() {
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
}
