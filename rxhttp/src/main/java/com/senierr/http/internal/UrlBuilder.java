package com.senierr.http.internal;

import android.support.annotation.NonNull;

import java.util.LinkedHashMap;

/**
 * Http请求行构建器
 *
 * @author zhouchunjie
 * @date 2018/8/29
 */
public final class UrlBuilder {

    private @NonNull String baseUrl;
    private @NonNull LinkedHashMap<String, String> urlParams;

    public UrlBuilder(@NonNull String baseUrl) {
        this.baseUrl = baseUrl;
        urlParams = new LinkedHashMap<>();
    }

    public void addUrlParam(@NonNull String key, @NonNull String value) {
        urlParams.put(key, value);
    }

    public void addUrlParams(@NonNull LinkedHashMap<String, String> params) {
        for (String key : params.keySet()) {
            urlParams.put(key, params.get(key));
        }
    }

    @NonNull
    public String build() {
        if (!urlParams.isEmpty()) {
            StringBuilder strParams = new StringBuilder();
            if (baseUrl.contains("?")) {
                strParams.append("&");
            } else {
                strParams.append("?");
            }

            for (String key : urlParams.keySet()) {
                strParams.append("&").append(key).append("=").append(urlParams.get(key));
            }

            strParams.deleteCharAt(1);
            if (baseUrl.indexOf("?") == baseUrl.length() - 1) {
                strParams.deleteCharAt(0);
            }

            strParams.insert(0, baseUrl);
            baseUrl = strParams.toString();
        }
        return baseUrl;
    }

    @NonNull
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(@NonNull String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @NonNull
    public LinkedHashMap<String, String> getUrlParams() {
        return urlParams;
    }

    public void setUrlParams(@NonNull LinkedHashMap<String, String> urlParams) {
        this.urlParams = urlParams;
    }
}