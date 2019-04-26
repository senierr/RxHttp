package com.senierr.http.builder;

import java.util.LinkedHashMap;

import io.reactivex.annotations.NonNull;
import okhttp3.Headers;

/**
 * Http请求头构建器
 *
 * @author zhouchunjie
 * @date 2018/8/29
 */
public final class HeaderBuilder {

    private LinkedHashMap<String, String> headers = new LinkedHashMap<>();

    public void addHeader(@NonNull String key, @NonNull String value) {
        headers.put(key, value);
    }

    public void addHeaders(@NonNull LinkedHashMap<String, String> headers) {
        this.headers.putAll(headers);
    }

    public @NonNull LinkedHashMap<String, String> getHeaders() {
        return headers;
    }

    public @NonNull Headers build(){
        Headers.Builder builder = new Headers.Builder();
        for (String key: headers.keySet()) {
            String value = headers.get(key);
            if (value != null && value.length() > 0) {
                builder.add(key, value);
            }
        }
        return builder.build();
    }
}