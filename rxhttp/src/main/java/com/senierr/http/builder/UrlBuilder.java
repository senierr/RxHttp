package com.senierr.http.builder;

import android.text.TextUtils;

import java.util.LinkedHashMap;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

/**
 * Http请求行构建器
 *
 * @author zhouchunjie
 * @date 2018/8/29
 */
public final class UrlBuilder {

    private @Nullable String baseUrl;
    private @NonNull String url;
    private @NonNull LinkedHashMap<String, String> urlParams;
    private boolean ignoreBaseUrl;

    public UrlBuilder(@NonNull String url) {
        this.url = url;
        urlParams = new LinkedHashMap<>();
        ignoreBaseUrl = false;
    }

    public @NonNull UrlBuilder setBaseUrl(@Nullable String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public @NonNull UrlBuilder addUrlParam(@NonNull String key, @NonNull String value) {
        urlParams.put(key, value);
        return this;
    }

    public @NonNull UrlBuilder addUrlParams(@NonNull LinkedHashMap<String, String> params) {
        for (String key : params.keySet()) {
            urlParams.put(key, params.get(key));
        }
        return this;
    }

    public @NonNull UrlBuilder setIgnoreBaseUrl(boolean ignoreBaseUrl) {
        this.ignoreBaseUrl = ignoreBaseUrl;
        return this;
    }

    public @NonNull String build() {
        String actualUrl = url;
        if (!TextUtils.isEmpty(baseUrl) && !ignoreBaseUrl) {
            actualUrl = baseUrl + url;
        }

        if (!urlParams.isEmpty()) {
            StringBuilder strParams = new StringBuilder();
            if (actualUrl.contains("?")) {
                strParams.append("&");
            } else {
                strParams.append("?");
            }

            for (String key : urlParams.keySet()) {
                strParams.append("&").append(key).append("=").append(urlParams.get(key));
            }

            strParams.deleteCharAt(1);
            if (actualUrl.indexOf("?") == actualUrl.length() - 1) {
                strParams.deleteCharAt(0);
            }

            strParams.insert(0, actualUrl);
            actualUrl = strParams.toString();
        }
        return actualUrl;
    }
}