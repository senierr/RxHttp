package com.senierr.http.builder;

import java.util.LinkedHashMap;

import io.reactivex.annotations.NonNull;

/**
 * Http请求行构建器
 *
 * @author zhouchunjie
 * @date 2018/8/29
 */
public final class UrlBuilder {

    private @NonNull String url;
    private @NonNull LinkedHashMap<String, String> urlParams = new LinkedHashMap<>();

    public UrlBuilder(String url) {
        this.url = url;
    }

    public void setUrl(@NonNull String url) {
        this.url = url;
    }

    public @NonNull String getUrl() {
        return url;
    }

    public void addUrlParam(@NonNull String key, @NonNull String value) {
        urlParams.put(key, value);
    }

    public void addUrlParams(@NonNull LinkedHashMap<String, String> params) {
        urlParams.putAll(params);
    }

    public @NonNull LinkedHashMap<String, String> getUrlParams() {
        return urlParams;
    }

    public @NonNull String build() {
        if (!urlParams.isEmpty()) {
            StringBuilder strParams = new StringBuilder();
            if (url.contains("?")) {
                strParams.append("&");
            } else {
                strParams.append("?");
            }

            for (String key : urlParams.keySet()) {
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