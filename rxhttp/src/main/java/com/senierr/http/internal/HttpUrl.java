package com.senierr.http.internal;

import android.support.annotation.NonNull;

import java.util.LinkedHashMap;

/**
 * Http URL
 *
 * @author zhouchunjie
 * @date 2018/8/29
 */
public final class HttpUrl {

    private @NonNull String url;
    private @NonNull LinkedHashMap<String, String> urlParams = new LinkedHashMap<>();

    public HttpUrl(@NonNull String url) {
        this.url = url;
    }

    public void addUrlParam(@NonNull String key, @NonNull String value) {
        urlParams.put(key, value);
    }

    public void addUrlParams(@NonNull LinkedHashMap<String, String> params) {
        for (String key: params.keySet()) {
            urlParams.put(key, params.get(key));
        }
    }

    public @NonNull String generateUrl() {
        if (!urlParams.isEmpty()) {
            StringBuilder strParams = new StringBuilder();
            if (url.contains("?")) {
                strParams.append("&");
            } else {
                strParams.append("?");
            }

            for (String key: urlParams.keySet()) {
                strParams.append("&").append(key).append("=").append(urlParams.get(key));
            }

            strParams.deleteCharAt(1);
            if (url.indexOf("?") == url.length() - 1) {
                strParams.deleteCharAt(0);
            }

            strParams.insert(0, url);
            url = strParams.toString();
        }
        return url;
    }
}