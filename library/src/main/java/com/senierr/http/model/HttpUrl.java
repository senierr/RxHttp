package com.senierr.http.model;

import android.text.TextUtils;

import java.util.LinkedHashMap;

/**
 * Http URL
 *
 * @author zhouchunjie
 * @date 2018/8/29
 */
public final class HttpUrl {

    private String url;
    private LinkedHashMap<String, String> urlParams = new LinkedHashMap<>();

    public HttpUrl(String url) {
        this.url = url;
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("The url must not be null!");
        }
    }

    public void addUrlParam(String key, String value) {
        urlParams.put(key, value);
    }

    public void addUrlParams(LinkedHashMap<String, String> params) {
        if (params == null) return;
        for (String key: params.keySet()) {
            urlParams.put(key, params.get(key));
        }
    }

    public String generateUrl() {
        if (urlParams != null && !urlParams.isEmpty()) {
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