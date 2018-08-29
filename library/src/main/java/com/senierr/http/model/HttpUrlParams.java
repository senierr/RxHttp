package com.senierr.http.model;

import com.senierr.http.internal.RequestFactory;
import com.senierr.http.util.Utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Http URL参数
 *
 * @author zhouchunjie
 * @date 2018/8/29
 */
public class HttpUrlParams {

    private final String url;
    private final LinkedHashMap<String, String> httpUrlParams;

    public HttpUrlParams(String url) {
        this.url = url;
        httpUrlParams = new LinkedHashMap<>();
    }

    public String generateUrl() {

    }

    /**
     * 添加请求参数
     *
     * @param key
     * @param value
     * @return
     */
    public void addUrlParam(String key, String value) {
        httpUrlParams.put(key, value);
    }

    /**
     * 添加多个请求参数
     *
     * @param params
     * @return
     */
    public void addUrlParams(LinkedHashMap<String, String> params) {
        httpUrlParams = Utils.mergeMap(httpUrlParams, params);
    }

    /**
     * 创建URL参数
     *
     * @param urlParams
     * @return
     */
    public static String buildUrlParams(String url, Map<String, String> urlParams){
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
