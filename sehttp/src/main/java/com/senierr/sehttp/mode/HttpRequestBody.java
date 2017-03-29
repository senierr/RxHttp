package com.senierr.sehttp.mode;

import android.text.TextUtils;

import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 请求体
 *
 * @author zhouchunjie
 * @date 2017/3/29
 */

public class HttpRequestBody {

    public static final String MEDIA_TYPE_PLAIN = "text/plain; charset=utf-8";
    public static final String MEDIA_TYPE_XML = "text/xml; charset=utf-8";
    public static final String MEDIA_TYPE_JSON = "application/json; charset=utf-8";
    public static final String MEDIA_TYPE_STREAM = "application/octet-stream";

    /**
     * 创建表单格式请求体
     *
     * @param bodyParams
     * @return
     */
    public static RequestBody buildRequestBody4Form(Map<String, String> bodyParams){
        RequestBody body = null;
        if (bodyParams != null) {
            FormBody.Builder builder = new FormBody.Builder();
            for (String key: bodyParams.keySet()) {
                builder.add(key, bodyParams.get(key));
            }
            body = builder.build();
        }
        return body;
    }

    /**
     * 创建JSON格式请求体
     *
     * @param jsonStr
     * @return
     */
    public static RequestBody buildRequestBody4Json(String jsonStr){
        RequestBody body = null;
        if (!TextUtils.isEmpty(jsonStr)) {
            body = RequestBody.create(MediaType.parse(MEDIA_TYPE_JSON), jsonStr);
        }
        return body;
    }

    /**
     * 创建文本格式请求体
     *
     * @param textStr
     * @return
     */
    public static RequestBody buildRequestBody4Text(String textStr){
        RequestBody body = null;
        if (!TextUtils.isEmpty(textStr)) {
            body = RequestBody.create(MediaType.parse(MEDIA_TYPE_PLAIN), textStr);
        }
        return body;
    }

    /**
     * 创建XML格式请求体
     *
     * @param xmlStr
     * @return
     */
    public static RequestBody buildRequestBody4Xml(String xmlStr){
        RequestBody body = null;
        if (!TextUtils.isEmpty(xmlStr)) {
            body = RequestBody.create(MediaType.parse(MEDIA_TYPE_XML), xmlStr);
        }
        return body;
    }

}
