package com.senierr.sehttp.mode;

import java.util.Map;

import okhttp3.Headers;

/**
 * 请求头
 *
 * @author zhouchunjie
 * @date 2017/3/29
 */

public class HttpHeaders {

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
}
