package com.senierr.http.model;

import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Headers;

/**
 * Http请求头
 *
 * @author zhouchunjie
 * @date 2018/8/29
 */
public final class HttpHeaders {

    private LinkedHashMap<String, String> httpHeaders = new LinkedHashMap<>();

    public void addHeader(String key, String value) {
        httpHeaders.put(key, value);
    }

    public void addHeaders(LinkedHashMap<String, String> headers) {
        if (headers == null) return;
        for (String key: headers.keySet()) {
            httpHeaders.put(key, headers.get(key));
        }
    }

    public Headers generateHeaders(){
        Headers.Builder builder = new Headers.Builder();
        for (String key: httpHeaders.keySet()) {
            builder.add(key, httpHeaders.get(key));
        }
        return builder.build();
    }
}