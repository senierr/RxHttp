package com.senierr.sehttp.util;

import android.text.TextUtils;

import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * @author zhouchunjie
 * @date 2017/3/28
 */

public class HttpUtil {

    /**
     * 创建请求头部
     *
     * @param headerParams
     * @return
     */
    public static Headers buildHeaders(Map<String, String> headerParams){
        Headers headers = null;
        if (headerParams != null) {
            Headers.Builder builder = new Headers.Builder();
            for (String key: headerParams.keySet()) {
                builder.add(key, headerParams.get(key));
            }
            headers = builder.build();
        }
        return headers;
    }

    /**
     * 创建URL参数
     *
     * @param urlParams
     * @return
     */
    public static String buildUrlParams(Map<String, String> urlParams){
        StringBuilder strParams = new StringBuilder();
        if (urlParams != null) {
            strParams.append("?");
            for (String key: urlParams.keySet()) {
                strParams.append("&");
                strParams.append(key);
                strParams.append("=");
                strParams.append(urlParams.get(key));
            }
            strParams.deleteCharAt(1);
        }
        return strParams.toString();
    }

    /**
     * 创建键值对请求体
     *
     * @param bodyParams
     * @return
     */
    public static RequestBody buildRequestBody(Map<String, String> bodyParams){
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
     * 创建JSon格式请求体
     *
     * @param jsonStr
     * @return
     */
    public static RequestBody buildRequestBody(String jsonStr){
        RequestBody body = null;
        if (!TextUtils.isEmpty(jsonStr)) {
            body = RequestBody.create(MediaType.parse("application/json"), jsonStr);
        }
        return body;
    }
}
